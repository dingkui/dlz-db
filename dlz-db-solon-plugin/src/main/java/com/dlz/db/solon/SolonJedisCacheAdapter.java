package com.dlz.db.solon;

import com.dlz.db.core.ICacheExecutor;
import redis.clients.jedis.JedisPool;

/**
 * Solon Redis 缓存执行器实现：基于原生 {@link JedisPool} 的轻量包装。
 *
 * <p>需要在 Solon 应用中注册 {@link JedisPool} Bean 后，本类作为 {@link ICacheExecutor} 自动启用。</p>
 *
 * @since 7.0.0
 */
public class SolonJedisCacheAdapter implements ICacheExecutor {

    private final JedisPool jedisPool;

    public SolonJedisCacheAdapter(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void set(String key, String value) {
        try (redis.clients.jedis.Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    @Override
    public String get(String key) {
        try (redis.clients.jedis.Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public boolean del(String key) {
        try (redis.clients.jedis.Jedis jedis = jedisPool.getResource()) {
            return jedis.del(key) > 0;
        }
    }

    @Override
    public long incrBy(String key, long step) {
        try (redis.clients.jedis.Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, step);
        }
    }

    @Override
    public boolean exists(String key) {
        try (redis.clients.jedis.Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }
}
