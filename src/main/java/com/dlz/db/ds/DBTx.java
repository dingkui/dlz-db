package com.dlz.db.ds;

import com.dlz.db.modal.DB;
import com.dlz.kit.exception.DbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * 事务执行器。
 * <p>基于 Spring {@link TransactionSynchronizationManager} 将 Connection 绑定到事务上下文，
 * 使 JdbcTemplate 在执行期间复用同一连接，从而保证事务语义。</p>
 *
 * <p>典型用法：</p>
 * <pre>
 * DB.Tx.run(() -&gt; { ... });           // 默认数据源 + 事务
 * DB.Tx.run("slave", () -&gt; { ... });  // 切换到 slave + 事务
 * </pre>
 *
 * <p>本类不处理 Spring 的 {@code @Transactional} 传播行为；如需嵌套事务、传播控制，请使用 Spring 自身机制。</p>
 */
@Slf4j
public class DBTx {

    /**
     * 在当前线程的数据源上开启事务执行。
     * <p>当前线程数据源由 {@link DBDynamic#use} 决定，未切换时使用默认数据源。</p>
     */
    public <T> T run(Supplier<T> c) {
        DataSourceConfig config = DB.Dynamic.getCurrentConfig();
        return doRun(config, c);
    }

    /**
     * 在当前线程的数据源上开启事务执行（无返回值版本）。
     */
    public void run(Runnable r) {
        run(() -> {
            r.run();
            return null;
        });
    }

    /**
     * 切换到指定数据源并在其上开启事务执行。
     * <p>等价于 {@code DB.Dynamic.use(name, () -> DB.Tx.run(c))}。</p>
     */
    public <T> T run(String name, Supplier<T> c) {
        return DB.Dynamic.use(name, () -> {
            DataSourceConfig config = DB.Dynamic.getCurrentConfig();
            return doRun(config, c);
        });
    }

    /**
     * 切换到指定数据源并在其上开启事务执行（无返回值版本）。
     */
    public void run(String name, Runnable r) {
        run(name, () -> {
            r.run();
            return null;
        });
    }

    /**
     * 事务核心执行逻辑。
     */
    private <T> T doRun(DataSourceConfig config, Supplier<T> c) {
        DataSource dataSource = config.getDataSource();
        Connection connection = null;
        boolean bound = false;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            // 绑定到 Spring 事务管理器，使 JdbcTemplate 复用同一连接
            ConnectionHolder connectionHolder = new ConnectionHolder(connection);
            TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
            bound = true;

            T result = c.get();
            connection.commit();
            return result;
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("连接回滚失败", ex);
                    throw new DbException("连接回滚失败", 1006, ex);
                }
            }
            if (e instanceof DbException) {
                throw new DbException("事务执行失败：" + e.getMessage(), 1006, e);
            }
            throw new DbException("事务执行失败：" + e.getMessage(), 1003, e);
        } finally {
            if (bound) {
                try {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                } catch (Exception e) {
                    log.error("解绑事务资源失败", e);
                }
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (Exception e) {
                    log.error("关闭连接失败", e);
                }
            }
        }
    }
}
