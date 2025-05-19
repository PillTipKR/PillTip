package com.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.oauth2")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
