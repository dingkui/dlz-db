package com.dlz.test.db.cases.helper.support.dbs;

import com.dlz.db.helper.support.dbs.DbOpSqlite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DbOpSqlite 类型转换测试
 * 测试 SQLite 数据库的字段类型映射
 */
@DisplayName("DbOpSqlite 类型转换测试")
class DbOpSqliteTest {

    private DbOpSqlite dbOpSqlite;

    @BeforeEach
    void setUp() {
        dbOpSqlite = new DbOpSqlite();
    }

    @Test
    @DisplayName("测试 String 类型映射到 TEXT")
    void testStringType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("name");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("TEXT", dbType);
    }

    @Test
    @DisplayName("测试 Integer 类型映射到 INTEGER")
    void testIntegerType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("age");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("INTEGER", dbType);
    }

    @Test
    @DisplayName("测试 int 基本类型映射到 INTEGER")
    void testIntPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("count");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("INTEGER", dbType);
    }

    @Test
    @DisplayName("测试 Boolean 类型映射到 TEXT")
    void testBooleanType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("active");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("TEXT", dbType);
    }

    @Test
    @DisplayName("测试 boolean 基本类型映射到 TEXT")
    void testBooleanPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("enabled");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("TEXT", dbType);
    }

    @Test
    @DisplayName("测试 Long 类型映射到 INTEGER")
    void testLongType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("id");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("INTEGER", dbType);
    }

    @Test
    @DisplayName("测试 long 基本类型映射到 INTEGER")
    void testLongPrimitiveType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("timestamp");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("INTEGER", dbType);
    }

    @Test
    @DisplayName("测试 Double 类型映射到 REAL")
    void testDoubleType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("score");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("REAL", dbType);
    }

    @Test
    @DisplayName("测试 Float 类型映射到 REAL")
    void testFloatType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("rating");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("REAL", dbType);
    }

    @Test
    @DisplayName("测试 Date 类型映射到 TEXT")
    void testDateType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("createTime");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("TEXT", dbType);
    }

    @Test
    @DisplayName("测试 Object 类型映射到 TEXT")
    void testObjectType() throws NoSuchFieldException {
        Field field = TestBean.class.getDeclaredField("data");
        String dbType = dbOpSqlite.getDbColumnType(field);
        
        assertEquals("TEXT", dbType);
    }

    @Test
    @DisplayName("测试 TEXT 转 String 类型")
    void testTextToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("TEXT");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 text 转 String 类型（小写）")
    void testTextLowercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("text");
        assertEquals(String.class, javaType);
    }

    @Test
    @DisplayName("测试 INTEGER 转 Integer 类型")
    void testIntegerToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("INTEGER");
        assertEquals(Integer.class, javaType);
    }

    @Test
    @DisplayName("测试 integer 转 Integer 类型（小写）")
    void testIntegerLowercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("integer");
        assertEquals(Integer.class, javaType);
    }

    @Test
    @DisplayName("测试 REAL 转 Double 类型")
    void testRealToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("REAL");
        assertEquals(Double.class, javaType);
    }

    @Test
    @DisplayName("测试 real 转 Double 类型（小写）")
    void testRealLowercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("real");
        assertEquals(Double.class, javaType);
    }

    @Test
    @DisplayName("测试 BLOB 转 byte[] 类型")
    void testBlobToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("BLOB");
        assertEquals(byte[].class, javaType);
    }

    @Test
    @DisplayName("测试 blob 转 byte[] 类型（小写）")
    void testBlobLowercaseToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("blob");
        assertEquals(byte[].class, javaType);
    }

    @Test
    @DisplayName("测试未知类型转 Object")
    void testUnknownTypeToJavaType() throws Exception {
        Class<?> javaType = invokeGetJavaType("UNKNOWN");
        assertEquals(Object.class, javaType);
    }

    private Class<?> invokeGetJavaType(String columnType) throws Exception {
        java.lang.reflect.Method method = DbOpSqlite.class.getDeclaredMethod("getJavaType", String.class);
        method.setAccessible(true);
        return (Class<?>) method.invoke(dbOpSqlite, columnType);
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
        private Object data;
    }
}
