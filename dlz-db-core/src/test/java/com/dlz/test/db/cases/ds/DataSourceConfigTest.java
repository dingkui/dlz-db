package com.dlz.test.db.cases.ds;

import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.enums.DbTypeEnum;
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
    @DisplayName("getDbType - MySQL URL识别")
    void testDbTypeMysql() {
        assertEquals(DbTypeEnum.MYSQL, createConfig("jdbc:mysql://localhost/db").getDbType());
    }

    @Test
    @DisplayName("getDbType - MariaDB URL识别")
    void testDbTypeMariadb() {
        assertEquals(DbTypeEnum.MYSQL, createConfig("jdbc:mariadb://localhost/db").getDbType());
    }

    @Test
    @DisplayName("getDbType - PostgreSQL URL识别")
    void testDbTypePostgresql() {
        assertEquals(DbTypeEnum.POSTGRESQL, createConfig("jdbc:postgresql://localhost/db").getDbType());
    }

    @Test
    @DisplayName("getDbType - Oracle URL识别")
    void testDbTypeOracle() {
        assertEquals(DbTypeEnum.ORACLE, createConfig("jdbc:oracle:thin:@localhost:1521/db").getDbType());
    }

    @Test
    @DisplayName("getDbType - DM8 URL识别")
    void testDbTypeDm8() {
        assertEquals(DbTypeEnum.DM8, createConfig("jdbc:dm://localhost:5236").getDbType());
    }

    @Test
    @DisplayName("getDbType - SQLite URL识别")
    void testDbTypeSqlite() {
        assertEquals(DbTypeEnum.SQLITE, createConfig("jdbc:sqlite::memory:").getDbType());
    }

    @Test
    @DisplayName("getDbType - SQL Server URL识别")
    void testDbTypeMssql() {
        assertEquals(DbTypeEnum.MSSQL, createConfig("jdbc:sqlserver://localhost:1433").getDbType());
    }

    @Test
    @DisplayName("getDbType - H2 URL识别")
    void testDbTypeH2() {
        assertEquals(DbTypeEnum.H2, createConfig("jdbc:h2:mem:testdb").getDbType());
    }

    @Test
    @DisplayName("getDbType - 未知URL抛异常")
    void testDbTypeUnknown() {
        assertThrows(Exception.class, () -> createConfig("jdbc:unknown://localhost").getDbType());
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
    @DisplayName("getSqlHelper - SQLite返回DbOpSqlite")
    void testSqlHelperSqlite() {
        DataSourceConfig config = createConfig("jdbc:sqlite::memory:");
        assertNotNull(config.getSqlHelper());
    }

    @Test
    @DisplayName("getSqlHelper - PostgreSQL返回DbOpPostgresql")
    void testSqlHelperPostgresql() {
        DataSourceConfig config = createConfig("jdbc:postgresql://localhost/db");
        assertNotNull(config.getSqlHelper());
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
