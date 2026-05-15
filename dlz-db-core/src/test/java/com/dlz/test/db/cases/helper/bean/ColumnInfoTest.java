package com.dlz.test.db.cases.helper.bean;

import com.dlz.db.support.bean.ColumnInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * ColumnInfo 测试类
 */
@DisplayName("ColumnInfo 测试")
class ColumnInfoTest {

    @Test
    @DisplayName("测试 ColumnInfo 基本属性设置和获取")
    void testBasicProperties() {
        ColumnInfo columnInfo = new ColumnInfo();
        
        columnInfo.setColumnName("user_name");
        columnInfo.setColumnType("varchar(255)");
        columnInfo.setColumnComment("用户名");
        columnInfo.setJavaType(String.class);
        
        assertEquals("user_name", columnInfo.getColumnName());
        assertEquals("varchar(255)", columnInfo.getColumnType());
        assertEquals("用户名", columnInfo.getColumnComment());
        assertEquals(String.class, columnInfo.getJavaType());
    }

    @Test
    @DisplayName("测试 ColumnInfo 不同 Java 类型")
    void testDifferentJavaTypes() {
        ColumnInfo columnInfo = new ColumnInfo();
        
        columnInfo.setJavaType(Integer.class);
        assertEquals(Integer.class, columnInfo.getJavaType());
        
        columnInfo.setJavaType(Long.class);
        assertEquals(Long.class, columnInfo.getJavaType());
        
        columnInfo.setJavaType(Boolean.class);
        assertEquals(Boolean.class, columnInfo.getJavaType());
        
        columnInfo.setJavaType(Date.class);
        assertEquals(Date.class, columnInfo.getJavaType());
    }

    @Test
    @DisplayName("测试 ColumnInfo null 值处理")
    void testNullValues() {
        ColumnInfo columnInfo = new ColumnInfo();
        
        assertNull(columnInfo.getColumnName());
        assertNull(columnInfo.getColumnType());
        assertNull(columnInfo.getColumnComment());
        assertNull(columnInfo.getJavaType());
    }

    @Test
    @DisplayName("测试 ColumnInfo 空字符串")
    void testEmptyStrings() {
        ColumnInfo columnInfo = new ColumnInfo();
        
        columnInfo.setColumnName("");
        columnInfo.setColumnType("");
        columnInfo.setColumnComment("");
        
        assertEquals("", columnInfo.getColumnName());
        assertEquals("", columnInfo.getColumnType());
        assertEquals("", columnInfo.getColumnComment());
    }
}
