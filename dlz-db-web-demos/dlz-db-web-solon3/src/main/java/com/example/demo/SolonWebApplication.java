package com.example.demo;

import org.noear.solon.Solon;

/**
 * DLZ-DB Solon 示例启动类。
 *
 * <p>DLZ-DB 通过 Solon SPI 插件（META-INF/solon/dlz-db-solon-plugin.properties）自动加载，
 * 无需手动注册。启动后自动扫描实体类并建表（auto-update: true）。</p>
 */
public class SolonWebApplication {

    public static void main(String[] args) {
        Solon.start(SolonWebApplication.class, args);
        System.out.println("========================================");
        System.out.println("  DLZ-DB Solon Demo 启动成功！");
        System.out.println("  接口地址: http://localhost:8080/user");
        System.out.println("========================================");
    }
}
