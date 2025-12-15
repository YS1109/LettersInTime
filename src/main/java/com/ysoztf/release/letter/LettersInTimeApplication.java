package com.ysoztf.release.letter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LettersInTimeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LettersInTimeApplication.class, args);
    }
}


