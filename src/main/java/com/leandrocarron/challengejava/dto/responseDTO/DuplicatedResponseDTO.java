package com.leandrocarron.challengejava.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DuplicatedResponseDTO {
    private Long transactionId;
    private boolean result;
    private String msg;
}
