package com.dlz.db.core;

/**
 * 空操作缓存执行器。
 * <p>核心模块默认实现，不执行任何缓存操作。</p>
 */
public class NoOpCacheExecutor implements CacheExecutor {

    @Override
    public void set(String key, String value) {
        // No-op
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public boolean del(String key) {
        return false;
    }

    @Override
    public long incrBy(String key, long step) {
        return step;
    }

    @Override
    public boolean exists(String key) {
        return false;
    }
}
