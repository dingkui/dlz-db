package com.dlz.test.db.helper.support.dbs;

import com.dlz.db.helper.support.dbs.DbOpPostgresql;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DbOpPostgresql 类型转换测试
 * 测试 PostgreSQL 数据库的字段类型映射
 */
@DisplayName("DbOpPostgresql 类型转换测试")
class DbOpPostgresqlTest {

    private DbOpPostgresql dbOpPostgresql;

    @BeforeEach
    void setUp() {
        dbOpPostgresql = new DbOpPostgresql();
    }

    @Test
    @DisplayName("测试 String 类型映射")
    void testStringType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("name");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("varchar(255)", dbType);
    }

    @Test
    @DisplayName("测试 Integer 类型映射到 int2")
    void testIntegerType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("age");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("int2", dbType);
    }

    @Test
    @DisplayName("测试 int 基本类型映射到 int2")
    void testIntPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("count");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("int2", dbType);
    }

    @Test
    @DisplayName("测试 Boolean 类型映射到 bool")
    void testBooleanType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("active");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("bool", dbType);
    }

    @Test
    @DisplayName("测试 boolean 基本类型映射到 bool")
    void testBooleanPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("enabled");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("bool", dbType);
    }

    @Test
    @DisplayName("测试 Long 类型映射到 int4")
    void testLongType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("id");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("int4", dbType);
    }

    @Test
    @DisplayName("测试 long 基本类型映射到 int4")
    void testLongPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("timestamp");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("int4", dbType);
    }

    @Test
    @DisplayName("测试 Double 类型映射到 numeric")
    void testDoubleType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("score");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("numeric(12, 1)", dbType);
    }

    @Test
    @DisplayName("测试 Date 类型映射到 date")
    void testDateType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("createTime");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("date", dbType);
    }

    @Test
    @DisplayName("测试 LocalDateTime 类型映射到 date")
    void testLocalDateTimeType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("updateTime");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("date", dbType);
    }

    @Test
    @DisplayName("测试 LocalDate 类型映射到 date")
    void testLocalDateType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("birthDate");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("date", dbType);
    }

    @Test
    @DisplayName("测试 Object 类型映射到 text")
    void testObjectType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("data");
        String dbType = dbOpPostgresql.getDbColumnType(field);
        
        assertEquals("text", dbType);
    }

    @Test
    @DisplayName("测试 varchar 转 Java 类型")
    void testVarcharToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("varchar(255)");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 char 转 Java 类型")
    void testCharToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("char(10)");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 VARCHAR 转 Java 类型（大写）")
    void testVarcharUppercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("VARCHAR");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 int 转 Java 类型")
    void testIntToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("int");
        assertEquals(Integer.class, javaType);
    }

    @Test
    @DisplayName("测试 integer 转 Java 类型")
    void testIntegerToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("integer");
        assertEquals(Integer.class, javaType);
    }

    @Test
    @DisplayName("测试 INT 转 Java 类型（大写）")
    void testIntUppercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("INT");
        assertEquals(Integer.class, javaType);
    }

    @Test
    @DisplayName("测试 boolean 转 Java 类型")
    void testBooleanToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("boolean");
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
    @DisplayName("测试 date 转 Java 类型")
    void testDateToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("date");
        assertEquals(Date.class, javaType);
    }

    @Test
    @DisplayName("测试 timestamp 转 LocalDateTime 类型")
    void testTimestampToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("timestamp");
        assertEquals(LocalDateTime.class, javaType);
    }

    @Test
    @DisplayName("测试 TIMESTAMP 转 LocalDateTime 类型（大写）")
    void testTimestampUppercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("TIMESTAMP");
        assertEquals(LocalDateTime.class, javaType);
    }

    @Test
    @DisplayName("测试未知类型转 Object")
    void testUnknownTypeToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("unknown_type");
        assertEquals(Object.class, javaType);
    }

    private Class<?> invokeGetJavaType(String columnType) throws Exception {
        java.lang.reflect.Method method = DbOpPostgresql.class.getDeclaredMethod("getJavaType", String.class);
        method.setAccessible(true);
        return (Class<?>) method.invoke(dbOpPostgresql, columnType);
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
        private Date createTime;
        private LocalDateTime updateTime;
        private LocalDate birthDate;
        private Object data;
    }
}
