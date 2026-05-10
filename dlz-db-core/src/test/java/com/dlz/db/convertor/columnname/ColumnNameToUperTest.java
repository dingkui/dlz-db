package com.dlz.db.convertor.columnname;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnNameToUper 测试类
 * 
 * @author test
 */
@DisplayName("列名转大写转换器测试")
class ColumnNameToUperTest {

    private ColumnNameToUper converter;

    @BeforeEach
    void setUp() {
        converter = new ColumnNameToUper();
    }

    @Test
    @DisplayName("toFieldName - 转大写")
    void testToFieldName() {
        assertEquals("USERNAME", converter.toFieldName("username"));
        assertEquals("USER_NAME", converter.toFieldName("user_name"));
        assertEquals("USERNAME", converter.toFieldName("userName"));
    }

    @Test
    @DisplayName("toDbColumnName - 不转换")
    void testToDbColumnName() {
        assertEquals("userName", converter.toDbColumnName("userName"));
        assertEquals("USER_NAME", converter.toDbColumnName("USER_NAME"));
        assertEquals("user_name", converter.toDbColumnName("user_name"));
    }

    @Test
    @DisplayName("toDbColumnName - null 值")
    void testToDbColumnName_Null() {
        assertNull(converter.toDbColumnName(null));
    }

    @Test
    @DisplayName("toFieldName - 空字符串")
    void testToFieldName_Empty() {
        assertEquals("", converter.toFieldName(""));
    }

    @Test
    @DisplayName("toDbColumnName - 空字符串")
    void testToDbColumnName_Empty() {
        assertEquals("", converter.toDbColumnName(""));
    }
}
