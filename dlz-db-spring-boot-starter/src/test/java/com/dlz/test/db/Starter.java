package com.dlz.test.db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication()
public class Starter{
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
}