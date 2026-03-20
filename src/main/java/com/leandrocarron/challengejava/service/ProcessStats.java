package com.leandrocarron.challengejava.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

@NoArgsConstructor
@Data
public class ProcessStats {
    private AtomicLong total = new AtomicLong(0);
    private AtomicLong processed = new AtomicLong(0);
    private AtomicLong errors = new AtomicLong(0);
    private AtomicLong duplicated = new AtomicLong(0);

    public void incrementTotal(){
        total.incrementAndGet();
    }

    public void incrementProcessed(){
        processed.incrementAndGet();
    }

    public void incrementError(){
        errors.incrementAndGet();
    }

    public void incrementDuplicated(){
        duplicated.incrementAndGet();
    }
}