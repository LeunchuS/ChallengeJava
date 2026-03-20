package com.leandrocarron.challengejava.service;

import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.model.ProcessStatus;
import com.leandrocarron.challengejava.model.Transaction;
import com.leandrocarron.challengejava.model.TransactionType;
import com.leandrocarron.challengejava.repository.ProcessRepository;
import com.leandrocarron.challengejava.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
//logs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//--------
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.*;

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
            //Creates a pool with threat that can be reused
            ExecutorService executor = Executors.newFixedThreadPool(consumers + 1);
            //put the task to execute. I send processId and not FileProcess to avoid errors
            executor.submit(() -> produce(file, queue, consumers, processId));
            //now the consumers are used
            for (int i = 0; i < consumers; i++) {
                executor.submit(createConsumer(queue, stats, processId));
            }
            executor.shutdown();
            //threads needs to finish their excecutions before
            executor.awaitTermination(1, TimeUnit.HOURS);
            //delete the file after processes it
            if (file.exists()) file.delete();
            log.info("CSV file {}, processId {} - processing COMPLETED", file.getName(), processId);
            process.setStatus(ProcessStatus.COMPLETED);
        } catch (Exception e) {
            log.error("CSV file {}, processId {} - processing FAILED ",file.getName(),processId,e);
            process.setStatus(ProcessStatus.FAILED);
        } finally {
            process.updateStates(stats);
            log.info("CSV file {}, processId {} - STATS total= {}, processed= {}, error= {}, duplicated= {}", file.getName(),processId, stats.getTotal(),stats.getProcessed(),stats.getErrors(),stats.getDuplicated());
            processRepository.save(process);
            if (file.exists()) file.delete();
        }
    }

    private Transaction mapToTransaction(String line) {
        //separate string in pieces
        String[] parts = line.split(",");
        //csv needs 5 pieces: transactionId, accountId,amount,type,timestamp
        if (parts.length != 5) {
            log.warn("Transaction parsing - invalid line format: ", line);
            //throw new RuntimeException("Invalid line format");
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
            //throw new RuntimeException("Error parsing line: {}" + line, e);
        }
    }


    public FileProcess createFileProcess() {
            FileProcess process = new FileProcess(ProcessStatus.PENDING, 0, 0, 0, 0);
            return processRepository.save(process);
    }

    private void processLine(String line, ProcessStats stats){
        stats.incrementTotal();
        try {
            Transaction transaction = mapToTransaction(line);
            //new iteration after finding a duplicated id
            if (transactionRepository.existsById(transaction.getTransactionId())) {
                log.warn("Transaction parsing - duplicated id {}", transaction.getTransactionId());
                stats.incrementDuplicated();
                return;
            }
            transactionRepository.save(transaction);
            stats.incrementProcessed();
        } catch (Exception e) {
            log.error("Transaction parsing - Something went wrong: ",e);
            stats.incrementError();
        }
    }

    //To avoid errors trying to create a Transaction with header data.
    public boolean isHeader(String line){
        return (line.toUpperCase()).contains("transactionId".toUpperCase());
    }

    //I'm trying to use producer separately
    private void produce(File file, BlockingQueue<String> queue, int consumers, long processId) {
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
            //consumer will stop when read this line
            for(int c = 0; c<consumers; c++)
                queue.put("CONSUMER_STOP");

            log.info("idProcess {} - Producer finishes filling queue. Total items {}",processId ,queue.size());
        } catch (Exception e) {
            log.error("idProcess {} - Queue failed", processId, e);
        }
    }

    //Here is where every string in queue is converted into instance and saved after that
    private Runnable createConsumer(BlockingQueue<String> queue, ProcessStats stats, Long processId) {
        return () -> {
            log.info("processId {} - Consumer started taking items from queue");
            try {
                while (true) {
                    String line = queue.take();
                    //terminates consumer when queue reach CONSUMER_STOP line
                    if ("CONSUMER_STOP".equals(line)) break;
                    processLine(line, stats);

                }
                log.info("processId {} - Consumer finished taking items from queue");
            } catch (InterruptedException e) {
                log.error("Consumer Thread interrupted. ",e);
                Thread.currentThread().interrupt();
            }
        };
    }
}
