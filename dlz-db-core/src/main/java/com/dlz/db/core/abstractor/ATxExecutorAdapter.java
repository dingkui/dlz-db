package com.dlz.db.core.abstractor;

import com.dlz.db.core.ITxExecutor;

import java.util.function.Supplier;

/**
 * 事务执行器抽象适配器。
 * <p>提供事务执行的默认实现，子类可选择性重写方法。</p>
 *
 * @since 7.0.0
 */
public abstract class ATxExecutorAdapter implements ITxExecutor {

    @Override
    public <T> T execute(Supplier<T> task) throws Exception {
        throw new UnsupportedOperationException("请实现此方法");
    }
}
