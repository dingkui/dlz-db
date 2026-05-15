package com.dlz.test.db.cases.helper.support.dbs;

import com.dlz.db.support.helper.DbOpMysql;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DbOpMysql 类型转换测试
 * 测试 MySQL 数据库的字段类型映射
 */
@DisplayName("DbOpMysql 类型转换测试")
class DbOpMysqlTest {

    private DbOpMysql dbOpMysql;

    @BeforeEach
    void setUp() {
        dbOpMysql = new DbOpMysql();
    }

    // ========== getDbColumnType 测试 ==========

    @Test
    @DisplayName("测试 String 类型映射")
    void testStringType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("name");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("varchar(255)", dbType);
    }

    @Test
    @DisplayName("测试 Integer 类型映射")
    void testIntegerType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("age");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("int", dbType);
    }

    @Test
    @DisplayName("测试 int 基本类型映射")
    void testIntPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("count");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("int", dbType);
    }

    @Test
    @DisplayName("测试 Boolean 类型映射")
    void testBooleanType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("active");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("tinyint", dbType);
    }

    @Test
    @DisplayName("测试 boolean 基本类型映射")
    void testBooleanPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("enabled");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("tinyint", dbType);
    }

    @Test
    @DisplayName("测试 Long 类型映射")
    void testLongType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("id");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("bigint", dbType);
    }

    @Test
    @DisplayName("测试 long 基本类型映射")
    void testLongPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("timestamp");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("bigint", dbType);
    }

    @Test
    @DisplayName("测试 Double 类型映射")
    void testDoubleType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("score");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("numeric(12, 1)", dbType);
    }

    @Test
    @DisplayName("测试 Date 类型映射")
    void testDateType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("createTime");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("datetime", dbType);
    }

    @Test
    @DisplayName("测试 LocalDateTime 类型映射")
    void testLocalDateTimeType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("updateTime");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("datetime", dbType);
    }

    @Test
    @DisplayName("测试 LocalDate 类型映射")
    void testLocalDateType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("birthDate");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("datetime", dbType);
    }

    @Test
    @DisplayName("测试 Object 类型映射")
    void testObjectType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("data");
        String dbType = dbOpMysql.getDbColumnType(field);
        
        assertEquals("text", dbType);
    }

    // ========== getJavaType (私有方法通过反射测试) ==========

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
    @DisplayName("测试未知类型转 Object")
    void testUnknownTypeToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("unknown_type");
        assertEquals(Object.class, javaType);
    }

    /**
     * 通过反射调用私有的 getJavaType 方法
     */
    private Class<?> invokeGetJavaType(String columnType) throws Exception {
        java.lang.reflect.Method method = DbOpMysql.class.getDeclaredMethod("getJavaType", String.class);
        method.setAccessible(true);
        return (Class<?>) method.invoke(dbOpMysql, columnType);
    }

    /**
     * 测试用的 Bean 类
     */
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
