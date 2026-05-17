package com.dlz.db.spring;

import com.dlz.db.core.ITxExecutor;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.exception.DbException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Spring 事务执行器实现。
 * <p>基于 Spring {@link TransactionSynchronizationManager} 将 Connection 绑定到事务上下文，
 * 使 JdbcTemplate 在执行期间复用同一连接，从而保证事务语义。</p>
 *
 * <p>事务传播：仅支持 REQUIRED（默认）</p>
 * <p>异常处理：任何异常都会触发回滚</p>
 */
@Slf4j
public class SpringTxExecutorAdapter implements ITxExecutor {

    private final DataSourceConfig config;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "DataSourceConfig由容器注入，视为不可变")
    public SpringTxExecutorAdapter(DataSourceConfig config) {
        this.config = config;
    }

//    @Override
//    public <T> T execute(Supplier<T> task) throws Exception {
//        DataSource dataSource = config.getDataSource();
//        Connection connection = null;
//        boolean bound = false;
//        try {
//            connection = dataSource.getConnection();
//            connection.setAutoCommit(false);
//
//            // 绑定到 Spring 事务管理器，使 JdbcTemplate 复用同一连接
//            ConnectionHolder connectionHolder = new ConnectionHolder(connection);
//            TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
//            bound = true;
//
//            T result = task.get();
//            connection.commit();
//            return result;
//        } catch (Exception e) {
//            if (connection != null) {
//                try {
//                    connection.rollback();
//                } catch (SQLException ex) {
//                    log.error("连接回滚失败", ex);
//                    throw new DbException("连接回滚失败", 1006, ex);
//                }
//            }
//            if (e instanceof DbException) {
//                throw new DbException("事务执行失败：" + e.getMessage(), 1006, e);
//            }
//            throw new DbException("事务执行失败：" + e.getMessage(), 1003, e);
//        } finally {
//            if (bound) {
//                try {
//                    TransactionSynchronizationManager.unbindResource(dataSource);
//                } catch (Exception e) {
//                    log.error("解绑事务资源失败", e);
//                }
//            }
//            if (connection != null) {
//                try {
//                    connection.setAutoCommit(true);
//                    connection.close();
//                } catch (Exception e) {
//                    log.error("关闭连接失败", e);
//                }
//            }
//        }
//    }

    @Override
    public DataSource getDataSource() {
        return config.getDataSource();
    }

    @Override
    public boolean hasBinding(DataSource dataSource) {
        return
                TransactionSynchronizationManager.hasResource(dataSource);
    }

    @Override
    public void bind(DataSource dataSource, Connection connection) {
        // 绑定到 Spring 事务管理器，使 JdbcTemplate 与 @Transactional 复用同一连接。
        // 关键：transactionActive=true 让 DataSourceTransactionManager.isExistingTransaction()
        // 识别为已有事务，PROPAGATION_REQUIRED 时内层 @Transactional 会加入外层事务。
        // setTransactionActive 是 protected，通过匿名子类暴露
        ConnectionHolder connectionHolder = new ConnectionHolder(connection) {{
            setTransactionActive(true);
        }};
        connectionHolder.setSynchronizedWithTransaction(true);
        TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
        }
    }

    @Override
    public void unBind(DataSource dataSource) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}
