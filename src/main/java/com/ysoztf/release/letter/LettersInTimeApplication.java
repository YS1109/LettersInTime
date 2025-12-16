package com.ysoztf.release.letter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.ysoztf.release.letter.mapper")
public class LettersInTimeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LettersInTimeApplication.class, args);
    }
}


