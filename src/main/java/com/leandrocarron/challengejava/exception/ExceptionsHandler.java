package com.leandrocarron.challengejava.exception;

import com.leandrocarron.challengejava.dto.ErrorDTO.ErrorResponseDTO;
//for loggin
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.rmi.UnexpectedException;

@Hidden
@RestControllerAdvice
public class ExceptionsHandler {
    private static final Logger log = LoggerFactory.getLogger(ExceptionsHandler.class);


    //For disconected DB uses
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseError(DataAccessException e) {
        log.error("Database error", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponseDTO("Database error", "DB_ERROR"));
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileErrors(FileException e){
        log.warn("File consulting error", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage(),e.getFileErrorType().toString()));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileErrors(FileUploadException e){
        log.warn("File uploading error", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage(),e.getFileErrorType().toString()));
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Param error", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Param error","PARAM ERROR"));
    }


    //Used when the exception isn't catched by another handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Unexpected error occurred","INTERNAL_ERROR"));
    }


}
