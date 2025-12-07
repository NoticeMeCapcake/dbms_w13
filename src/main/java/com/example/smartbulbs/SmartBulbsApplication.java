package com.example.smartbulbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartBulbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBulbsApplication.class, args);
    }
}