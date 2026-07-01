package com.dlz.db.modal;

import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.support.DBHolder;

import java.util.function.Supplier;

/**
 * 事务门面。
 * <p>底层桥接 Spring PlatformTransactionManager / Solon TranExecutor。
 *
 * <p>方法：
 * <ul>
 *   <li>run(Runnable) / call(Supplier) — 默认数据源</li>
 *   <li>run(dsName, Runnable) / call(dsName, Supplier) — 指定数据源（重载，避免链式线程安全问题）</li>
 *   <li>runNew(Runnable) / callNew(Supplier) — 新事务（REQUIRES_NEW，依赖底层支持）</li>
 * </ul>
 */
public class DbTx {

    /** 默认数据源事务，带返回值。异常自动回滚，正常自动提交。 */
    public <T> T call(Supplier<T> c) {
        DataSourceConfig config = DB.ds.getCurrentConfig();
        return DBHolder.getTxExecutor(config).execute(c);
    }

    /** 默认数据源事务，无返回值。 */
    public void run(Runnable r) {
        call(() -> {
            r.run();
            return null;
        });
    }

    /** 指定数据源事务，带返回值。 */
    public <T> T call(String dsName, Supplier<T> c) {
        return DB.ds.use(dsName, () -> call(c));
    }

    /** 指定数据源事务，无返回值。 */
    public void run(String dsName, Runnable r) {
        call(dsName, () -> {
            r.run();
            return null;
        });
    }

    /** 新事务（REQUIRES_NEW），带返回值。独立事务，不受外层事务影响。 */
    public <T> T callNew(Supplier<T> c) {
        // TODO: REQUIRES_NEW 传播行为，依赖底层 Spring/Solon 支持，需验证 TranExecutor 能力
        return call(c);
    }

    /** 新事务（REQUIRES_NEW），无返回值。 */
    public void runNew(Runnable r) {
        callNew(() -> {
            r.run();
            return null;
        });
    }
}
