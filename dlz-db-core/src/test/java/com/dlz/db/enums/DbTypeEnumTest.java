package com.dlz.db.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DbTypeEnum 测试类
 * 
 * @author test
 */
@DisplayName("数据库类型枚举测试")
class DbTypeEnumTest {

    @Test
    @DisplayName("测试获取数据库后缀 - MySQL")
    void testGetEnd_MySQL() {
        assertEquals("_mysql", DbTypeEnum.MYSQL.getEnd());
    }

    @Test
    @DisplayName("测试获取数据库后缀 - H2")
    void testGetEnd_H2() {
        assertEquals("_h2", DbTypeEnum.H2.getEnd());
    }

    @Test
    @DisplayName("测试获取数据库后缀 - PostgreSQL")
    void testGetEnd_PostgreSQL() {
        assertEquals("_postgresql", DbTypeEnum.POSTGRESQL.getEnd());
    }

    @Test
    @DisplayName("测试获取数据库后缀 - Oracle")
    void testGetEnd_Oracle() {
        assertEquals("_oracle", DbTypeEnum.ORACLE.getEnd());
    }

    @Test
    @DisplayName("测试获取数据库后缀 - DM8")
    void testGetEnd_DM8() {
        assertEquals("_dm8", DbTypeEnum.DM8.getEnd());
    }

    @Test
    @DisplayName("测试获取数据库后缀 - SQLite")
    void testGetEnd_SQLite() {
        assertEquals("_sqlite", DbTypeEnum.SQLITE.getEnd());
    }

    @Test
    @DisplayName("测试获取数据库后缀 - SQL Server")
    void testGetEnd_SQLServer() {
        assertEquals("_sqlserver", DbTypeEnum.MSSQL.getEnd());
    }

    @Test
    @DisplayName("测试所有枚举值")
    void testAllEnumValues() {
        assertEquals(7, DbTypeEnum.values().length);
        
        // 验证所有枚举值
        assertAll("DbTypeEnum values",
            () -> assertEquals("MYSQL", DbTypeEnum.MYSQL.name()),
            () -> assertEquals("H2", DbTypeEnum.H2.name()),
            () -> assertEquals("POSTGRESQL", DbTypeEnum.POSTGRESQL.name()),
            () -> assertEquals("ORACLE", DbTypeEnum.ORACLE.name()),
            () -> assertEquals("DM8", DbTypeEnum.DM8.name()),
            () -> assertEquals("SQLITE", DbTypeEnum.SQLITE.name()),
            () -> assertEquals("MSSQL", DbTypeEnum.MSSQL.name())
        );
    }
}
