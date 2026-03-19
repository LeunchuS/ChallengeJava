package com.leandrocarron.challengejava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChallenjeJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChallenjeJavaApplication.class, args);
    }

}
