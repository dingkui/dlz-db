package com.dlz.db.spring;

import com.dlz.db.core.ITxExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;

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

    private final DataSource dataSource;

    public SpringTxExecutorAdapter(DataSource config) {
        this.dataSource = config;
    }
    @Override
    public DataSource getDataSource() {
        return dataSource;
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
