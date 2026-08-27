package com.dlz.test.db.cases.solon;

import com.dlz.db.DB;
import com.dlz.db.solon.DynamicDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DynamicDataSourceTest {
    @Test
    void delegatesEveryDataSourceOperationToDefaultAndCurrentDataSource() throws Exception {
        AtomicReference<String> calls = new AtomicReference<>();
        Connection connection = (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> null);
        DataSource initial = dataSource("initial", connection, calls);
        DynamicDataSource dynamic = new DynamicDataSource(initial);

        assertSame(connection, dynamic.getConnection());
        assertSame(connection, dynamic.getConnection("user", "password"));
        PrintWriter writer = new PrintWriter(System.out);
        dynamic.setLogWriter(writer);
        assertSame(writer, dynamic.getLogWriter());
        dynamic.setLoginTimeout(7);
        assertEquals(7, dynamic.getLoginTimeout());
        assertTrue(dynamic.isWrapperFor(DataSource.class));
        assertSame(initial, dynamic.unwrap(DataSource.class));

        DataSource replacement = dataSource("replacement", connection, calls);
        DB.ds.setDefaultDataSource(replacement);
        assertSame(replacement, dynamic.unwrap(DataSource.class));
        assertEquals("replacement:unwrap", calls.get());
    }

    private DataSource dataSource(String name, Connection connection, AtomicReference<String> calls) {
        AtomicReference<PrintWriter> writer = new AtomicReference<>();
        AtomicReference<Integer> timeout = new AtomicReference<>(0);
        return (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{DataSource.class}, (p, method, args) -> {
            calls.set(name + ":" + method.getName());
            switch (method.getName()) {
                case "getConnection": return connection;
                case "getLogWriter": return writer.get();
                case "setLogWriter": writer.set((PrintWriter) args[0]); return null;
                case "getLoginTimeout": return timeout.get();
                case "setLoginTimeout": timeout.set((Integer) args[0]); return null;
                case "isWrapperFor": return args[0] == DataSource.class;
                case "unwrap": return args[0] == DataSource.class ? p : null;
                case "getParentLogger": return java.util.logging.Logger.getGlobal();
                case "toString": return name;
                default: return null;
            }
        });
    }
}
