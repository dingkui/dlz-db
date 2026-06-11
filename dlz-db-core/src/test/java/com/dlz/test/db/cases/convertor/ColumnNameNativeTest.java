package com.dlz.test.db.cases.convertor;

import com.dlz.db.convertor.columnname.ColumnNameNative;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ColumnNameNative 原生列名转换测试")
class ColumnNameNativeTest {

    private final ColumnNameNative convertor = new ColumnNameNative();

    @Test
    @DisplayName("toFieldName - 原样返回")
    void testToFieldName() {
        assertEquals("USER_NAME", convertor.toFieldName("USER_NAME"));
        assertEquals("id", convertor.toFieldName("id"));
        assertEquals("", convertor.toFieldName(""));
    }

    @Test
    @DisplayName("toDbColumnName - 原样返回")
    void testToDbColumnName() {
        assertEquals("userName", convertor.toDbColumnName("userName"));
        assertEquals("ID", convertor.toDbColumnName("ID"));
        assertEquals("", convertor.toDbColumnName(""));
    }

    @Test
    @DisplayName("实现IColumnNameConvertor接口")
    void testImplementsInterface() {
        assertTrue(convertor instanceof com.dlz.db.convertor.columnname.IColumnNameConvertor);
    }
}
