package com.leandrocarron.challengejava.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessStats {
    private long total;
    private long processed;
    private long errors;
    private long duplicated;
}