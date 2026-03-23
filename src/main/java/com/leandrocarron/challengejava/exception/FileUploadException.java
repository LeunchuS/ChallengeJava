package com.leandrocarron.challengejava.exception;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FileUploadException extends RuntimeException {
    private FileErrorType fileErrorType;
    private String message;
}
