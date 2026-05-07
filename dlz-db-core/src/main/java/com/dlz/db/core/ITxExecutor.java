package com.dlz.db.core;

import java.util.function.Supplier;

/**
 * 事务执行器接口。
 * <p>抽象事务执行逻辑，支持不同框架的事务实现（Spring、Solon 等）。</p>
 *
 * <p>事务传播级别：仅支持 REQUIRED（默认）</p>
 * <p>异常处理：任何异常都会触发回滚</p>
 */
@FunctionalInterface
public interface ITxExecutor {

    /**
     * 在事务中执行任务。
     * <p>如果执行过程中抛出异常，事务将回滚；否则提交。</p>
     *
     * @param task 要执行的任务
     * @param <T>  返回值类型
     * @return 任务执行结果
     * @throws Exception 执行失败或事务异常
     */
    <T> T execute(Supplier<T> task) throws Exception;

    /**
     * 在事务中执行无返回值的任务。
     *
     * @param task 要执行的任务
     * @throws Exception 执行失败或事务异常
     */
    default void execute(Runnable task) throws Exception {
        execute(() -> {
            task.run();
            return null;
        });
    }
}
