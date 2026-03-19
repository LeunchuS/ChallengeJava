package com.leandrocarron.challengejava.model;

import com.leandrocarron.challengejava.service.ProcessStats;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name="FILE_PROCESS")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileProcess {
    @Id
    //Using H2 it's necesary to create the secuence in schema.sql
    @SequenceGenerator(name = "process_seq", sequenceName = "PROCESS_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "process_seq")
    private long fileProcessId;
    private long totalRecords;
    private long processedRecords;
    private long errorRecords;
    private long duplicatedRecords;
    private Timestamp created_at;
    private ProcessStatus status;

    public FileProcess(ProcessStatus status,long totalRecords, long processedRecords, long errorRecords, long duplicatedRecords){
        this.status = status;
        this.totalRecords = totalRecords;
        this.processedRecords = processedRecords;
        this.errorRecords = errorRecords;
        this.duplicatedRecords = duplicatedRecords;
        created_at = new Timestamp(System.currentTimeMillis());
    }

    public void setStats(ProcessStats stats){
        this.totalRecords = stats.getTotal();
        this.processedRecords = stats.getProcessed();
        this.errorRecords = stats.getErrors();
        this.duplicatedRecords = stats.getDuplicated();
    }
}
