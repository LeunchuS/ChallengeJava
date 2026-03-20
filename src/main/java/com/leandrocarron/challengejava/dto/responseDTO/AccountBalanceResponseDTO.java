package com.leandrocarron.challengejava.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountBalanceResponseDTO {
    private Long accountId;
    private BigDecimal amount;
}
