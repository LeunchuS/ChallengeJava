package com.leandrocarron.challengejava.controller;

import com.leandrocarron.challengejava.dto.responseDTO.ProcessIdResponseDTO;
import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.model.ProcessStatus;
import com.leandrocarron.challengejava.service.FileProcessService;
//logs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@RestController
@RequestMapping("/fileProcess")
public class FileProcessController {

    private final FileProcessService fileProcessService;

    public FileProcessController(FileProcessService fileProcessService) {
        this.fileProcessService = fileProcessService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ProcessIdResponseDTO> uploadCSV(@RequestParam("file") MultipartFile file) throws IOException {
        //first step: verify if file is empty/null or has a diferent content than csv
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ProcessIdResponseDTO(null,"File is empty or missing"));
        }
        if (!"text/csv".equalsIgnoreCase(file.getContentType())) {
            return ResponseEntity.badRequest().body(new ProcessIdResponseDTO(null,"Invalid file type. It must be a csv file"));
        }
        //a FileProcess is created and returning. PENDING STATE
        FileProcess process = fileProcessService.createFileProcess();
        //this is how I get de porcessId that I need to send in the response
        Long processId = process.getFileProcessId();
        //Save file content in a temporary file to prevent issues
        File tempFile = File.createTempFile("upload-", ".csv");
        file.transferTo(tempFile);
        //Now the content of the uploaded file is processed
        fileProcessService.processCSVAsync( tempFile, processId);

        return ResponseEntity
                .accepted()
                .body(new ProcessIdResponseDTO(processId, ProcessStatus.PENDING.toString()));
    }


}
