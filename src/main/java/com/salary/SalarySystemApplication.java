package com.salary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SalarySystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalarySystemApplication.class, args);
    }
}
