package com.dlz.test.db.cases.exception;

import com.dlz.db.exception.DbException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * DbException 测试类
 * 
 * @author test
 */
@DisplayName("数据库异常测试")
class DbExceptionTest {

    @Test
    @DisplayName("测试构造函数 - 带 cause")
    void testConstructor_WithCause() {
        Throwable cause = new Throwable("Test cause");
        DbException exception = new DbException("Test message", 1001, cause);
        
        assertEquals("1001:[Test message]", exception.getMessage());
        assertEquals(1001, exception.getCode());
//        assertEquals(cause, exception.getStackTrace()[0]);
    }

    @Test
    @DisplayName("测试构造函数 - 不带 cause")
    void testConstructor_WithoutCause() {
        DbException exception = new DbException("Test message", 1001);
        
        assertEquals("1001:[Test message]", exception.getMessage());
        assertEquals(1001, exception.getCode());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("测试错误码 - 数据库连接异常")
    void testErrorCode_Connection() {
        DbException exception = new DbException("Connection error", 1000);
        assertEquals(1000, exception.getCode());
    }

    @Test
    @DisplayName("测试错误码 - SQL 执行异常")
    void testErrorCode_SqlExecution() {
        DbException exception = new DbException("SQL error", 1001);
        assertEquals(1001, exception.getCode());
    }

    @Test
    @DisplayName("测试错误码 - 参数校验异常")
    void testErrorCode_ParameterValidation() {
        DbException exception = new DbException("Parameter error", 1002);
        assertEquals(1002, exception.getCode());
    }

    @Test
    @DisplayName("测试错误码 - 其他异常")
    void testErrorCode_Other() {
        DbException exception = new DbException("Other error", 1003);
        assertEquals(1003, exception.getCode());
    }

    @Test
    @DisplayName("测试错误码 - 结果异常")
    void testErrorCode_Result() {
        DbException exception = new DbException("Result error", 1004);
        assertEquals(1004, exception.getCode());
    }

    @Test
    @DisplayName("测试错误码 - 数据转换异常")
    void testErrorCode_Conversion() {
        DbException exception = new DbException("Conversion error", 1005);
        assertEquals(1005, exception.getCode());
    }

    @Test
    @DisplayName("测试错误码 - 事务执行异常")
    void testErrorCode_Transaction() {
        DbException exception = new DbException("Transaction error", 1006);
        assertEquals(1006, exception.getCode());
    }

    @Test
    @DisplayName("测试错误码 - 连接关闭异常")
    void testErrorCode_ConnectionClose() {
        DbException exception = new DbException("Connection close error", 1007);
        assertEquals(1007, exception.getCode());
    }

}
