package com.leandrocarron.challengejava.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessIdResponseDTO {
    private Long processingID;
    private String msg;
}
