package com.dlz.test.db.cases.enums;

import com.dlz.db.enums.DbTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DbTypeEnum 数据库类型枚举测试")
class DbTypeEnumTest {

    @Test
    @DisplayName("所有枚举值都有正确的end后缀")
    void testAllEndValues() {
        assertEquals("_mysql", DbTypeEnum.MYSQL.getEnd());
        assertEquals("_h2", DbTypeEnum.H2.getEnd());
        assertEquals("_postgresql", DbTypeEnum.POSTGRESQL.getEnd());
        assertEquals("_oracle", DbTypeEnum.ORACLE.getEnd());
        assertEquals("_dm8", DbTypeEnum.DM8.getEnd());
        assertEquals("_sqlite", DbTypeEnum.SQLITE.getEnd());
        assertEquals("_sqlserver", DbTypeEnum.MSSQL.getEnd());
    }

    @Test
    @DisplayName("枚举数量为7")
    void testEnumCount() {
        assertEquals(7, DbTypeEnum.values().length);
    }

    @Test
    @DisplayName("valueOf 正确解析")
    void testValueOf() {
        assertEquals(DbTypeEnum.MYSQL, DbTypeEnum.valueOf("MYSQL"));
        assertEquals(DbTypeEnum.H2, DbTypeEnum.valueOf("H2"));
        assertEquals(DbTypeEnum.MSSQL, DbTypeEnum.valueOf("MSSQL"));
    }

    @Test
    @DisplayName("end后缀以下划线开头")
    void testEndStartsWithUnderscore() {
        for (DbTypeEnum type : DbTypeEnum.values()) {
            assertTrue(type.getEnd().startsWith("_"),
                    type.name() + " end should start with _");
        }
    }
}
