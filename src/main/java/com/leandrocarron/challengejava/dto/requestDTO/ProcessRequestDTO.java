package com.leandrocarron.challengejava.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProcessRequestDTO {
    @NotNull(message = "El campo 'processingId' no puede ser nulo")
    @NotBlank(message = "El campo 'processingId' no puede estar vacío")
    private Long processingId;
}
