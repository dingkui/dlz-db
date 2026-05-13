package com.dlz.test.db.helper.support.dbs;

import com.dlz.db.helper.support.dbs.DbOpDm8;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DbOpDm8 类型转换测试
 * 测试达梦数据库（DM8）的字段类型映射
 */
@DisplayName("DbOpDm8 类型转换测试")
class DbOpDm8Test {

    private DbOpDm8 dbOpDm8;

    @BeforeEach
    void setUp() {
        dbOpDm8 = new DbOpDm8();
    }

    @Test
    @DisplayName("测试 String 类型映射到 varchar(255)")
    void testStringType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("name");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("varchar(255)", dbType);
    }

    @Test
    @DisplayName("测试 Integer 类型映射到 NUMBER(10)")
    void testIntegerType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("age");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("NUMBER(10)", dbType);
    }

    @Test
    @DisplayName("测试 int 基本类型映射到 NUMBER(10)")
    void testIntPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("count");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("NUMBER(10)", dbType);
    }

    @Test
    @DisplayName("测试 Boolean 类型映射到 NUMBER(1)")
    void testBooleanType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("active");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("NUMBER(1)", dbType);
    }

    @Test
    @DisplayName("测试 boolean 基本类型映射到 NUMBER(1)")
    void testBooleanPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("enabled");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("NUMBER(1)", dbType);
    }

    @Test
    @DisplayName("测试 Long 类型映射到 NUMBER(19)")
    void testLongType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("id");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("NUMBER(19)", dbType);
    }

    @Test
    @DisplayName("测试 long 基本类型映射到 NUMBER(19)")
    void testLongPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("timestamp");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("NUMBER(19)", dbType);
    }

    @Test
    @DisplayName("测试 Double 类型映射到 NUMBER(12, 2)")
    void testDoubleType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("score");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("NUMBER(12, 2)", dbType);
    }

    @Test
    @DisplayName("测试 Float 类型映射到 NUMBER(12, 2)")
    void testFloatType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("rating");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("NUMBER(12, 2)", dbType);
    }

    @Test
    @DisplayName("测试 Date 类型映射到 TIMESTAMP")
    void testDateType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("createTime");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("TIMESTAMP", dbType);
    }

    @Test
    @DisplayName("测试 LocalDateTime 类型映射到 TIMESTAMP")
    void testLocalDateTimeType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("updateTime");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("TIMESTAMP", dbType);
    }

    @Test
    @DisplayName("测试 LocalDate 类型映射到 TIMESTAMP")
    void testLocalDateType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("birthDate");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("TIMESTAMP", dbType);
    }

    @Test
    @DisplayName("测试 Object 类型映射到 CLOB")
    void testObjectType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("data");
        String dbType = dbOpDm8.getDbColumnType(field);
        
        assertEquals("CLOB", dbType);
    }

    @Test
    @DisplayName("测试 char 转 Java 类型")
    void testCharToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("char");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 varchar 转 Java 类型")
    void testVarcharToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("varchar");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 VARCHAR2 转 Java 类型")
    void testVarchar2ToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("VARCHAR2");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 clob 转 Java 类型")
    void testClobToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("clob");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 text 转 Java 类型")
    void testTextToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("text");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 int 转 Java 类型")
    void testIntToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("int");
        assertEquals(Integer.class, javaType);
    }

    @Test
    @DisplayName("测试 tinyint 转 Java 类型")
    void testTinyintToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("tinyint");
        assertEquals(Boolean.class, javaType);
    }

    @Test
    @DisplayName("测试 bigint 转 Java 类型")
    void testBigintToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("bigint");
        assertEquals(Long.class, javaType);
    }

    @Test
    @DisplayName("测试 decimal 转 Java 类型")
    void testDecimalToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("decimal(10,2)");
        assertEquals(Double.class, javaType);
    }

    @Test
    @DisplayName("测试 numeric 转 Java 类型")
    void testNumericToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("numeric(12,1)");
        assertEquals(Double.class, javaType);
    }

    @Test
    @DisplayName("测试 number 转 Java 类型")
    void testNumberToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("number(10)");
        assertEquals(Double.class, javaType);
    }

    @Test
    @DisplayName("测试 NUMBER 转 Java 类型（大写）")
    void testNumberUppercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("NUMBER(19)");
        assertEquals(Double.class, javaType);
    }

    @Test
    @DisplayName("测试 date 转 Java 类型")
    void testDateToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("date");
        assertEquals(Date.class, javaType);
    }

    @Test
    @DisplayName("测试 datetime 转 Java 类型")
    void testDatetimeToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("datetime");
        assertEquals(Date.class, javaType);
    }

    @Test
    @DisplayName("测试 timestamp 转 Java 类型")
    void testTimestampToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("timestamp");
        assertEquals(Date.class, javaType);
    }

    @Test
    @DisplayName("测试 TIMESTAMP 转 Java 类型（大写）")
    void testTimestampUppercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("TIMESTAMP");
        assertEquals(Date.class, javaType);
    }

    @Test
    @DisplayName("测试未知类型转 Object")
    void testUnknownTypeToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("unknown_type");
        assertEquals(Object.class, javaType);
    }

    private Class<?> invokeGetJavaType(String columnType) throws Exception {
        java.lang.reflect.Method method = DbOpDm8.class.getDeclaredMethod("getJavaType", String.class);
        method.setAccessible(true);
        return (Class<?>) method.invoke(dbOpDm8, columnType);
    }

    static class TestBean {
        private Long id;
        private String name;
        private Integer age;
        private int count;
        private Boolean active;
        private boolean enabled;
        private long timestamp;
        private Double score;
        private Float rating;
        private Date createTime;
        private LocalDateTime updateTime;
        private LocalDate birthDate;
        private Object data;
    }
}
