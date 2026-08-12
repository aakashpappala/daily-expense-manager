package com.expensetrack;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAsync
public class ExpenseTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackApplication.class, args);
    }
}
