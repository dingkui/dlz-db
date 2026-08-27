package com.dlz.db.spring;

import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.internal.anno.ConnectionSupplier;
import com.dlz.db.core.jdbc.JdbcSqlExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

/**
 * {@link ISqlExecutor} 的 Spring JDBC 实现。
 * <p>基于 {@link JdbcTemplate}，继承 Spring 事务上下文（{@code @Transactional} 自然生效）。</p>
 * <p>v7.0 起，原 {@code com.dlz.db.dao.DlzDao} 已被此类取代。</p>
 */
@Slf4j
public class SpringSqlExecutorAdapter extends JdbcSqlExecutor {
    private final JdbcTemplate dao;

    @Override
    public ConnectionSupplier getConnectionSupplier() {
        return () -> {
            DataSource ds = dao.getDataSource();

            // 1. 优先复用 dlz-db 自身事务连接（DB.Tx.run）
            if (TransactionSynchronizationManager.hasResource(ds)) {
                ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(ds);
                return wrapNoClose(conHolder.getConnection());
            }

            log.debug("获取新连接");
            return ds.getConnection();
        };
    }
    public SpringSqlExecutorAdapter(JdbcTemplate jdbcTemplate) {
        this.dao = jdbcTemplate;
    }
}
