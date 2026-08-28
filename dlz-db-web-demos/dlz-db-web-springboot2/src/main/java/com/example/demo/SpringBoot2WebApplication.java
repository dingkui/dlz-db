package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DLZ-DB Spring Boot 2 示例启动类。
 *
 * <p>DLZ-DB 通过 starter 自动装配（META-INF/spring.factories）完成初始化，
 * 无需手动导入配置类。启动后自动扫描实体类并建表（auto-update: true）。</p>
 */
@SpringBootApplication
public class SpringBoot2WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot2WebApplication.class, args);
    }
}
