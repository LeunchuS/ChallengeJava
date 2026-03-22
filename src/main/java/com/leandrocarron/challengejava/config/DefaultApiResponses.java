package com.leandrocarron.challengejava.config;

import com.leandrocarron.challengejava.dto.ErrorDTO.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse(
        responseCode = "400",
        description = "Requerimiento inválido",
        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
)
@ApiResponse(
        responseCode = "404",
        description = "Recurso no encontrado",
        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
)
@ApiResponse(
        responseCode = "500",
        description = "Error inesperado",
        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
)

public @interface DefaultApiResponses {
}