package com.dlz.test.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;

/**
 * Solon 测试应用入口（与 Spring 测试 {@code Starter} 同 FQN）。
 *
 * <p>注册测试用 SQLite + Hikari DataSource，dlz-db-solon-plugin 通过 SPI 自动接管。</p>
 */
@Configuration
public class Starter {

    public static void main(String[] args) {
        Solon.start(Starter.class, args);
    }

    @Bean
    public DataSource dataSource() {
        File dir = new File("./test");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("org.sqlite.JDBC");
        cfg.setJdbcUrl("jdbc:sqlite:./test/testdb.sqlite3");
        cfg.setMaximumPoolSize(2);
        cfg.setMinimumIdle(1);
        return new HikariDataSource(cfg);
    }
}
