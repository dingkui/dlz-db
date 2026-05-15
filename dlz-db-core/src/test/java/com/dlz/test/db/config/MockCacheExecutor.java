package com.dlz.test.db.config;

import com.dlz.db.core.IRedisExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试用缓存执行器 - 基于内存 Map
 */
public class MockCacheExecutor implements IRedisExecutor {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public long incrBy(String key, long step) {
        AtomicLong counter = counters.computeIfAbsent(key, k -> new AtomicLong(0));
        return counter.addAndGet(step);
    }
}
