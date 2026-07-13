package com.dlz.test.db.cases.convertor;

import com.dlz.db.mapper.name.NameConvertNative;
import com.dlz.db.mapper.name.INameConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ColumnNameNative 原生列名转换测试")
class ColumnNameNativeTest {

    private final NameConvertNative convertor = new NameConvertNative();

    @Test
    @DisplayName("toFieldName - 原样返回")
    void testToFieldName() {
        assertEquals("USER_NAME", convertor.toFieldName("USER_NAME"));
        assertEquals("id", convertor.toFieldName("id"));
        assertEquals("", convertor.toFieldName(""));
    }

    @Test
    @DisplayName("toDbColumnName - 原样返回")
    void testToDbName() {
        assertEquals("userName", convertor.toDbName("userName"));
        assertEquals("id", convertor.toDbName("id"));
        assertEquals("", convertor.toDbName(""));
    }

    @Test
    @DisplayName("实现IColumnNameConvertor接口")
    void testImplementsInterface() {
        assertTrue(convertor instanceof INameConverter);
    }
}
