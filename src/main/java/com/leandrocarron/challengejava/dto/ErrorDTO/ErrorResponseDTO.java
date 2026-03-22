package com.leandrocarron.challengejava.dto.ErrorDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Respuesta de error estándar")
public class ErrorResponseDTO {
    @Schema(description = "Resumen de lo acontecido")
    private String message;
    @Schema(description = "codigo de error")
    private String code;
    @Schema(description = "momento de origen")
    private String timestamp;

    public ErrorResponseDTO(String message, String code) {
        this.message = message;
        this.code = code;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}