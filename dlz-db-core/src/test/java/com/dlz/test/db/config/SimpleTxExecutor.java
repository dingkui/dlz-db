package com.dlz.test.db.config;

import com.dlz.db.core.ITxExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 简单的事务执行器 - 基于 SQLite 的真实事务支持
 */
@Slf4j
public class SimpleTxExecutor implements ITxExecutor {

    private final DataSource dataSource;
    private static final ThreadLocal<Map<DataSource, Connection>> connectionHolder = new ThreadLocal<>();

    public SimpleTxExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T execute(Supplier<T> task) throws Exception {
        Map<DataSource, Connection> connections = connectionHolder.get();
        if (connections == null) {
            connections = new ConcurrentHashMap<>();
            connectionHolder.set(connections);
        }

        Connection conn = null;
        boolean isNewConnection = false;
        
        try {
            // 检查是否已有连接
            conn = connections.get(dataSource);
            if (conn == null) {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                connections.put(dataSource, conn);
                isNewConnection = true;
            }
            
            // 执行任务
            T result = task.get();
            
            // 如果是新连接，提交事务
            if (isNewConnection && !conn.isClosed()) {
                conn.commit();
            }
            
            return result;
        } catch (Exception e) {
            // 回滚事务
            if (conn != null && !conn.isClosed() && isNewConnection) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Error rolling back transaction", ex);
                }
            }
            throw e;
        } finally {
            // 清理资源
            if (isNewConnection && conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (SQLException e) {
                    log.error("Error closing connection", e);
                }
                connections.remove(dataSource);
            }
        }
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public boolean hasBinding(DataSource dataSource) {
        Map<DataSource, Connection> connections = connectionHolder.get();
        return connections != null && connections.containsKey(dataSource);
    }

    @Override
    public void bind(DataSource dataSource, Connection connection) {
        Map<DataSource, Connection> connections = connectionHolder.get();
        if (connections == null) {
            connections = new ConcurrentHashMap<>();
            connectionHolder.set(connections);
        }
        connections.put(dataSource, connection);
    }

    @Override
    public void unBind(DataSource dataSource) {
        Map<DataSource, Connection> connections = connectionHolder.get();
        if (connections != null) {
            connections.remove(dataSource);
        }
    }
}