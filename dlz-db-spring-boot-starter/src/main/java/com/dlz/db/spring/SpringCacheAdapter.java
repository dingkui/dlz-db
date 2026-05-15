package com.dlz.db.spring;

import com.dlz.db.core.IRedisExecutor;
import com.dlz.spring.redis.excutor.JedisExecutor;

/**
 * Spring 缓存执行器实现。
 * <p>基于 {@link JedisExecutor}，委托给 Spring Redis 实现。</p>
 */
public class SpringCacheAdapter implements IRedisExecutor {

    private final JedisExecutor jedisExecutor;

    public SpringCacheAdapter(JedisExecutor jedisExecutor) {
        this.jedisExecutor = jedisExecutor;
    }

    @Override
    public void set(String key, String value) {
        jedisExecutor.set(key, value);
    }


    @Override
    public long incrBy(String key, long step) {
        return jedisExecutor.incrBy(key, step);
    }

}
