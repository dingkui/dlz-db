package com.example.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源配置：注册 DataSource 后，dlz-db-solon-plugin 通过 SPI 自动接管。
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("org.sqlite.JDBC");
        cfg.setJdbcUrl("jdbc:sqlite:./demo.sqlite3");
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(1);
        return new HikariDataSource(cfg);
    }
}
