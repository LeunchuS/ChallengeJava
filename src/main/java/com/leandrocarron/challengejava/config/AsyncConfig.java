package com.leandrocarron.challengejava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // It was defindet in the principal class too
public class AsyncConfig {

    @Bean(name = "csvProcessorExecutor") // excecutor name
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // min threads
        executor.setMaxPoolSize(10); //max threads
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("CSV-Processor-");
        executor.initialize(); // thread pool inicialize
        return executor;
    }
}