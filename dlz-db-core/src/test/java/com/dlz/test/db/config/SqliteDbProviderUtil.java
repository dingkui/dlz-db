package com.dlz.test.db.config;

import com.dlz.db.core.DlzDbProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;

/**
 * 测试用数据库提供者 - 基于内存数据存储
 * 用于单元测试，无需真实数据库连接
 */
public class SqliteDbProviderUtil {

    static DataSource createSqliteDataSource() {
        try {
            // 创建测试目录
            File dir = new File("./test");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 配置 HikariCP 连接池
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:./test/testdb-core.sqlite3");
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            return new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SQLite data source", e);
        }
    }

    static DlzDbProperties createTestConfig() {
        DlzDbProperties config = new DlzDbProperties();
        config.setLogicDeleteField("IS_DELETED");
        config.setTableCacheTime(-1);

        config.getLog().setShowRunSql(true);
        config.getLog().setShowCaller(true);

        config.getHelper().setAutoUpdate( true);



        return config;
    }
}
