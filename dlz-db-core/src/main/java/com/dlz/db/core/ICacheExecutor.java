package com.dlz.db.core;

/**
 * 缓存执行器接口。
 * <p>抽象缓存操作，支持不同缓存实现（Redis、Memcached 等）。</p>
 *
 * <p>核心模块提供空实现（NoOpCacheExecutor），Spring 模块提供 Jedis 实现。</p>
 */
public interface ICacheExecutor {

    /**
     * 设置缓存值。
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void set(String key, String value);

    /**
     * 获取缓存值。
     *
     * @param key 缓存键
     * @return 缓存值，不存在返回 null
     */
    String get(String key);

    /**
     * 删除缓存。
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean del(String key);

    /**
     * 原子递增。
     *
     * @param key  缓存键
     * @param step 步长
     * @return 递增后的值
     */
    long incrBy(String key, long step);

    /**
     * 检查键是否存在。
     *
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);
}
