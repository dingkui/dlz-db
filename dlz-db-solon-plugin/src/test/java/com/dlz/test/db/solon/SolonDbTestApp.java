package com.dlz.test.db.solon;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;

/**
 * Solon 测试应用：注册 SQLite DataSource Bean，让 dlz-db-solon-plugin 自动接管。
 */
@Configuration
public class SolonDbTestApp {

    @Bean
    public DataSource dataSource() {
        // 测试库放到 target 下，避免污染源码目录
        File dir = new File("target/solon-test");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("org.sqlite.JDBC");
        cfg.setJdbcUrl("jdbc:sqlite:" + dir.getAbsolutePath() + "/test.db");
        cfg.setMaximumPoolSize(2);
        cfg.setMinimumIdle(1);
        return new HikariDataSource(cfg);
    }
}
