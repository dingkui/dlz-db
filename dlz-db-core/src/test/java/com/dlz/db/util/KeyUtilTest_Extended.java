package com.dlz.db.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KeyUtil 扩展测试类
 * 
 * @author test
 */
@DisplayName("键名生成工具扩展测试")
class KeyUtilTest_Extended {

    @Test
    @DisplayName("测试生成键名 - 特殊字符前缀")
    void testGetKeyName_SpecialCharPrefix() {
        String key1 = KeyUtil.getKeyName("param_");
        String key2 = KeyUtil.getKeyName("param$");
        String key3 = KeyUtil.getKeyName("param-");
        
        assertTrue(key1.startsWith("param_"));
        assertTrue(key2.startsWith("param$"));
        assertTrue(key3.startsWith("param-"));
    }

    @Test
    @DisplayName("测试生成键名 - 长前缀")
    void testGetKeyName_LongPrefix() {
        String longPrefix = "veryLongPrefixForTestingPurpose";
        String key = KeyUtil.getKeyName(longPrefix);
        
        assertTrue(key.startsWith(longPrefix));
        assertTrue(key.length() > longPrefix.length());
    }

    @Test
    @DisplayName("测试生成键名 - Unicode 前缀")
    void testGetKeyName_UnicodePrefix() {
        String key = KeyUtil.getKeyName("参数");
        assertTrue(key.startsWith("参数"));
    }

    @Test
    @DisplayName("测试生成键名 - 空格前缀")
    void testGetKeyName_SpacePrefix() {
        String key = KeyUtil.getKeyName("param ");
        assertTrue(key.startsWith("param "));
    }

    @Test
    @DisplayName("测试生成键名 - Tab 前缀")
    void testGetKeyName_TabPrefix() {
        String key = KeyUtil.getKeyName("param\t");
        assertTrue(key.startsWith("param\t"));
    }

    @Test
    @DisplayName("测试生成键名 - 换行前缀")
    void testGetKeyName_NewlinePrefix() {
        String key = KeyUtil.getKeyName("param\n");
        assertTrue(key.startsWith("param\n"));
    }

    @Test
    @DisplayName("测试生成键名 - 混合前缀")
    void testGetKeyName_MixedPrefix() {
        String key = KeyUtil.getKeyName("param_123_$-test");
        assertTrue(key.startsWith("param_123_$-test"));
    }

    @Test
    @DisplayName("测试生成键名 - 重复测试确保唯一性")
    void testGetKeyName_Uniqueness() {
        Set<String> keys = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            String key = KeyUtil.getKeyName("test");
            assertTrue(keys.add(key), "生成了重复的键: " + key);
        }
        
        assertEquals(1000, keys.size());
    }

    @Test
    @DisplayName("测试生成键名 - 大量生成")
    void testGetKeyName_LargeScale() {
        Set<String> keys = new HashSet<>();
        
        for (int i = 0; i < 10000; i++) {
            String key = KeyUtil.getKeyName("large");
            keys.add(key);
        }
        
        // 验证所有键都是唯一的
        assertEquals(10000, keys.size());
    }

    @Test
    @DisplayName("测试生成键名 - 格式验证")
    void testGetKeyName_Format() {
        String key = KeyUtil.getKeyName("param");
        
        // 验证格式：前缀 + 数字
        assertTrue(key.matches("param\\d+"), "键名格式不正确: " + key);
    }

    @Test
    @DisplayName("测试生成键名 - 性能测试")
    void testGetKeyName_Performance() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10000; i++) {
            KeyUtil.getKeyName("perf");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 10000次调用应该在1秒内完成
        assertTrue(duration < 1000, "性能测试失败，耗时: " + duration + "ms");
    }

    @Test
    @DisplayName("测试生成键名 - 连续生成")
    void testGetKeyName_Sequential() {
        String key1 = KeyUtil.getKeyName("p");
        String key2 = KeyUtil.getKeyName("p");
        String key3 = KeyUtil.getKeyName("p");
        
        // 提取数字部分
        int num1 = Integer.parseInt(key1.substring(1));
        int num2 = Integer.parseInt(key2.substring(1));
        int num3 = Integer.parseInt(key3.substring(1));
        
        // 验证递增
        assertEquals(num1 + 1, num2);
        assertEquals(num2 + 1, num3);
    }

    @Test
    @DisplayName("测试生成键名 - 不同前缀独立计数")
    void testGetKeyName_DifferentPrefixes() {
        String key1a = KeyUtil.getKeyName("a");
        String key1b = KeyUtil.getKeyName("b");
        String key2a = KeyUtil.getKeyName("a");
        String key2b = KeyUtil.getKeyName("b");
        
        // 不同前缀应该独立计数
        assertNotEquals(key1a, key2a);
        assertNotEquals(key1b, key2b);
        
        // 同一前缀应该递增
        assertTrue(key2a.compareTo(key1a) > 0);
        assertTrue(key2b.compareTo(key1b) > 0);
    }

    @Test
    @DisplayName("测试生成键名 - 空前缀")
    void testGetKeyName_EmptyPrefix() {
        String key = KeyUtil.getKeyName("");
        assertNotNull(key);
        assertFalse(key.isEmpty());
        
        // 验证格式：数字
        assertTrue(key.matches("\\d+"), "键名格式不正确: " + key);
    }

    @Test
    @DisplayName("测试生成键名 - 单字符前缀")
    void testGetKeyName_SingleCharPrefix() {
        String key = KeyUtil.getKeyName("a");
        assertTrue(key.startsWith("a"));
    }

    @Test
    @DisplayName("测试生成键名 - 多字符前缀")
    void testGetKeyName_MultiCharPrefix() {
        String key = KeyUtil.getKeyName("abc");
        assertTrue(key.startsWith("abc"));
    }

    @Test
    @DisplayName("测试生成键名 - 数字前缀")
    void testGetKeyName_NumberPrefix() {
        String key = KeyUtil.getKeyName("123");
        assertTrue(key.startsWith("123"));
    }

    @Test
    @DisplayName("测试生成键名 - 符号前缀")
    void testGetKeyName_SymbolPrefix() {
        String key = KeyUtil.getKeyName("@#$%");
        assertTrue(key.startsWith("@#$%"));
    }

    @Test
    @DisplayName("测试生成键名 - 混合符号前缀")
    void testGetKeyName_MixedSymbolPrefix() {
        String key = KeyUtil.getKeyName("a1@b2#c3");
        assertTrue(key.startsWith("a1@b2#c3"));
    }
}
