package com.dlz.test.db.helper.support;

import com.dlz.db.helper.support.SnowFlake;
import com.dlz.kit.exception.ValidateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SnowFlake 雪花算法测试类
 */
@DisplayName("SnowFlake 雪花算法测试")
class SnowFlakeTest {

    @Test
    @DisplayName("测试生成 ID - 基本功能")
    void testGenerateId() {
        SnowFlake snowFlake = new SnowFlake(1, 1);
        
        String id = snowFlake.nextId();
        
        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertTrue(id.matches("\\d+"), "ID 应该是数字字符串");
    }

    @Test
    @DisplayName("测试静态方法生成 ID")
    void testStaticIdMethod() {
        String id = SnowFlake.id();
        
        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertTrue(id.matches("\\d+"));
    }

    @Test
    @DisplayName("测试生成的 ID 唯一性")
    void testIdUniqueness() {
        SnowFlake snowFlake = new SnowFlake(1, 1);
        Set<String> ids = new HashSet<>();
        
        // 生成 1000 个 ID
        for (int i = 0; i < 1000; i++) {
            String id = snowFlake.nextId();
            assertTrue(ids.add(id), "ID 应该唯一，重复的 ID: " + id);
        }
        
        assertEquals(1000, ids.size(), "应该生成 1000 个唯一的 ID");
    }

    @Test
    @DisplayName("测试不同机器 ID 生成不同的 ID")
    void testDifferentMachineIds() {
        SnowFlake snowFlake1 = new SnowFlake(1, 1);
        SnowFlake snowFlake2 = new SnowFlake(1, 2);
        
        String id1 = snowFlake1.nextId();
        String id2 = snowFlake2.nextId();
        
        // 不同机器的 ID 应该不同（虽然理论上可能相同，但概率极低）
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("测试不同数据中心 ID 生成不同的 ID")
    void testDifferentDataCenterIds() {
        SnowFlake snowFlake1 = new SnowFlake(1, 1);
        SnowFlake snowFlake2 = new SnowFlake(2, 1);
        
        String id1 = snowFlake1.nextId();
        String id2 = snowFlake2.nextId();
        
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("测试连续生成的 ID 递增")
    void testSequentialIdsIncreasing() {
        SnowFlake snowFlake = new SnowFlake(1, 1);
        
        long prevId = Long.parseLong(snowFlake.nextId());
        
        for (int i = 0; i < 100; i++) {
            long currId = Long.parseLong(snowFlake.nextId());
            assertTrue(currId > prevId, "ID 应该递增: " + prevId + " -> " + currId);
            prevId = currId;
        }
    }

    @Test
    @DisplayName("测试 dataCenterId 边界值 - 最小值")
    void testDataCenterIdMinBoundary() {
        // 注意：SnowFlake 有静态实例，测试边界值可能触发异常
        // 这里只测试正常范围的值
        assertDoesNotThrow(() -> {
            SnowFlake sf = new SnowFlake(0, 1);
            assertNotNull(sf.nextId());
        });
    }

    @Test
    @DisplayName("测试 dataCenterId 边界值 - 最大值")
    void testDataCenterIdMaxBoundary() {
        // MAX_DATA_CENTER_NUM = 31 (5 bits)
        assertDoesNotThrow(() -> {
            SnowFlake sf = new SnowFlake(31, 1);
            assertNotNull(sf.nextId());
        });
    }

    @Test
    @DisplayName("测试 machineId 边界值 - 最小值")
    void testMachineIdMinBoundary() {
        assertDoesNotThrow(() -> {
            SnowFlake sf = new SnowFlake(1, 0);
            assertNotNull(sf.nextId());
        });
    }

    @Test
    @DisplayName("测试 machineId 边界值 - 最大值")
    void testMachineIdMaxBoundary() {
        // MAX_MACHINE_NUM = 31 (5 bits)
        assertDoesNotThrow(() -> {
            SnowFlake sf = new SnowFlake(1, 31);
            assertNotNull(sf.nextId());
        });
    }

    @Test
    @DisplayName("测试高并发下 ID 唯一性")
    void testHighConcurrencyUniqueness() throws InterruptedException {
        SnowFlake snowFlake = new SnowFlake(1, 1);
        Set<String> ids = new HashSet<>();
        int threadCount = 10;
        int idsPerThread = 100;
        
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    String id = snowFlake.nextId();
                    synchronized (ids) {
                        ids.add(id);
                    }
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertEquals(threadCount * idsPerThread, ids.size(), 
                "高并发下应该生成唯一的 ID");
    }

    @Test
    @DisplayName("测试 ID 长度合理性")
    void testIdLength() {
        SnowFlake snowFlake = new SnowFlake(1, 1);
        
        // 生成多个 ID，检查长度是否合理
        for (int i = 0; i < 10; i++) {
            String id = snowFlake.nextId();
            // SnowFlake ID 通常在 15-20 位之间
            assertTrue(id.length() >= 15 && id.length() <= 20, 
                    "ID 长度应该在合理范围内: " + id.length());
        }
    }

    @Test
    @DisplayName("测试 ID 可解析为 Long")
    void testIdParsableToLong() {
        SnowFlake snowFlake = new SnowFlake(1, 1);
        
        for (int i = 0; i < 100; i++) {
            String id = snowFlake.nextId();
            assertDoesNotThrow(() -> Long.parseLong(id), 
                    "ID 应该可以解析为 Long: " + id);
        }
    }
}
