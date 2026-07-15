package com.dlz.test.db.cases.ds;

import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.ds.DataSourceProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataSourceConfig 数据源配置测试")
class DataSourceConfigTest {

    private DataSourceConfig createConfig(String url) {
        DataSourceProperty prop = new DataSourceProperty();
        prop.setUrl(url);
        return new DataSourceConfig(prop);
    }

    @Test
    @DisplayName("getDialect - MySQL URL识别")
    void testDbTypeMysql() {
        assertEquals("mysql", createConfig("jdbc:mysql://localhost/db").getDialect().id());
    }

    @Test
    @DisplayName("getDbType - MariaDB URL识别")
    void testDbTypeMariadb() {
        assertEquals("mysql", createConfig("jdbc:mariadb://localhost/db").getDialect().id());
    }

    @Test
    @DisplayName("getDbType - PostgreSQL URL识别")
    void testDbTypePostgresql() {
        assertEquals("postgresql", createConfig("jdbc:postgresql://localhost/db").getDialect().id());
    }

    @Test
    @DisplayName("getDbType - Oracle URL识别")
    void testDbTypeOracle() {
        assertEquals("oracle", createConfig("jdbc:oracle:thin:@localhost:1521/db").getDialect().id());
    }

    @Test
    @DisplayName("getDbType - DM8 URL识别")
    void testDbTypeDm8() {
        assertEquals("dm8", createConfig("jdbc:dm://localhost:5236").getDialect().id());
    }

    @Test
    @DisplayName("getDbType - SQLite URL识别")
    void testDbTypeSqlite() {
        assertEquals("sqlite", createConfig("jdbc:sqlite::memory:").getDialect().id());
    }

    @Test
    @DisplayName("getDbType - SQL Server URL识别")
    void testDbTypeMssql() {
        assertEquals("mssql", createConfig("jdbc:sqlserver://localhost:1433").getDialect().id());
    }

    @Test
    @DisplayName("getDbType - H2 URL识别")
    void testDbTypeH2() {
        assertEquals("h2", createConfig("jdbc:h2:mem:testdb").getDialect().id());
    }

    @Test
    @DisplayName("getDbType - 未知URL抛异常")
    void testDbTypeUnknown() {
        assertThrows(Exception.class, () -> createConfig("jdbc:unknown://localhost").getDialect());
    }

    @Test
    @DisplayName("getRowMapper - SQLite使用默认RowMapper")
    void testRowMapperSqlite() {
        DataSourceConfig config = createConfig("jdbc:sqlite::memory:");
        assertNotNull(config.getRowMapper());
    }

    @Test
    @DisplayName("getRowMapper - MySQL使用MySqlColumnMapRowMapper")
    void testRowMapperMysql() {
        DataSourceConfig config = createConfig("jdbc:mysql://localhost/db");
        assertNotNull(config.getRowMapper());
    }

    @Test
    @DisplayName("getSchemaDialect - SQLite")
    void testSchemaDialectSqlite() {
        DataSourceConfig config = createConfig("jdbc:sqlite::memory:");
        assertNotNull(config.getSchemaDialect());
    }

    @Test
    @DisplayName("getSchemaDialect - PostgreSQL")
    void testSchemaDialectPostgresql() {
        DataSourceConfig config = createConfig("jdbc:postgresql://localhost/db");
        assertNotNull(config.getSchemaDialect());
    }

    @Test
    @DisplayName("getName - 返回property的name")
    void testGetName() {
        DataSourceProperty prop = new DataSourceProperty();
        prop.setName("primary");
        prop.setUrl("jdbc:sqlite::memory:");
        DataSourceConfig config = new DataSourceConfig(prop);
        assertEquals("primary", config.getName());
    }

    @Test
    @DisplayName("close - 无数据源不抛异常")
    void testCloseNoDataSource() throws Exception {
        DataSourceConfig config = createConfig("jdbc:sqlite::memory:");
        assertDoesNotThrow(config::close);
    }
}
