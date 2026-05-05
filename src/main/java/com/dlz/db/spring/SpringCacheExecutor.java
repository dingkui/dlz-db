package com.dlz.db.spring;

import com.dlz.db.core.CacheExecutor;
import com.dlz.spring.redis.excutor.JedisExecutor;

/**
 * Spring 缓存执行器实现。
 * <p>基于 {@link JedisExecutor}，委托给 Spring Redis 实现。</p>
 */
public class SpringCacheExecutor implements CacheExecutor {

    private final JedisExecutor jedisExecutor;

    public SpringCacheExecutor(JedisExecutor jedisExecutor) {
        this.jedisExecutor = jedisExecutor;
    }

    @Override
    public void set(String key, String value) {
        jedisExecutor.set(key, value);
    }

    @Override
    public String get(String key) {
        return jedisExecutor.get(key);
    }

    @Override
    public boolean del(String key) {
        return jedisExecutor.del(key) > 0;
    }

    @Override
    public long incrBy(String key, long step) {
        return jedisExecutor.incrBy(key, step);
    }

    @Override
    public boolean exists(String key) {
        return jedisExecutor.exists(key);
    }
}
