package com.dlz.test.db.cases.annotation;

import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SnowFlakeUtil 雪花ID生成器测试")
class SnowFlakeUtilTest extends BaseDBTest {

    private Object newInstance(long dataCenterId, long machineId) throws Exception {
        Class<?> clazz = Class.forName("com.dlz.db.annotation.SnowFlakeUtil");
        Constructor<?> ctor = clazz.getDeclaredConstructor(long.class, long.class);
        ctor.setAccessible(true);
        return ctor.newInstance(dataCenterId, machineId);
    }

    private String callNextId(Object instance) throws Exception {
        Method m = instance.getClass().getDeclaredMethod("nextId");
        m.setAccessible(true);
        return (String) m.invoke(instance);
    }

    private String callStaticId() throws Exception {
        Class<?> clazz = Class.forName("com.dlz.db.annotation.SnowFlakeUtil");
        Method m = clazz.getDeclaredMethod("id");
        m.setAccessible(true);
        return (String) m.invoke(null);
    }

    @Test
    @DisplayName("静态 id() 返回非空且纯数字")
    void testStaticIdNotNullAndNumeric() throws Exception {
        String id = callStaticId();
        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertDoesNotThrow(() -> Long.parseLong(id));
    }

    @Test
    @DisplayName("静态 id() 生成唯一ID")
    void testStaticIdUnique() throws Exception {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            ids.add(callStaticId());
        }
        assertEquals(100, ids.size());
    }

    @Test
    @DisplayName("nextId 生成递增ID")
    void testNextIdIncreasing() throws Exception {
        Object snowFlake = newInstance(1, 1);
        long prev = Long.parseLong(callNextId(snowFlake));
        for (int i = 0; i < 50; i++) {
            long curr = Long.parseLong(callNextId(snowFlake));
            assertTrue(curr > prev, "ID should be increasing");
            prev = curr;
        }
    }

    @Test
    @DisplayName("dataCenterId 超范围抛异常")
    void testInvalidDataCenterId() {
        assertThrows(Exception.class, () -> newInstance(32, 1));
        assertThrows(Exception.class, () -> newInstance(-1, 1));
    }

    @Test
    @DisplayName("machineId 超范围抛异常")
    void testInvalidMachineId() {
        assertThrows(Exception.class, () -> newInstance(1, 32));
        assertThrows(Exception.class, () -> newInstance(1, -1));
    }

    @Test
    @DisplayName("不同 dataCenterId/machineId 产生不同ID")
    void testDifferentWorkers() throws Exception {
        Object sf1 = newInstance(0, 0);
        Object sf2 = newInstance(1, 1);
        String id1 = callNextId(sf1);
        String id2 = callNextId(sf2);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("边界值 dataCenterId=31, machineId=31 正常工作")
    void testBoundaryValues() throws Exception {
        Object snowFlake = newInstance(31, 31);
        String id = callNextId(snowFlake);
        assertNotNull(id);
        assertDoesNotThrow(() -> Long.parseLong(id));
    }
}
