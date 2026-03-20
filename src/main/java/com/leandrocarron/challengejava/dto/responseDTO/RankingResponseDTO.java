package com.leandrocarron.challengejava.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RankingResponseDTO {
    List<Ranked> rankedList;
}
