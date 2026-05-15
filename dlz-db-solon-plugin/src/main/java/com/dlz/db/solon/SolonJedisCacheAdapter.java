package com.dlz.db.solon;

import com.dlz.db.core.IRedisExecutor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import redis.clients.jedis.JedisPool;

/**
 * Solon Redis 缓存执行器实现：基于原生 {@link JedisPool} 的轻量包装。
 *
 * <p>需要在 Solon 应用中注册 {@link JedisPool} Bean 后，本类作为 {@link IRedisExecutor} 自动启用。</p>
 *
 * @since 7.0.0
 */
public class SolonJedisCacheAdapter implements IRedisExecutor {

    private final JedisPool jedisPool;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JedisPool由容器注入，视为外部管理资源")
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
    public long incrBy(String key, long step) {
        try (redis.clients.jedis.Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, step);
        }
    }
}
