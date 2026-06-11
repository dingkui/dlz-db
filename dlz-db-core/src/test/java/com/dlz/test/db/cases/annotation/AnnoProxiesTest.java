package com.dlz.test.db.cases.annotation;

import com.dlz.db.annotation.proxy.AnnoProxies;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnnoProxies 注解代理测试")
class AnnoProxiesTest {

    @Test
    @DisplayName("MybatisPlusIdType 实例不为null")
    void testMybatisPlusIdTypeNotNull() {
        assertNotNull(AnnoProxies.MybatisPlusIdType);
    }

    @Test
    @DisplayName("MybatisPlusTableField 实例不为null")
    void testMybatisPlusTableFieldNotNull() {
        assertNotNull(AnnoProxies.MybatisPlusTableField);
    }

    @Test
    @DisplayName("MybatisPlusTableName 实例不为null")
    void testMybatisPlusTableNameNotNull() {
        assertNotNull(AnnoProxies.MybatisPlusTableName);
    }

    @Test
    @DisplayName("MybatisPlusTableName.value - 无注解返回null")
    void testMybatisPlusTableNameValueNoAnnotation() {
        assertNull(AnnoProxies.MybatisPlusTableName.value(String.class));
    }

    @Test
    @DisplayName("MybatisPlusTableName.value - null参数返回null")
    void testMybatisPlusTableNameValueNull() {
        assertNull(AnnoProxies.MybatisPlusTableName.value(null));
    }

    @Test
    @DisplayName("MybatisPlusIdType.value - null Field返回null")
    void testMybatisPlusIdTypeValueNull() {
        assertNull(AnnoProxies.MybatisPlusIdType.value((Field) null));
    }

    @Test
    @DisplayName("MybatisPlusIdType.type - null Field返回null")
    void testMybatisPlusIdTypeTypeNull() {
        assertNull(AnnoProxies.MybatisPlusIdType.type(null));
    }

    @Test
    @DisplayName("MybatisPlusIdType.value - 普通Field无MP注解返回null")
    void testMybatisPlusIdTypeValueNoAnnotation() throws Exception {
        Field field = SampleClass.class.getDeclaredField("name");
        assertNull(AnnoProxies.MybatisPlusIdType.value(field));
    }

    @Test
    @DisplayName("MybatisPlusTableField.value - null Field返回null")
    void testMybatisPlusTableFieldValueNull() {
        assertNull(AnnoProxies.MybatisPlusTableField.value(null));
    }

    @Test
    @DisplayName("MybatisPlusTableField.exist - null Field返回TRUE")
    void testMybatisPlusTableFieldExistNull() {
        assertEquals(Boolean.TRUE, AnnoProxies.MybatisPlusTableField.exist(null));
    }

    @Test
    @DisplayName("MybatisPlusTableField.exist - 普通Field无MP注解返回TRUE")
    void testMybatisPlusTableFieldExistNoAnnotation() throws Exception {
        Field field = SampleClass.class.getDeclaredField("name");
        assertEquals(Boolean.TRUE, AnnoProxies.MybatisPlusTableField.exist(field));
    }

    static class SampleClass {
        String name;
        int age;
    }
}
