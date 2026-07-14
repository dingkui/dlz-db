package com.dlz.test.db.cases.core;

import com.dlz.db.core.DlzConnectionHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DlzConnectionHolder 连接持有者测试")
class DlzConnectionHolderTest {

    private static DataSource stubDs(String name) {
        return new DataSource() {
            @Override public String toString() { return "DS:" + name; }
            @Override public Connection getConnection() { return null; }
            @Override public Connection getConnection(String u, String p) { return null; }
            @Override public PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(PrintWriter out) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { return null; }
            @Override public <T> T unwrap(Class<T> iface) { return null; }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        };
    }

    private static Connection stubConn(String name) {
        return new StubConnection(name);
    }

    @Test
    @DisplayName("bind/get - 绑定后可获取连接")
    void testBindAndGet() {
        DataSource ds = stubDs("test");
        Connection conn = stubConn("conn1");

        DlzConnectionHolder.bind(ds, conn);
        assertSame(conn, DlzConnectionHolder.get(ds));
        DlzConnectionHolder.unbind(ds);
    }

    @Test
    @DisplayName("get - 未绑定返回null")
    void testGetUnbound() {
        DataSource ds = stubDs("unbound");
        assertNull(DlzConnectionHolder.get(ds));
    }

    @Test
    @DisplayName("unbind - 解绑返回之前绑定的连接")
    void testUnbind() {
        DataSource ds = stubDs("unbind");
        Connection conn = stubConn("conn2");

        DlzConnectionHolder.bind(ds, conn);
        Connection unbound = DlzConnectionHolder.unbind(ds);
        assertSame(conn, unbound);
        assertNull(DlzConnectionHolder.get(ds));
    }

    @Test
    @DisplayName("hasBinding - 检测绑定状态")
    void testHasBinding() {
        DataSource ds = stubDs("binding");
        Connection conn = stubConn("conn3");

        assertFalse(DlzConnectionHolder.hasBinding(ds));
        DlzConnectionHolder.bind(ds, conn);
        assertTrue(DlzConnectionHolder.hasBinding(ds));
        DlzConnectionHolder.unbind(ds);
        assertFalse(DlzConnectionHolder.hasBinding(ds));
    }

    @Test
    @DisplayName("多数据源独立绑定")
    void testMultipleDataSources() {
        DataSource ds1 = stubDs("ds1");
        DataSource ds2 = stubDs("ds2");
        Connection conn1 = stubConn("conn1");
        Connection conn2 = stubConn("conn2");

        DlzConnectionHolder.bind(ds1, conn1);
        DlzConnectionHolder.bind(ds2, conn2);

        assertSame(conn1, DlzConnectionHolder.get(ds1));
        assertSame(conn2, DlzConnectionHolder.get(ds2));

        DlzConnectionHolder.unbind(ds1);
        assertNull(DlzConnectionHolder.get(ds1));
        assertSame(conn2, DlzConnectionHolder.get(ds2));

        DlzConnectionHolder.unbind(ds2);
    }

    /**
     * Minimal Connection stub for testing
     */
    static class StubConnection implements Connection {
        final String name;
        StubConnection(String name) { this.name = name; }
        @Override public String toString() { return "Conn:" + name; }

        // All other methods throw UnsupportedOperationException
        @Override public java.sql.Statement createStatement() { throw new UnsupportedOperationException(); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql) { throw new UnsupportedOperationException(); }
        @Override public java.sql.CallableStatement prepareCall(String sql) { throw new UnsupportedOperationException(); }
        @Override public String nativeSQL(String sql) { throw new UnsupportedOperationException(); }
        @Override public void setAutoCommit(boolean autoCommit) {}
        @Override public boolean getAutoCommit() { return true; }
        @Override public void commit() {}
        @Override public void rollback() {}
        @Override public void close() {}
        @Override public boolean isClosed() { return false; }
        @Override public java.sql.DatabaseMetaData getMetaData() { return null; }
        @Override public void setReadOnly(boolean readOnly) {}
        @Override public boolean isReadOnly() { return false; }
        @Override public void setCatalog(String catalog) {}
        @Override public String getCatalog() { return null; }
        @Override public void setTransactionIsolation(int level) {}
        @Override public int getTransactionIsolation() { return Connection.TRANSACTION_NONE; }
        @Override public java.sql.SQLWarning getWarnings() { return null; }
        @Override public void clearWarnings() {}
        @Override public java.sql.Statement createStatement(int a, int b) { throw new UnsupportedOperationException(); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int a, int b) { throw new UnsupportedOperationException(); }
        @Override public java.sql.CallableStatement prepareCall(String sql, int a, int b) { throw new UnsupportedOperationException(); }
        @Override public java.util.Map<String, Class<?>> getTypeMap() { return null; }
        @Override public void setTypeMap(java.util.Map<String, Class<?>> map) {}
        @Override public void setHoldability(int holdability) {}
        @Override public int getHoldability() { return 0; }
        @Override public java.sql.Savepoint setSavepoint() { return null; }
        @Override public java.sql.Savepoint setSavepoint(String name) { return null; }
        @Override public void rollback(java.sql.Savepoint savepoint) {}
        @Override public void releaseSavepoint(java.sql.Savepoint savepoint) {}
        @Override public java.sql.Statement createStatement(int a, int b, int c) { throw new UnsupportedOperationException(); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int a, int b, int c) { throw new UnsupportedOperationException(); }
        @Override public java.sql.CallableStatement prepareCall(String sql, int a, int b, int c) { throw new UnsupportedOperationException(); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) { throw new UnsupportedOperationException(); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) { throw new UnsupportedOperationException(); }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) { throw new UnsupportedOperationException(); }
        @Override public java.sql.Clob createClob() { return null; }
        @Override public java.sql.Blob createBlob() { return null; }
        @Override public java.sql.NClob createNClob() { return null; }
        @Override public java.sql.SQLXML createSQLXML() { return null; }
        @Override public boolean isValid(int timeout) { return true; }
        @Override public void setClientInfo(String name, String value) {}
        @Override public void setClientInfo(java.util.Properties properties) {}
        @Override public String getClientInfo(String name) { return null; }
        @Override public java.util.Properties getClientInfo() { return null; }
        @Override public java.sql.Array createArrayOf(String typeName, Object[] elements) { return null; }
        @Override public java.sql.Struct createStruct(String typeName, Object[] attributes) { return null; }
        @Override public void setSchema(String schema) {}
        @Override public String getSchema() { return null; }
        @Override public void abort(java.util.concurrent.Executor executor) {}
        @Override public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) {}
        @Override public int getNetworkTimeout() { return 0; }
        @Override public <T> T unwrap(Class<T> iface) { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    }
}
