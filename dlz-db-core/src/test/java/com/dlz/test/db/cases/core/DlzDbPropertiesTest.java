package com.dlz.test.db.cases.core;

import com.dlz.db.core.DlzDbProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DlzDbProperties 配置类测试")
class DlzDbPropertiesTest {

    @Test
    @DisplayName("默认值验证")
    void testDefaultValues() {
        DlzDbProperties props = new DlzDbProperties();
        assertEquals("", props.getDbSupport());
        assertEquals("GBK", props.getBlob_charset());
        assertEquals(1, props.getSqllist().size());
        assertEquals("app/*", props.getSqllist().get(0));
        assertEquals("select sql_key as k ,sql_value as s from sys_sql", props.getSql());
        assertFalse(props.isUseDbSql());
        assertEquals(-1, props.getTableCacheTime());
        assertEquals("deleted", props.getLogicDeleteField());
        assertNotNull(props.getHelper());
        assertNotNull(props.getLog());
    }

    @Test
    @DisplayName("getSqllist 返回不可变列表")
    void testSqllistUnmodifiable() {
        DlzDbProperties props = new DlzDbProperties();
        List<String> list = props.getSqllist();
        assertThrows(UnsupportedOperationException.class, () -> list.add("new"));
    }

    @Test
    @DisplayName("setSqllist 创建副本")
    void testSetSqllistCreatesCopy() {
        DlzDbProperties props = new DlzDbProperties();
        List<String> original = Arrays.asList("a", "b");
        props.setSqllist(original);
        assertEquals(2, props.getSqllist().size());
        assertEquals("a", props.getSqllist().get(0));
    }

    @Test
    @DisplayName("Helper 默认值验证")
    void testHelperDefaults() {
        DlzDbProperties.Helper helper = new DlzDbProperties.Helper();
        assertEquals("com.dlz", helper.getPackageName());
        assertFalse(helper.isAutoUpdate());
    }

    @Test
    @DisplayName("Helper setter/getter")
    void testHelperSetterGetter() {
        DlzDbProperties.Helper helper = new DlzDbProperties.Helper();
        helper.setPackageName("com.test");
        helper.setAutoUpdate(true);
        assertEquals("com.test", helper.getPackageName());
        assertTrue(helper.isAutoUpdate());
    }

    @Test
    @DisplayName("Log 默认值验证")
    void testLogDefaults() {
        DlzDbProperties.Log log = new DlzDbProperties.Log();
        assertFalse(log.isShowResult());
        assertFalse(log.isShowRunSql());
        assertFalse(log.isShowCaller());
        assertEquals(0L, log.getSlowSqlThreshold());
    }

    @Test
    @DisplayName("Log setter/getter")
    void testLogSetterGetter() {
        DlzDbProperties.Log log = new DlzDbProperties.Log();
        log.setShowResult(true);
        log.setShowRunSql(true);
        log.setShowCaller(true);
        log.setSlowSqlThreshold(1000L);
        assertTrue(log.isShowResult());
        assertTrue(log.isShowRunSql());
        assertTrue(log.isShowCaller());
        assertEquals(1000L, log.getSlowSqlThreshold());
    }

    @Test
    @DisplayName("各属性可正常设置和读取")
    void testSettersAndGetters() {
        DlzDbProperties props = new DlzDbProperties();
        props.setDbSupport("mysql");
        props.setBlob_charset("UTF-8");
        props.setSql("select * from sys");
        props.setUseDbSql(true);
        props.setTableCacheTime(600);
        props.setLogicDeleteField("is_deleted");

        assertEquals("mysql", props.getDbSupport());
        assertEquals("UTF-8", props.getBlob_charset());
        assertEquals("select * from sys", props.getSql());
        assertTrue(props.isUseDbSql());
        assertEquals(600, props.getTableCacheTime());
        assertEquals("is_deleted", props.getLogicDeleteField());
    }
}
