package com.WinkProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WinkProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(WinkProjectApplication.class, args);
    }

}
