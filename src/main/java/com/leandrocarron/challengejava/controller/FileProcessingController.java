package com.leandrocarron.challengejava.controller;

import com.leandrocarron.challengejava.dto.ErrorDTO.ErrorResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.AccountBalanceResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.ProcessIdResponseDTO;
import com.leandrocarron.challengejava.exception.FileException;
import com.leandrocarron.challengejava.exception.FileErrorType;
import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.model.ProcessStatus;
import com.leandrocarron.challengejava.service.FileProcessService;
//logs
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Tag(name = "File Processing", description = "Operaciones de carga de datos")

@RestController
@RequestMapping("/fileProcess")

public class FileProcessingController {
    private static final Logger log = LoggerFactory.getLogger(FileProcessingController.class);
    private final FileProcessService fileProcessService;

    public FileProcessingController(FileProcessService fileProcessService) {
        this.fileProcessService = fileProcessService;
    }

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //operations makes that swagger-ui schemes shows DataResponseErrorDTO like default
    @Operation(
            summary = "Carga un archivo csv para procesarlo, convertirlo en transacciones y almacenarlas",
            description = "Retorna el processingId generado",
           responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "OK",
                            content = @Content(
                                    schema = @Schema(implementation = ProcessIdResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponseDTO.class)
                            )
                    )
            })
    //parameters allows upload csv on swagger like string
    public ResponseEntity<ProcessIdResponseDTO> uploadCSV(@Parameter(description = "Archivo CSV", required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "string", format = "binary"))) @RequestParam("file") MultipartFile file) {
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
        ProcessStatus processStatus= ProcessStatus.FAILED;
        process.setStatus(processStatus);
        fileProcessService.saveProcess(process);
        fileProcessService.processCSVAsync( tempFile, processId);

        return ResponseEntity
                .accepted()
                .body(new ProcessIdResponseDTO(processId));
    }


}
