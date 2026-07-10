package com.dlz.test.db;

import com.dlz.test.config.DlzDbConfigs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@ConfigurationPropertiesScan
@SpringBootApplication()
@Import({DlzDbConfigs.class})
public class Starter{
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
}