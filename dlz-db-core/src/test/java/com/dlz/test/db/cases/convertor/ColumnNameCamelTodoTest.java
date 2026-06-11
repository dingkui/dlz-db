package com.dlz.test.db.cases.convertor;

import com.dlz.db.convertor.columnname.ColumnNameCamelTodo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ColumnNameCamelTodo 列名转换测试")
class ColumnNameCamelTodoTest {

    private final ColumnNameCamelTodo convertor = new ColumnNameCamelTodo();

    // ===== toFieldName tests =====

    @Test
    @DisplayName("toFieldName - null返回空字符串")
    void testToFieldNameNull() {
        assertEquals("", convertor.toFieldName(null));
    }

    @Test
    @DisplayName("toFieldName - 空字符串")
    void testToFieldNameEmpty() {
        assertEquals("", convertor.toFieldName(""));
    }

    @Test
    @DisplayName("toFieldName - 已是驼峰无需转换")
    void testToFieldNameAlreadyCamel() {
        assertEquals("username", convertor.toFieldName("username"));
    }

    @Test
    @DisplayName("toFieldName - 下划线转驼峰")
    void testToFieldNameUnderscoreToCamel() {
        assertEquals("userName", convertor.toFieldName("user_name"));
    }

    @Test
    @DisplayName("toFieldName - 多下划线")
    void testToFieldNameMultiUnderscores() {
        assertEquals("createTimeStamp", convertor.toFieldName("create_time_stamp"));
    }

    @Test
    @DisplayName("toFieldName - 大写转小写")
    void testToFieldNameUpperToLower() {
        assertEquals("username", convertor.toFieldName("USERNAME"));
    }

    @Test
    @DisplayName("toFieldName - 大写下划线转驼峰")
    void testToFieldNameUpperUnderscoreToCamel() {
        assertEquals("userName", convertor.toFieldName("USER_NAME"));
    }

    @Test
    @DisplayName("toFieldName - 缓存命中")
    void testToFieldNameCache() {
        String first = convertor.toFieldName("CACHED_KEY");
        String second = convertor.toFieldName("CACHED_KEY");
        assertEquals(first, second);
    }

    // ===== toDbColumnName tests =====

    @Test
    @DisplayName("toDbColumnName - null返回null")
    void testToDbColumnNameNull() {
        assertNull(convertor.toDbColumnName(null));
    }

    @Test
    @DisplayName("toDbColumnName - 空字符串")
    void testToDbColumnNameEmpty() {
        assertEquals("", convertor.toDbColumnName(""));
    }

    @Test
    @DisplayName("toDbColumnName - 驼峰转下划线大写")
    void testToDbColumnNameCamelToUnderscore() {
        assertEquals("USER_NAME", convertor.toDbColumnName("userName"));
    }

    @Test
    @DisplayName("toDbColumnName - 已含下划线不转换")
    void testToDbColumnNameWithUnderscore() {
        assertEquals("user_name", convertor.toDbColumnName("user_name"));
    }

    @Test
    @DisplayName("toDbColumnName - 全大写无小写不转换")
    void testToDbColumnNameAllUpper() {
        assertEquals("USERNAME", convertor.toDbColumnName("USERNAME"));
    }

    @Test
    @DisplayName("toDbColumnName - 单个字段")
    void testToDbColumnNameSingle() {
        assertEquals("ID", convertor.toDbColumnName("id"));
    }

    @Test
    @DisplayName("toDbColumnName - 多驼峰")
    void testToDbColumnNameMultiCamel() {
        assertEquals("CREATE_TIME_STAMP", convertor.toDbColumnName("createTimeStamp"));
    }

    @Test
    @DisplayName("toDbColumnName - 含特殊字符被清洗")
    void testToDbColumnNameSpecialChars() {
        String result = convertor.toDbColumnName("user@Name#Test");
        assertFalse(result.contains("@"));
        assertFalse(result.contains("#"));
    }

    @Test
    @DisplayName("toDbColumnName - 保留点号和美元符号")
    void testToDbColumnNameKeepDotDollar() {
        String result = convertor.toDbColumnName("schema.table$col");
        assertTrue(result.contains("."));
        assertTrue(result.contains("$"));
    }

    @Test
    @DisplayName("toDbColumnName - 缓存命中")
    void testToDbColumnNameCache() {
        String first = convertor.toDbColumnName("cachedField");
        String second = convertor.toDbColumnName("cachedField");
        assertEquals(first, second);
    }
}
