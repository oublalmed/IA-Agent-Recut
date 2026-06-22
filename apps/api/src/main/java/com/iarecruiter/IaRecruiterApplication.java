package com.iarecruiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IaRecruiterApplication {
    public static void main(String[] args) {
        SpringApplication.run(IaRecruiterApplication.class, args);
    }
}
