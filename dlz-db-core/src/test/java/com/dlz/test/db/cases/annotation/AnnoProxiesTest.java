package com.dlz.test.db.cases.annotation;

import com.dlz.db.annotation.TableField;
import com.dlz.db.annotation.TableName;
import com.dlz.db.annotation.proxy.AnnoProxies;
import com.dlz.db.annotation.proxy.TableFieldMeta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
    @DisplayName("SwaggerApiModel 实例不为null")
    void testSwaggerApiModelNotNull() {
        assertNotNull(AnnoProxies.SwaggerApiModel);
    }

    @Test
    @DisplayName("SwaggerModelProp 实例不为null")
    void testSwaggerModelPropNotNull() {
        assertNotNull(AnnoProxies.SwaggerModelProp);
    }

    @Test
    @DisplayName("TableFieldMeta.comment - 直接标注 @TableField 读取 comment")
    void testTableFieldMetaCommentDirect() throws Exception {
        Field field = SampleClass.class.getDeclaredField("directComment");
        assertEquals("直接中文注释", TableFieldMeta.comment(field));
        assertEquals("user_name", TableFieldMeta.value(field));
        assertEquals(Boolean.FALSE, TableFieldMeta.exist(field));
    }

    @Test
    @DisplayName("TableFieldMeta.comment - 自定义元注解扩展 @TableField 读取 comment")
    void testTableFieldMetaCommentMetaAnnotation() throws Exception {
        Field field = SampleClass.class.getDeclaredField("customComment");
        assertEquals("自定义中文注释", TableFieldMeta.comment(field));
        assertEquals("custom_col", TableFieldMeta.value(field));
        assertEquals(Boolean.TRUE, TableFieldMeta.exist(field));
    }

    @Test
    @DisplayName("TableFieldMeta.comment - 无注解字段返回 null")
    void testTableFieldMetaCommentNull() throws Exception {
        Field field = SampleClass.class.getDeclaredField("name");
        assertNull(TableFieldMeta.comment(field));
        assertNull(TableFieldMeta.value(field));
        assertNull(TableFieldMeta.exist(field));
    }

    @Test
    @DisplayName("TableFieldMeta.comment(Class) - 直接标注 @TableName 读取 comment")
    void testTableFieldMetaClassCommentDirect() {
        assertEquals("直接表注释", TableFieldMeta.comment(TableEntity.class));
    }

    @Test
    @DisplayName("TableFieldMeta.comment(Class) - 自定义元注解扩展 @TableName 读取 comment")
    void testTableFieldMetaClassCommentMetaAnnotation() {
        assertEquals("自定义表注释", TableFieldMeta.comment(CustomTableEntity.class));
    }

    @Test
    @DisplayName("TableFieldMeta.comment(Class) - 无注解类返回 null")
    void testTableFieldMetaClassCommentNull() {
        assertNull(TableFieldMeta.comment(String.class));
    }

    @Test
    @DisplayName("TableFieldMeta.value - null Field返回 null")
    void testTableFieldMetaValueNullField() {
        assertNull(TableFieldMeta.value(null));
        assertNull(TableFieldMeta.exist(null));
        assertNull(TableFieldMeta.comment((Field) null));
    }

    @Test
    @DisplayName("TableFieldMeta.comment - null Class返回 null")
    void testTableFieldMetaClassCommentNullClass() {
        assertNull(TableFieldMeta.comment((Class<?>) null));
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

    /**
     * 自定义注解：通过 {@code @TableField} 元注解扩展，提供中文注释。
     */
    @TableField
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface CustomField {
        String value() default "";
        boolean exist() default true;
        boolean select() default true;
        String comment() default "";
    }

    /**
     * 自定义注解：通过 {@code @TableName} 元注解扩展，提供表中文注释。
     */
    @TableName
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface CustomTable {
        String value() default "";
        String comment() default "";
    }

    static class SampleClass {
        String name;
        int age;

        @TableField(value = "user_name", exist = false, comment = "直接中文注释")
        String directComment;

        @CustomField(value = "custom_col", exist = true, comment = "自定义中文注释")
        String customComment;
    }

    @TableName(value = "t_direct", comment = "直接表注释")
    static class TableEntity {
    }

    @CustomTable(value = "t_custom", comment = "自定义表注释")
    static class CustomTableEntity {
    }
}
