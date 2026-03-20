package com.leandrocarron.challengejava.controller;

import com.leandrocarron.challengejava.dto.responseDTO.ProcessIdResponseDTO;
import com.leandrocarron.challengejava.exception.FileException;
import com.leandrocarron.challengejava.exception.FileErrorType;
import com.leandrocarron.challengejava.model.FileProcess;
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
    private static final Logger log = LoggerFactory.getLogger(FileProcessController.class);
    private final FileProcessService fileProcessService;

    public FileProcessController(FileProcessService fileProcessService) {
        this.fileProcessService = fileProcessService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ProcessIdResponseDTO> uploadCSV(@RequestParam("file") MultipartFile file) {
        //verify if uploaded file. not csv or empty
        if (file == null || file.isEmpty()) {
            throw new FileException(FileErrorType.EMPTY_FILE,"El archivo está vacío o no fue enviado");
        }
        if (!file.getOriginalFilename().endsWith(".csv")) {
            throw new FileException(FileErrorType.INVALID_CONTENT, "Archivo con extensión incorrecta. Se requiere csv");
        }
        //a FileProcess is created and returning. PENDING STATE
        FileProcess process = fileProcessService.createFileProcess();
        //this is how I get de porcessId that I need to send in the response
        Long processId = process.getFileProcessId();
        //Save file content in a temporary file to prevent issues
        File tempFile;
        try {
            tempFile = File.createTempFile("upload-", ".csv");
            file.transferTo(tempFile);
            log.info(" processId {} - temporary copy of the file was created and save until the hole process ends");
        } catch (IOException e) {
            throw new FileException( FileErrorType.IO_ERROR,"El archivo no pudo guardarse en disco");
        }
        //Now the content of the uploaded file is processed
        fileProcessService.processCSVAsync( tempFile, processId);

        return ResponseEntity
                .accepted()
                .body(new ProcessIdResponseDTO(processId));
    }


}
