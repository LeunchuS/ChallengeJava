package com.leandrocarron.challengejava.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountBalanceRequestDTO {
    private Long accountId;
}
