package com.leandrocarron.challengejava.dto.responseDTO;

import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.model.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessResponseDTO {
    private ProcessStatus processStatus;
    private Long total;
    private Long processed;
    private Long error;

    public void prepareDTO(FileProcess fileProcess){
        this.processStatus = fileProcess.getStatus();
        this.total = fileProcess.getTotalRecords();
        this.processed = fileProcess.getProcessedRecords();
        this.error = fileProcess.getErrorRecords();
    }
}
