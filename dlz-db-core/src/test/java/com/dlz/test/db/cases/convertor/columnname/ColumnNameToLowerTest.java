package com.dlz.test.db.cases.convertor.columnname;

import com.dlz.db.mapper.name.NameConvertToLower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnNameToLower 测试类
 * 
 * @author test
 */
@DisplayName("列名转小写转换器测试")
class ColumnNameToLowerTest {

    private NameConvertToLower converter;

    @BeforeEach
    void setUp() {
        converter = new NameConvertToLower();
    }

    @Test
    @DisplayName("toFieldName - 转小写")
    void testToFieldName() {
        assertEquals("username", converter.toFieldName("USERNAME"));
        assertEquals("user_name", converter.toFieldName("USER_NAME"));
        assertEquals("username", converter.toFieldName("userName"));
    }

    @Test
    @DisplayName("toDbColumnName - 不转换")
    void testToDbName() {
        assertEquals("username", converter.toDbName("userName"));
        assertEquals("user_name", converter.toDbName("USER_NAME"));
        assertEquals("user_name", converter.toDbName("user_name"));
    }


    @Test
    @DisplayName("toDbColumnName - null 值")
    void testToDbName_Null() {
        assertThrows(NullPointerException.class, () -> converter.toDbName(null));
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
