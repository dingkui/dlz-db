package com.dlz.test.db.cases.util;

import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.util.DbLogUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbLogUtil 测试类
 * 
 * @author test
 */
@DisplayName("数据库日志工具测试")
class DbLogUtilTest {

    @Test
    @DisplayName("测试 init 方法 - 初始化配置")
    void testInit() {
        DlzDbProperties properties = new DlzDbProperties();
        
        // 设置日志配置
        DlzDbProperties.Log log = new DlzDbProperties.Log();
        log.setShowCaller(true);
        log.setShowRunSql(true);
        log.setShowResult(true);
        log.setSlowSqlThreshold(1000);
        
        properties.setLog(log);
        
        // 测试初始化
        assertDoesNotThrow(() -> DbLogUtil.init(properties));
    }

    @Test
    @DisplayName("测试 init 方法 - 默认配置")
    void testInit_Default() {
        DlzDbProperties properties = new DlzDbProperties();
        
        // 测试使用默认配置初始化
        assertDoesNotThrow(() -> DbLogUtil.init(properties));
    }

    @Test
    @DisplayName("测试 setCaller 方法")
    void testSetCaller() {
        // 测试设置调用者信息
        assertDoesNotThrow(() -> DbLogUtil.setCaller(1));
    }

    @Test
    @DisplayName("测试 clearCaller 方法")
    void testClearCaller() {
        // 先设置调用者
        DbLogUtil.setCaller(1);
        
        // 测试清除调用者
        assertDoesNotThrow(DbLogUtil::clearCaller);
    }

    @Test
    @DisplayName("测试 generateSqlMessage 方法 - 带结果")
    void testGenerateSqlMessage_WithResult() {
        Long time = 100L;
        ResultMap result = new ResultMap();
        result.put("id", 1);
        result.put("name", "test");
        String methodName = "testMethod";
        String sql = "SELECT * FROM test";
        Object[] args = new Object[]{};

        String message = DbLogUtil.generateSqlMessage(time, result, methodName, sql, args);
        assertNotNull(message);
        assertTrue(message.contains(methodName));
        assertTrue(message.contains(sql));
    }


    @Test
    @DisplayName("测试 generateSqlMessage 方法 - 批量参数")
    void testGenerateSqlMessage_Batch() {
        Long time = 100L;
        String methodName = "testMethod";
        String sql = "INSERT INTO test VALUES (?)";
        java.util.List<Object[]> batchArgs = java.util.Arrays.asList(
            new Object[]{"value1"},
            new Object[]{"value2"}
        );

        String message = DbLogUtil.generateSqlMessage(time, methodName, sql, batchArgs);
        assertNotNull(message);
        assertTrue(message.contains(methodName));
        assertTrue(message.contains("size:2"));
    }

    @Test
    @DisplayName("测试 logInfo 方法 - 正常日志")
    void testLogInfo_Normal() {
        Long time = System.currentTimeMillis();
        ResultMap result = new ResultMap();
        
        // 测试正常日志记录（不抛出异常即可）
        assertDoesNotThrow(() -> 
            DbLogUtil.logInfo((t, r) -> "Test message", time, result, null)
        );
    }

//    @Test
//    @DisplayName("测试 logInfo 方法 - 异常日志")
//    void testLogInfo_Exception() {
//        Long time = System.currentTimeMillis();
//        ResultMap result = new ResultMap();
//        Exception error = new RuntimeException("Test error");
//
//        // 测试异常日志记录（不抛出异常即可）
//        assertDoesNotThrow(() ->
//            DbLogUtil.logInfo((t, r) -> "Test message", time, result, error)
//        );
//    }
}
