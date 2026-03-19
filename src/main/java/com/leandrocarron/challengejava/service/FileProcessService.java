package com.leandrocarron.challengejava.service;

import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.model.ProcessStatus;
import com.leandrocarron.challengejava.model.Transaction;
import com.leandrocarron.challengejava.model.TransactionType;
import com.leandrocarron.challengejava.repository.ProcessRepository;
import com.leandrocarron.challengejava.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@AllArgsConstructor
@Data
public class FileProcessService {

    private final ProcessRepository processRepository;
    private final TransactionRepository transactionRepository;


    @Async("csvProcessorExecutor")
    public void processCSVAsync(File file, Long processId){
        FileProcess process = processRepository.findById(processId).orElseThrow();
        //update FileProcess status
        process.setStatus(ProcessStatus.PROCESSING);
        processRepository.save(process);
        //inicialize stats to save
        ProcessStats stats = new ProcessStats(0,0,0,0);
        //now the file processing starts
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            //it'll represent a trasanction or record in the DB table
            String line = reader.readLine();
            //first line is a header?
            if (!isHeader(line)) {
                processLine(line, stats);
            }

            while ((line = reader.readLine()) != null) {
                processLine(line, stats);
            }

            process.setStatus(ProcessStatus.COMPLETED);

        } catch (Exception e) {
            process.setStatus(ProcessStatus.FAILED);
        } finally {
            //delete the file
            if (file.exists()) {
                file.delete();
            }
        }
        //if
        process.setStats(stats);

        processRepository.save(process);
    }

    private Transaction mapToTransaction(String line) {
        //separate string in pieces
        String[] parts = line.split(",");
        //csv needs 5 pieces: transactionId, accountId,amount,type,timestamp
        if (parts.length != 5) {
            throw new RuntimeException("Invalid line format");
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
            throw new RuntimeException("Error parsing line: " + line, e);
        }
    }


    public FileProcess createFileProcess() {
            FileProcess process = new FileProcess(ProcessStatus.PENDING, 0, 0, 0, 0);
            return processRepository.save(process);
    }

    private void processLine(String line, ProcessStats stats){
        stats.setTotal(stats.getTotal()+1);
        try {
            Transaction transaction = mapToTransaction(line);
            //new iteration after finding a duplicated id
            if (transactionRepository.existsById(transaction.getTransactionId())) {
                stats.setDuplicated(stats.getDuplicated()+1);
                return;
            }
            transactionRepository.save(transaction);
            stats.setProcessed(stats.getProcessed()+1);
        } catch (Exception e) {
            stats.setErrors(stats.getErrors()+1);
        }
    }

    //To avoid errors trying to create a Transaction with header data.
    public boolean isHeader(String line){
        return (line.toUpperCase()).contains("transactionId".toUpperCase());
    }
}
