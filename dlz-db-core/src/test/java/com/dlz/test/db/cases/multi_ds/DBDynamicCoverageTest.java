package com.dlz.test.db.cases.multi_ds;

import com.dlz.db.ds.DBDynamic;
import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.enums.DbTypeEnum;
import com.dlz.db.exception.DbException;
import com.dlz.kit.exception.SystemException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DBDynamicCoverageTest {
    @Test
    void registerSwitchRestoreAndReadCurrentDataSource() {
        DBDynamic dynamic = new DBDynamic();
        assertFalse(dynamic.setDefaultDataSource(null));
        assertThrows(SystemException.class, dynamic::getCurrentConfig);
        assertThrows(SystemException.class, () -> dynamic.use("missing", () -> null));

        DataSource defaultSource = metadataDataSource();
        assertTrue(dynamic.setDefaultDataSource(defaultSource));
        assertEquals(Collections.singleton("default"), dynamic.getAllDataSourceNames());
        assertSame(defaultSource, dynamic.getDataSource());
        assertEquals(DbTypeEnum.SQLITE, dynamic.getDbType());
        assertNotNull(dynamic.getRowMapper());
        assertNotNull(dynamic.getSqlHelper());
        assertNull(dynamic.getUsedDataSourceName());
        assertEquals("default", dynamic.getDataSourceProperty("default").getName());

        DataSourceProperty property = property("custom", "jdbc:sqlite::memory:");
        assertTrue(dynamic.setDataSource(property));
        assertEquals("custom", dynamic.getDataSourceProperty("custom").getName());
        assertEquals("custom-result", dynamic.use("custom", () -> {
            assertEquals("custom", dynamic.getUsedDataSourceName());
            return "custom-result";
        }));
        assertNull(dynamic.getUsedDataSourceName());
        dynamic.use("custom", () -> assertEquals("custom", dynamic.getUsedDataSourceName()));
        assertNull(dynamic.getUsedDataSourceName());
        assertThrows(SystemException.class, () -> dynamic.getDataSourceProperty("missing"));
    }

    @Test
    void nestedUseAndExceptionHandlingRestorePreviousConfig() {
        DBDynamic dynamic = new DBDynamic();
        dynamic.setDataSource(property("one", "jdbc:sqlite::memory:"));
        dynamic.setDataSource(property("two", "jdbc:sqlite::memory:"));

        dynamic.use("one", () -> {
            assertEquals("one", dynamic.getUsedDataSourceName());
            dynamic.use("two", () -> assertEquals("two", dynamic.getUsedDataSourceName()));
            assertEquals("one", dynamic.getUsedDataSourceName());
            return null;
        });
        assertNull(dynamic.getUsedDataSourceName());

        DbException dbException = new DbException("db", 1001);
        assertSame(dbException, assertThrows(DbException.class, () -> dynamic.use("one", () -> {
            throw dbException;
        })));
        assertNull(dynamic.getUsedDataSourceName());
        DbException wrapped = assertThrows(DbException.class, () -> dynamic.use("one", () -> {
            throw new IllegalStateException("boom");
        }));
        assertTrue(wrapped.getMessage().contains("boom"));
        assertNull(dynamic.getUsedDataSourceName());

        assertThrows(SystemException.class, () -> dynamic.use("missing", (Runnable) () -> { }));
    }

    @Test
    void dataSourceRegistrationRemovalAndConnectionTesting() {
        DBDynamic dynamic = new DBDynamic();
        assertThrows(SystemException.class, () -> dynamic.setDataSource(null));
        DataSourceProperty defaultProperty = property("", "jdbc:sqlite::memory:");
        assertTrue(dynamic.setDataSource(defaultProperty));
        assertTrue(dynamic.getAllDataSourceNames().contains("default"));
        assertTrue(dynamic.removeDataSource("default"));
        assertFalse(dynamic.removeDataSource("default"));

        DataSourceProperty sqlite = property("connection-test", "jdbc:sqlite::memory:");
        sqlite.setDriverClassName("org.sqlite.JDBC");
        sqlite.setTestQuery("SELECT 1");
        assertDoesNotThrow(() -> dynamic.testConnection(sqlite));
        DataSourceProperty defaultQuery = property("connection-test-2", "jdbc:sqlite::memory:");
        defaultQuery.setDriverClassName("org.sqlite.JDBC");
        assertDoesNotThrow(() -> dynamic.testConnection(defaultQuery));
        assertThrows(SystemException.class, () -> dynamic.testConnection(null));
        DataSourceProperty missingUrl = property("bad", "");
        assertThrows(SystemException.class, () -> dynamic.testConnection(missingUrl));
        DataSourceProperty badUrl = property("bad", "jdbc:unknown:invalid");
        assertThrows(DbException.class, () -> dynamic.testConnection(badUrl));

        DataSource closeFailing = metadataDataSource(true);
        assertTrue(dynamic.setDefaultDataSource(closeFailing));
        assertFalse(dynamic.removeDataSource("default"));

        DataSource getConnectionFailing = (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(), new Class<?>[]{DataSource.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getConnection")) throw new RuntimeException("connection failed");
                    return defaultValue(method.getReturnType());
                });
        assertTrue(dynamic.setDefaultDataSource(getConnectionFailing));
    }

    private static DataSourceProperty property(String name, String url) {
        DataSourceProperty property = new DataSourceProperty();
        property.setName(name);
        property.setUrl(url);
        return property;
    }

    private static DataSource metadataDataSource() {
        return metadataDataSource(false);
    }

    private static DataSource metadataDataSource(boolean closeFails) {
        DatabaseMetaData metadata = (DatabaseMetaData) Proxy.newProxyInstance(
                DatabaseMetaData.class.getClassLoader(), new Class<?>[]{DatabaseMetaData.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getDriverName")) return "SQLite JDBC";
                    if (method.getName().equals("getURL")) return "jdbc:sqlite::memory:";
                    if (method.getName().equals("getUserName")) return "";
                    if (method.getName().equals("getDatabaseProductName")) return "SQLite";
                    return defaultValue(method.getReturnType());
                });
        Connection connection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getMetaData")) return metadata;
                    if (method.getName().equals("close") && closeFails) throw new RuntimeException("close failed");
                    return defaultValue(method.getReturnType());
                });
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(), new Class<?>[]{DataSource.class, AutoCloseable.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getConnection")) return connection;
                    if (method.getName().equals("close") && closeFails) throw new RuntimeException("close failed");
                    return defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return (char) 0;
        return null;
    }
}
