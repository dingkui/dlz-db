package com.dlz.db.core.abstractor;

import com.dlz.db.core.ICacheExecutor;

/**
 * 缓存执行器抽象适配器。
 * <p>提供缓存操作的默认实现，子类可选择性重写方法。</p>
 *
 * @since 7.0.0
 */
public abstract class ACacheAdapter implements ICacheExecutor {

    @Override
    public void set(String key, String value) {
        // 默认不做任何操作
    }

    @Override
    public String get(String key) {
        // 默认返回 null
        return null;
    }

    @Override
    public boolean del(String key) {
        // 默认返回 false
        return false;
    }

    @Override
    public long incrBy(String key, long step) {
        // 默认返回步长
        return step;
    }

    @Override
    public boolean exists(String key) {
        // 默认返回 false
        return false;
    }
}
