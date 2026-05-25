package com.dlz.test.db;

import com.dlz.test.db.config.DlzDbConfigs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@ConfigurationPropertiesScan
@SpringBootApplication()
@Import({DlzDbConfigs.class})
public class Starter{
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
}