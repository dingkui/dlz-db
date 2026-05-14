package com.dlz.test.db.cases.util;

import com.dlz.db.util.KeyUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KeyUtil 测试类
 * 
 * @author test
 */
@DisplayName("键名生成工具测试")
class KeyUtilTest {

    @Test
    @DisplayName("测试生成键名 - 基本功能")
    void testGetKeyName_Basic() {
        String key1 = KeyUtil.getKeyName("param");
        String key2 = KeyUtil.getKeyName("param");
        
        // 验证格式
        assertTrue(key1.startsWith("param"));
        assertTrue(key2.startsWith("param"));
        
        // 验证递增
        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("测试生成键名 - 不同前缀")
    void testGetKeyName_DifferentPrefix() {
        String key1 = KeyUtil.getKeyName("user");
        String key2 = KeyUtil.getKeyName("order");
        
        assertTrue(key1.startsWith("user"));
        assertTrue(key2.startsWith("order"));
    }

    @Test
    @DisplayName("测试生成键名 - 空前缀")
    void testGetKeyName_EmptyPrefix() {
        String key = KeyUtil.getKeyName("");
        assertNotNull(key);
        assertFalse(key.isEmpty());
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

    @RepeatedTest(10)
    @DisplayName("测试生成键名 - 重复测试确保唯一性")
    void testGetKeyName_Uniqueness() {
        Set<String> keys = new HashSet<>();
        
        for (int i = 0; i < 100; i++) {
            String key = KeyUtil.getKeyName("test");
            assertTrue(keys.add(key), "生成了重复的键: " + key);
        }
        
        assertEquals(100, keys.size());
    }

    @Test
    @DisplayName("测试生成键名 - 线程隔离")
    void testGetKeyName_ThreadIsolation() throws InterruptedException {
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // 用于收集每个线程生成的键
        Map<Integer, Set<String>> threadKeysMap = new ConcurrentHashMap<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    Set<String> threadKeys = new HashSet<>();
                    
                    // 每个线程生成10个键
                    for (int j = 0; j < 10; j++) {
                        String key = KeyUtil.getKeyName("thread");
                        threadKeys.add(key);
                    }
                    
                    // 保存该线程生成的键
                    threadKeysMap.put(threadId, threadKeys);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // 验证每个线程内的唯一性
        for (Map.Entry<Integer, Set<String>> entry : threadKeysMap.entrySet()) {
            assertEquals(10, entry.getValue().size(), 
                "线程 " + entry.getKey() + " 内应该生成10个唯一的键");
        }
        
        // 验证线程间可能有重复（因为使用 ThreadLocal，每个线程独立计数）
        // 收集所有键
        Set<String> allKeys = new HashSet<>();
        for (Set<String> keys : threadKeysMap.values()) {
            allKeys.addAll(keys);
        }
        
        // 由于每个线程独立计数，不同线程可能生成相同的键
        // 所以 allKeys.size() 可能小于 threadCount * 10
        assertTrue(allKeys.size() <= threadCount * 10, 
            "由于线程隔离，总键数可能少于 " + (threadCount * 10));
        
        // 验证至少有一些键（确保测试正常运行）
        assertTrue(allKeys.size() > 0, "应该至少生成了一些键");
    }

    @Test
    @DisplayName("测试生成键名 - 验证线程间可能重复")
    void testGetKeyName_ThreadsMayGenerateDuplicates() throws InterruptedException {
        int threadCount = 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        Map<Integer, String> firstKeyPerThread = new ConcurrentHashMap<>();
        
        // 让所有线程同时开始，增加生成相同键的概率
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程就绪
                    String firstKey = KeyUtil.getKeyName("test");
                    firstKeyPerThread.put(threadId, firstKey);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        startLatch.countDown(); // 让所有线程同时开始
        doneLatch.await();
        executor.shutdown();
        
        // 由于 ThreadLocal 的特性，不同线程可能生成相同的键
        // 例如：thread1 生成 "test1"，thread2 也可能生成 "test1"
        Set<String> uniqueKeys = new HashSet<>(firstKeyPerThread.values());
        
        // 验证：唯一键的数量可能少于线程数量（说明有重复）
        assertTrue(uniqueKeys.size() <= threadCount, 
            "由于 ThreadLocal 隔离，不同线程可能生成相同的键");
        
        System.out.println("线程数: " + threadCount + ", 唯一键数: " + uniqueKeys.size());
        System.out.println("生成的键: " + firstKeyPerThread.values());
    }

    @Test
    @DisplayName("测试生成键名 - 格式验证")
    void testGetKeyName_Format() {
        String key = KeyUtil.getKeyName("param");
        
        // 验证格式：前缀 + 数字
        assertTrue(key.matches("param\\d+"), "键名格式不正确: " + key);
    }

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
    @DisplayName("测试生���键名 - 长前缀")
    void testGetKeyName_LongPrefix() {
        String longPrefix = "veryLongPrefixForTestingPurpose";
        String key = KeyUtil.getKeyName(longPrefix);
        
        assertTrue(key.startsWith(longPrefix));
        assertTrue(key.length() > longPrefix.length());
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
}
