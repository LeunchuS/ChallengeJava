package com.leandrocarron.challengejava.service;

import com.leandrocarron.challengejava.exception.FileErrorType;
import com.leandrocarron.challengejava.exception.FileException;
import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.model.ProcessStatus;
import com.leandrocarron.challengejava.model.Transaction;
import com.leandrocarron.challengejava.model.TransactionType;
import com.leandrocarron.challengejava.repository.ProcessRepository;
import com.leandrocarron.challengejava.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
//logs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
//--------
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@AllArgsConstructor
public class FileProcessService {

    private final ProcessRepository processRepository;
    private final TransactionRepository transactionRepository;
    private static final Logger log = LoggerFactory.getLogger(FileProcessService.class);


    @Async("csvProcessorExecutor")
    public void processCSVAsync(File file, Long processId){
        //get process to update atomic status
        FileProcess process = processRepository.findById(processId).orElseThrow();
        ProcessStats stats = new ProcessStats();
        try {
            //queue for producer consumer pattern
            LinkedBlockingDeque queue = new LinkedBlockingDeque<>();
            process.setStatus(ProcessStatus.PROCESSING);
            processRepository.save(process);
            log.info("CSV file {} - processing started. processId: {}", file.getName(), processId);
            //number of consumers
            int consumers = 4;
            //used to save fail status
            AtomicBoolean fail = new AtomicBoolean();
            //Creates a pool with threat that can be reused
            ExecutorService executor = Executors.newFixedThreadPool(consumers + 1);
            //put the task to execute. I send processId and not FileProcess to avoid errors
            Future<?> producerFuture = executor.submit(() -> {
                try {
                    produce(file, queue, consumers, processId);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            //now the consumers are used
            List<Future<?>> consumersFuture = new ArrayList<>();
            for (int i = 0; i < consumers; i++) {
                consumersFuture.add(executor.submit(createConsumer(queue, stats, processId)));
            }
            executor.shutdown();
            //Has the producer finished without problems?
            try {
                producerFuture.get();
            } catch (Exception e) {
                log.error("Producer failed", e.getCause());
                process.setStatus(ProcessStatus.FAILED);
                executor.shutdownNow();
            }

            //Has the consumers finished without problems?
            for (Future<?> f : consumersFuture) {
                try {
                    f.get();
                } catch (Exception e) {
                    process.setStatus(ProcessStatus.FAILED);
                    log.error("Consumer failed", e.getCause());
                    executor.shutdownNow();
                    break;
                }
            }

            //threads needs to finish their excecutions before
            executor.awaitTermination(1, TimeUnit.HOURS);

            log.info("CSV file {}, processId {} - processing COMPLETED", file.getName(), processId);
            process.setStatus(ProcessStatus.COMPLETED);
        } catch (Exception e) {
            log.error("CSV file {}, processId {} - processing FAILED ",file.getName(),processId,e);
            process.setStatus(ProcessStatus.FAILED);
        } finally {
            process.updateStates(stats);
            log.info("CSV file {}, processId {} - STATS total= {}, processed= {}, error= {}, duplicated= {}", file.getName(),processId, stats.getTotal(),stats.getProcessed(),stats.getErrors(),stats.getDuplicated());
            processRepository.save(process);
            //delete the file after processes it
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.error("Error deleting file {}", file.getName(), e);
            }
        }
    }

    private Transaction mapToTransaction(String line) {
        //separate string in pieces
        String[] parts = line.split(",");
        //csv needs 5 pieces: transactionId, accountId,amount,type,timestamp
        if (parts.length != 5) {
            log.warn("Transaction parsing - invalid line format: ", line);
            return null;
        }
        try {//it can be created using AllArgsConstructor
            Transaction transaction = new Transaction();
            transaction.setTransactionId(Long.parseLong(parts[0]));
            transaction.setAccountId(Long.parseLong(parts[1]));
            transaction.setAmount(new BigDecimal(parts[2]));
            transaction.setType(TransactionType.valueOf(parts[3]));

            LocalDateTime timeStampLDT = LocalDateTime.parse(parts[4].trim());
            Instant instant = timeStampLDT.atZone(ZoneId.of("UTC")).toInstant();
            transaction.setCreatedAt(Timestamp.from(instant));

            return transaction;
        } catch (Exception e) {
            log.error("Transaction parsing - error parsing line: {}", line);
            return null;
        }
    }


    private void processLine(String line, ProcessStats stats, Long processId){
        stats.incrementTotal();
        //before try-catch. This allows me to use it inside catch block when duplicate throws an exception
        Transaction transaction = null;
        try {
            transaction = mapToTransaction(line);
            //if something went wrong
            if (transaction == null) {
                log.error("processId {} - Transaction parsing error, line: {}", processId,line);
                stats.incrementError();
                return;
            }
            //verifying duplicated but it is not 100% safe on concurrency.
            /*It was replaced by DataIntegrityViolationException
            if (transactionRepository.existsById(transaction.getTransactionId())) {
                log.warn("Transaction parsing - duplicated id {}", transaction.getTransactionId());
                stats.incrementDuplicated();
                return;
            }*/
            transactionRepository.save(transaction);
            stats.incrementProcessed();
        } catch (DataIntegrityViolationException e) {
            log.warn("processId {} - duplicated transactionId {}", processId,
                    transaction != null ? transaction.getTransactionId() : "unknown");
            stats.incrementDuplicated();
        }catch (Exception e) {
            log.error("processId {} - Transaction parsing error, line: {}", processId,line);
            stats.incrementError();
        }
    }

    //To avoid errors trying to create a Transaction with header data.
    public boolean isHeader(String line){
        return (line.toUpperCase()).contains("transactionId".toUpperCase());
    }

    //I'm trying to use producer separately
    private void produce(File file, BlockingQueue<String> queue, int consumers, long processId) throws InterruptedException {
        log.info("idProcess {} - Producer starts filling the queue", processId);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            //Is the first line a header or null?
            String line = reader.readLine();
            if (line != null && !isHeader(line)) {
                queue.put(line);
            }
            //Now everything left on the file is only data
            while ((line = reader.readLine()) != null) {
                queue.put(line);
            }
            log.info("idProcess {} - Producer finishes filling queue. Total items {}",processId ,queue.size());
        } catch (Exception e) {
            log.error("idProcess {} - Queue failed", processId, e);
            throw new RuntimeException(e);
        }finally {
            //consumer will stop when read this line
            for(int c = 0; c<consumers; c++)
                queue.put("__CONSUMER_STOP__POISNON_PILL");
        }
    }

    //Here is where every string in queue is converted into instance and saved after that
    private Runnable createConsumer(BlockingQueue<String> queue, ProcessStats stats, Long processId) {
        return () -> {
            log.info("processId {} - Consumer started taking items from queue", processId);
            try {
                while (true) {
                    String line = queue.take();
                    //terminates consumer when queue reach CONSUMER_STOP line
                    if ("__CONSUMER_STOP__POISNON_PILL".equals(line)) break;
                    processLine(line, stats, processId);

                }
                log.info("processId {} - Consumer finished taking items from queue", processId);
            } catch (InterruptedException e) {
                log.error("Consumer Thread interrupted. ",e);
                Thread.currentThread().interrupt();
            }
        };
    }

    public FileProcess newProcessing(){
        try{
        //a FileProcess is created and returning. PENDING STATE
        FileProcess process = new FileProcess(ProcessStatus.PENDING, 0, 0, 0, 0);
        return processRepository.save(process);
        } catch (DataAccessException e) {
            throw new FileException(FileErrorType.DB_ERROR,"Error al generar el nuevo procesamiento");
        }

    }

    public File createTemporaryFile(MultipartFile multipartFile, FileProcess fileProcess)  {
        File file = null;
        try {
            //Save file content in a temporary file to prevent issues
            file = File.createTempFile("upload-" + fileProcess.getFileProcessId(), ".csv");
            multipartFile.transferTo(file);
            log.info(" processId {} - temporary copy of the file was created and save until the hole process ends", fileProcess.getFileProcessId());
            return file;
        }catch (IOException e){
            ProcessStatus processStatus = ProcessStatus.FAILED;
            fileProcess.setStatus(processStatus);
            processRepository.save(fileProcess);
            //deleting the file
            if (file != null && file.exists() && !file.delete()) {
                log.warn("No se pudo eliminar el archivo temporal {}", file.getAbsolutePath());
            }
            throw new FileException(FileErrorType.IO_ERROR, "Error al generar archivo temporal");
        }
    }

}
