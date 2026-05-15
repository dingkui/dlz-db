package com.dlz.db.core;

/**
 * 缓存执行器接口。
 * <p>抽象缓存操作，支持不同缓存实现（Redis、Memcached 等）。</p>
 *
 * <p>核心模块提供空实现（NoOpCacheExecutor），Spring 模块提供 Jedis 实现。</p>
 */
public interface IRedisExecutor {

    /**
     * 设置缓存值。
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void set(String key, String value);

    /**
     * 原子递增。
     *
     * @param key  缓存键
     * @param step 步长
     * @return 递增后的值
     */
    long incrBy(String key, long step);
}
