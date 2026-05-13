package com.dlz.test.db.ds;

import com.dlz.db.convertor.rowMapper.MySqlColumnMapRowMapper;
import com.dlz.db.convertor.rowMapper.OracleColumnMapRowMapper;
import com.dlz.db.convertor.rowMapper.ResultMapRowMapper;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.enums.DbTypeEnum;
import com.dlz.db.helper.support.SqlHelper;
import com.dlz.db.helper.support.dbs.DbOpDm8;
import com.dlz.db.helper.support.dbs.DbOpMysql;
import com.dlz.db.helper.support.dbs.DbOpPostgresql;
import com.dlz.db.helper.support.dbs.DbOpSqlite;
import com.dlz.kit.exception.SystemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSourceConfig 数据源配置测试
 */
@DisplayName("DataSourceConfig 数据源配置测试")
class DataSourceConfigTest {

    @Test
    @DisplayName("测试 MySQL 数据库类型识别")
    void testMySqlDbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:mysql://localhost:3306/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.MYSQL, config.getDbType());
    }

    @Test
    @DisplayName("测试 MariaDB 数据库类型识别")
    void testMariaDbDbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:mariadb://localhost:3306/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.MYSQL, config.getDbType());
    }

    @Test
    @DisplayName("测试 PostgreSQL 数据库类型识别")
    void testPostgreSqlDbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:postgresql://localhost:5432/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.POSTGRESQL, config.getDbType());
    }

    @Test
    @DisplayName("测试 Oracle 数据库类型识别")
    void testOracleDbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:oracle:thin:@localhost:1521:orcl");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.ORACLE, config.getDbType());
    }

    @Test
    @DisplayName("测试 DM8 数据库类型识别")
    void testDm8DbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:dm://localhost:5236");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.DM8, config.getDbType());
    }

    @Test
    @DisplayName("测试 SQLite 数据库类型识别")
    void testSqliteDbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:sqlite:test.db");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.SQLITE, config.getDbType());
    }

    @Test
    @DisplayName("测试 SQL Server 数据库类型识别")
    void testSqlServerDbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:sqlserver://localhost:1433");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.MSSQL, config.getDbType());
    }

    @Test
    @DisplayName("测试 H2 数据库类型识别")
    void testH2DbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:h2:mem:testdb");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.H2, config.getDbType());
    }

    @Test
    @DisplayName("测试未识别的数据库类型抛出异常")
    void testUnknownDbType() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:unknown://localhost/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertThrows(SystemException.class, () -> config.getDbType());
    }

    @Test
    @DisplayName("测试 MySQL 的 RowMapper")
    void testMySqlRowMapper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:mysql://localhost:3306/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getRowMapper() instanceof MySqlColumnMapRowMapper);
    }

    @Test
    @DisplayName("测试 PostgreSQL 的 RowMapper")
    void testPostgreSqlRowMapper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:postgresql://localhost:5432/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getRowMapper() instanceof MySqlColumnMapRowMapper);
    }

    @Test
    @DisplayName("测试 Oracle 的 RowMapper")
    void testOracleRowMapper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:oracle:thin:@localhost:1521:orcl");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getRowMapper() instanceof OracleColumnMapRowMapper);
    }

    @Test
    @DisplayName("测试 DM8 的 RowMapper")
    void testDm8RowMapper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:dm://localhost:5236");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getRowMapper() instanceof OracleColumnMapRowMapper);
    }

    @Test
    @DisplayName("测试 SQLite 的 RowMapper")
    void testSqliteRowMapper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:sqlite:test.db");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getRowMapper() instanceof ResultMapRowMapper);
    }

    @Test
    @DisplayName("测试 RowMapper 缓存（多次调用返回同一实例）")
    void testRowMapperCache() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:mysql://localhost:3306/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        Object mapper1 = config.getRowMapper();
        Object mapper2 = config.getRowMapper();
        
        assertSame(mapper1, mapper2);
    }

    @Test
    @DisplayName("测试 MySQL 的 SqlHelper")
    void testMySqlHelper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:mysql://localhost:3306/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getSqlHelper() instanceof DbOpMysql);
    }

    @Test
    @DisplayName("测试 PostgreSQL 的 SqlHelper")
    void testPostgreSqlHelper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:postgresql://localhost:5432/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getSqlHelper() instanceof DbOpPostgresql);
    }

    @Test
    @DisplayName("测试 SQLite 的 SqlHelper")
    void testSqliteHelper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:sqlite:test.db");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getSqlHelper() instanceof DbOpSqlite);
    }

    @Test
    @DisplayName("测试 DM8 的 SqlHelper")
    void testDm8Helper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:dm://localhost:5236");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getSqlHelper() instanceof DbOpDm8);
    }

    @Test
    @DisplayName("测试 Oracle 的 SqlHelper（使用 DbOpDm8）")
    void testOracleHelper() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:oracle:thin:@localhost:1521:orcl");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertTrue(config.getSqlHelper() instanceof DbOpDm8);
    }

    @Test
    @DisplayName("测试 SqlHelper 缓存（多次调用返回同一实例）")
    void testSqlHelperCache() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:mysql://localhost:3306/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        Object helper1 = config.getSqlHelper();
        Object helper2 = config.getSqlHelper();
        
        assertSame(helper1, helper2);
    }

    @Test
    @DisplayName("测试 close 方法")
    void testClose() throws Exception {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("jdbc:mysql://localhost:3306/test");
        DataSourceConfig config = new DataSourceConfig(property);
        
        // 先获取 rowMapper 和 helper 以初始化它们
        config.getRowMapper();
        config.getSqlHelper();
        
        // 关闭配置
        config.close();
        
        // 验证可以再次获取（会重新创建）
        assertNotNull(config.getRowMapper());
        assertNotNull(config.getSqlHelper());
    }

    @Test
    @DisplayName("测试 URL 大小写不敏感")
    void testUrlCaseInsensitive() {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl("JDBC:MYSQL://LOCALHOST:3306/TEST");
        DataSourceConfig config = new DataSourceConfig(property);
        
        assertEquals(DbTypeEnum.MYSQL, config.getDbType());
    }
}
