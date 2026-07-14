package com.dlz.db.solon;

import com.dlz.db.modal.DB;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * 动态数据源包装器
 * 支持根据线程上下文自动选择数据源（类似 Spring 的 DynamicJdbcTemplate）
 *
 * <p>使用方式：将此类的实例注册为 Solon 的 DataSource Bean，
 * 所有使用此 DataSource 的地方都会自动从 DB.Dynamic 获取数据源。</p>
 *
 * @since 7.0.0
 */
@Slf4j
public class DynamicDataSource implements DataSource {

    private final DataSource defaultDataSource;

    public DynamicDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        DB.ds.setDefaultDataSource(defaultDataSource);
        log.info("初始化 DynamicDataSource，默认数据源: {}", defaultDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getTargetDataSource().getConnection(username, password);
    }

    @Override
    public java.io.PrintWriter getLogWriter() throws SQLException {
        return getTargetDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        getTargetDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getTargetDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getTargetDataSource().getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getTargetDataSource().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getTargetDataSource().isWrapperFor(iface);
    }

    /**
     * 从 DB.Dynamic 获取当前线程的目标数据源
     */
    private DataSource getTargetDataSource() {
        DataSource dataSource = DB.ds.getDataSource();
        if (dataSource == null || dataSource == this) {
            return defaultDataSource;
        }
        return dataSource;
    }
}
