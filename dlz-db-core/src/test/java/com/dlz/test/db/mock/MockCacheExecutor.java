package com.dlz.test.db.mock;

import com.dlz.db.core.ICacheExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试用缓存执行器 - 基于内存 Map
 */
public class MockCacheExecutor implements ICacheExecutor {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public String get(String key) {
        return cache.get(key);
    }

    @Override
    public boolean del(String key) {
        return cache.remove(key) != null;
    }

    @Override
    public long incrBy(String key, long step) {
        AtomicLong counter = counters.computeIfAbsent(key, k -> new AtomicLong(0));
        return counter.addAndGet(step);
    }

    @Override
    public boolean exists(String key) {
        return cache.containsKey(key);
    }
}
