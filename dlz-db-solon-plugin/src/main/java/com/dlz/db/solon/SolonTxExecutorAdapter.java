package com.dlz.db.solon;

import com.dlz.db.core.ITxExecutor;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.exception.DbException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * {@link ITxExecutor} 的 Solon 实现：基于原生 JDBC + {@link SolonConnectionHolder}。
 *
 * <h3>嵌套事务策略</h3>
 * <ul>
 *   <li>同一线程同一 {@link DataSource} 已存在事务时，复用外层连接（加入外层事务），
 *       内层提交/回滚由外层负责。</li>
 *   <li>切换数据源时（嵌套 {@code DB.Dynamic.use(name, ...)}），对新数据源重新开启独立事务。</li>
 * </ul>
 *
 * <h3>异常策略</h3>
 * <p>任何异常触发回滚（最外层事务）。</p>
 *
 * @since 7.0.0
 */
@Slf4j
public class SolonTxExecutorAdapter implements ITxExecutor {

    private final DataSourceConfig config;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "DataSourceConfig由容器注入，视为不可变")
    public SolonTxExecutorAdapter(DataSourceConfig config) {
        this.config = config;
    }

    @Override
    public <T> T execute(Supplier<T> task) throws Exception {
        DataSource dataSource = config.getDataSource();
        // 已在事务中：直接复用外层连接，不重复 begin/commit
        if (SolonConnectionHolder.hasBinding(dataSource)) {
            return runInExisting(task);
        }
        return runNewTransaction(dataSource, task);
    }

    private <T> T runInExisting(Supplier<T> task) {
        try {
            return task.get();
        } catch (DbException e) {
            throw e;
        } catch (Exception e) {
            throw new DbException("事务执行失败：" + e.getMessage(), 1003, e);
        }
    }

    private <T> T runNewTransaction(DataSource dataSource, Supplier<T> task) throws Exception {
        Connection connection = null;
        boolean bound = false;
        boolean prevAutoCommit = true;
        try {
            connection = dataSource.getConnection();
            prevAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            SolonConnectionHolder.bind(dataSource, connection);
            bound = true;

            T result = task.get();
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
                SolonConnectionHolder.unbind(dataSource);
            }
            if (connection != null) {
                try {
                    connection.setAutoCommit(prevAutoCommit);
                } catch (Exception e) {
                    log.warn("还原 autoCommit 失败", e);
                }
                try {
                    connection.close();
                } catch (Exception e) {
                    log.error("关闭连接失败", e);
                }
            }
        }
    }
}
