package com.leandrocarron.challengejava.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum FileErrorType {
    EMPTY_FILE(HttpStatus.BAD_REQUEST),
    INVALID_EXTENSION(HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_CONTENT(HttpStatus.BAD_REQUEST),
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND(HttpStatus.NOT_FOUND);

    private final HttpStatus httpStatus;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}