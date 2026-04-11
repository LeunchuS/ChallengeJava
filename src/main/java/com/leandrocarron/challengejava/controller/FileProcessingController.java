package com.leandrocarron.challengejava.controller;

import com.leandrocarron.challengejava.config.DefaultApiResponses;
import com.leandrocarron.challengejava.dto.ErrorDTO.ErrorResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.ProcessIdResponseDTO;
import com.leandrocarron.challengejava.exception.FileErrorType;
import com.leandrocarron.challengejava.exception.FileException;
import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.service.FileProcessService;
//logs
import com.leandrocarron.challengejava.validation.annotation.CSVFile;
import com.leandrocarron.challengejava.validation.annotation.MaxFileSize;
import com.leandrocarron.challengejava.validation.annotation.NotEmptyFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

@Tag(name = "File Processing", description = "Operaciones de carga de datos")

@RestController
@RequestMapping("/fileProcess")

public class FileProcessingController {
    private static final Logger log = LoggerFactory.getLogger(FileProcessingController.class);
    private final FileProcessService fileProcessService;

    public FileProcessingController(FileProcessService fileProcessService) {
        this.fileProcessService = fileProcessService;
    }


    //operations makes that swagger-ui schemes shows DataResponseErrorDTO like default
    @Operation(
            summary = "Carga un archivo csv headers para procesarlo, convertirlo en transacciones y almacenarlas",
            description = "Retorna el processingId generado",
            responses = {
                    @ApiResponse(responseCode = "202",description = "Requerimiento aceptado",
                            content = @Content(schema = @Schema(implementation = ProcessIdResponseDTO.class))),
                    @ApiResponse(responseCode = "503",description = "Base de datos no disponible",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                    )
            }
            )
    @DefaultApiResponses
    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //parameters allows upload csv on swagger like string
    public ResponseEntity<ProcessIdResponseDTO> uploadCSV(@Parameter(description = "Archivo CSV", required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "string", format = "binary"))) @RequestParam("file") @NotNull @NotEmptyFile @CSVFile @MaxFileSize(15*1024*1024) MultipartFile file) {

        //new processing inicialization
        FileProcess fileProcess = null;
        File tempFile = null;
        //The exceptions throwed by the following service functions work like a break. Async function will never be excecuted
        fileProcess = fileProcessService.newProcessing();
        tempFile = fileProcessService.createTemporaryFile(file, fileProcess);

        fileProcessService.processCSVAsync( tempFile, fileProcess.getFileProcessId());

        return ResponseEntity
                .accepted()
                .body(new ProcessIdResponseDTO(fileProcess.getFileProcessId()));
    }


}
