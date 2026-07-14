package com.dlz.test.db.cases.convertor.columnname;

import com.dlz.db.mapper.name.NameConvertToUper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ColumnNameToUper 测试类
 * 
 * @author test
 */
@DisplayName("列名转大写转换器测试")
class ColumnNameToUperTest {

    private NameConvertToUper converter;

    @BeforeEach
    void setUp() {
        converter = new NameConvertToUper();
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
    void testToDbName() {
        assertEquals("username", converter.toDbName("userName"));
        assertEquals("user_name", converter.toDbName("USER_NAME"));
        assertEquals("user_name", converter.toDbName("user_name"));
    }

    @Test
    @DisplayName("toFieldName - 空字符串")
    void testToFieldName_Empty() {
        assertEquals("", converter.toFieldName(""));
    }

    @Test
    @DisplayName("toDbColumnName - 空字符串")
    void testToDbName_Empty() {
        assertEquals("", converter.toDbName(""));
    }
}
