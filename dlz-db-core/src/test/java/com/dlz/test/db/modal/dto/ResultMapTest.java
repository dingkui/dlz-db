package com.dlz.test.db.modal.dto;

import com.dlz.db.modal.dto.ResultMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ResultMap 测试类
 * 
 * @author test
 */
@DisplayName("结果映射测试")
class ResultMapTest {

    @Test
    @DisplayName("测试基本 put 和 get")
    void testPutAndGet() {
        ResultMap map = new ResultMap();
        
        map.put("id", 1);
        map.put("name", "test");
        
        assertEquals(Integer.valueOf(1), map.get("id"));
        assertEquals("test", map.get("name"));
    }

    @Test
    @DisplayName("测试 coverDate2Str - 日期转字符串")
    void testCoverDate2Str() {
        ResultMap map = new ResultMap();
        map.put("date1", new Date());
        map.put("date2", new Date());
        map.put("name", "test");
        
        map.coverDate2Str("yyyy-MM-dd HH:mm:ss");
        
        // 日期应该被转换为字符串
        assertTrue(map.get("date1") instanceof String);
        assertTrue(map.get("date2") instanceof String);
        // 非日期字段应该保持原类型
        assertTrue(map.get("name") instanceof String);
    }

    @Test
    @DisplayName("测试 coverDate2Str - 空 Map")
    void testCoverDate2Str_Empty() {
        ResultMap map = new ResultMap();
        
        // 空 Map 不应该抛出异常
        assertDoesNotThrow(() -> map.coverDate2Str("yyyy-MM-dd"));
    }

    @Test
    @DisplayName("测试 coverDate2Str - null 值")
    void testCoverDate2Str_Null() {
        ResultMap map = new ResultMap();
        map.put("date1", null);
        map.put("name", "test");
        
        // null 值不应该抛出异常
        assertDoesNotThrow(() -> map.coverDate2Str("yyyy-MM-dd"));
    }

    @Test
    @DisplayName("测试 coverDate2Str - 无日期字段")
    void testCoverDate2Str_NoDate() {
        ResultMap map = new ResultMap();
        map.put("id", 1);
        map.put("name", "test");
        
        // 没有日期字段不应该抛出异常
        assertDoesNotThrow(() -> map.coverDate2Str("yyyy-MM-dd"));
    }

    @Test
    @DisplayName("测试覆盖已有值")
    void testPutOverride() {
        ResultMap map = new ResultMap();
        
        map.put("id", 1);
        map.put("id", 2);
        
        assertEquals(Integer.valueOf(2), map.get("id"));
    }

    @Test
    @DisplayName("测试 containsKey")
    void testContainsKey() {
        ResultMap map = new ResultMap();
        map.put("id", 1);
        
        assertTrue(map.containsKey("id"));
        assertFalse(map.containsKey("nonexistent"));
    }

    @Test
    @DisplayName("测试 size")
    void testSize() {
        ResultMap map = new ResultMap();
        
        assertEquals(0, map.size());
        
        map.put("id", 1);
        map.put("name", "test");
        
        assertEquals(2, map.size());
    }

    @Test
    @DisplayName("测试 clear")
    void testClear() {
        ResultMap map = new ResultMap();
        map.put("id", 1);
        map.put("name", "test");
        
        map.clear();
        
        assertEquals(0, map.size());
    }

    @Test
    @DisplayName("测试 keySet")
    void testKeySet() {
        ResultMap map = new ResultMap();
        map.put("id", 1);
        map.put("name", "test");
        
        assertEquals(2, map.keySet().size());
        assertTrue(map.keySet().contains("id"));
        assertTrue(map.keySet().contains("name"));
    }

    @Test
    @DisplayName("测试 values")
    void testValues() {
        ResultMap map = new ResultMap();
        map.put("id", 1);
        map.put("name", "test");
        
        assertEquals(2, map.values().size());
    }
}
