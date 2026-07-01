package com.dlz.test.db.cases.util;

import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.util.DbLogUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbLogUtil 全面覆盖测试
 */
@DisplayName("数据库日志工具测试")
class DbLogUtilTest {

    @AfterEach
    void tearDown() {
        // 重置配置，避免影响其他测试
        DlzDbProperties def = new DlzDbProperties();
        DbLogUtil.init(def);
    }

    @Test
    @DisplayName("init - 全量配置初始化")
    void testInit_Full() {
        DlzDbProperties properties = new DlzDbProperties();
        DlzDbProperties.Log log = new DlzDbProperties.Log();
        log.setShowCaller(true);
        log.setShowRunSql(true);
        log.setShowResult(true);
        log.setSlowSqlThreshold(1000);
        properties.setLog(log);
        assertDoesNotThrow(() -> DbLogUtil.init(properties));
    }

    @Test
    @DisplayName("init - 默认配置")
    void testInit_Default() {
        assertDoesNotThrow(() -> DbLogUtil.init(new DlzDbProperties()));
    }

    @Test
    @DisplayName("setCaller / clearCaller - 设置与清除调用者")
    void testSetAndClearCaller() {
        assertDoesNotThrow(() -> DbLogUtil.setCaller(1));
        assertDoesNotThrow(DbLogUtil::clearCaller);
    }

    @Test
    @DisplayName("generateSqlMessage - 单次执行（showResult=false）")
    void testGenerateSqlMessage_NoResult() {
        String msg = DbLogUtil.generateSqlMessage(100L, null, "test", "SELECT 1", new Object[]{});
        assertNotNull(msg);
        assertTrue(msg.contains("test"));
        assertTrue(msg.contains("SELECT 1"));
    }

    @Test
    @DisplayName("generateSqlMessage - 带结果（showResult=true）")
    void testGenerateSqlMessage_WithResult() {
        DlzDbProperties properties = new DlzDbProperties();
        DlzDbProperties.Log log = new DlzDbProperties.Log();
        log.setShowResult(true);
        properties.setLog(log);
        DbLogUtil.init(properties);

        String msg = DbLogUtil.generateSqlMessage(100L, "ok", "test", "SELECT 1", new Object[]{});
        assertNotNull(msg);
        assertTrue(msg.contains("result:ok"));
    }

    @Test
    @DisplayName("generateSqlMessage - 批量参数")
    void testGenerateSqlMessage_Batch() {
        String msg = DbLogUtil.generateSqlMessage(100L, "batch", "INSERT ...",
                Collections.singletonList(new Object[]{"v1"}));
        assertNotNull(msg);
        assertTrue(msg.contains("size:1"));
    }

    @Test
    @DisplayName("logInfo - 正常日志（无异常）")
    void testLogInfo_Normal() {
        assertDoesNotThrow(() ->
                DbLogUtil.logInfo((t, r) -> "normal log", System.currentTimeMillis(), null, null));
    }

    @Test
    @DisplayName("logInfo - 异常日志")
    void testLogInfo_WithError() {
        assertDoesNotThrow(() ->
                DbLogUtil.logInfo((t, r) -> "error log", System.currentTimeMillis(), null,
                        new RuntimeException("test error")));
    }

    @Test
    @DisplayName("logInfo - 慢 SQL 日志（slowSqlThreshold 触发 warn 分支）")
    void testLogInfo_SlowSql() {
        DlzDbProperties properties = new DlzDbProperties();
        DlzDbProperties.Log log = new DlzDbProperties.Log();
        log.setSlowSqlThreshold(10);
        properties.setLog(log);
        DbLogUtil.init(properties);

        // 传一个很早的时间戳，使 elapsed 很大，触发慢 SQL 警告
        assertDoesNotThrow(() ->
                DbLogUtil.logInfo((t, r) -> "slow sql", 0L, null, null));
    }

    @Test
    @DisplayName("logInfo - 带 caller 的日志")
    void testLogInfo_WithCaller() {
        DlzDbProperties properties = new DlzDbProperties();
        DlzDbProperties.Log log = new DlzDbProperties.Log();
        log.setShowCaller(true);
        properties.setLog(log);
        DbLogUtil.init(properties);

        assertDoesNotThrow(() ->
                DbLogUtil.logInfo((t, r) -> "caller log", System.currentTimeMillis(), null, null));
    }

    @Test
    @DisplayName("warn - 无异常调用")
    void testWarn_Normal() {
        assertDoesNotThrow(() -> DbLogUtil.warn("warn message", null));
    }

    @Test
    @DisplayName("warn - 带异常调用")
    void testWarn_WithError() {
        assertDoesNotThrow(() -> DbLogUtil.warn("warn with error",
                new RuntimeException("warn error")));
    }

    @Test
    @DisplayName("debug - 无异常调用")
    void testDebug_Normal() {
        assertDoesNotThrow(() -> DbLogUtil.debug("debug message", null));
    }

    @Test
    @DisplayName("debug - 带异常调用")
    void testDebug_WithError() {
        assertDoesNotThrow(() -> DbLogUtil.debug("debug with error",
                new RuntimeException("debug error")));
    }

    @Test
    @DisplayName("getTraceCaller - level 小于 1 时自动修正（覆盖边界分支）")
    void testGetTraceCaller_LevelZero() {
        // level < 1 应修正为 1
        String caller0 = DbLogUtil.getTraceCaller(0);
        String caller1 = DbLogUtil.getTraceCaller(1);
        assertNotNull(caller0);
        assertNotNull(caller1);
    }
}
