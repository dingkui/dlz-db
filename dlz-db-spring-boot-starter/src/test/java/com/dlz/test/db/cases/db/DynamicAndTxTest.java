package com.dlz.test.db.cases.db;

import com.dlz.db.ds.DBDynamic;
import com.dlz.db.ds.DBTx;
import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.modal.DB;
import com.dlz.test.db.config.SpingDbBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 测试 {@link DBDynamic#use} 与 {@link DBTx#run} 的拆分语义：
 * <ul>
 *   <li>{@code DB.Dynamic.use(name, ...)} —— 仅切换数据源，不开启事务</li>
 *   <li>{@code DB.Tx.run(() -> ...)} —— 默认数据源 + 事务</li>
 *   <li>{@code DB.Tx.run(name, () -> ...)} —— 切换数据源 + 事务</li>
 * </ul>
 * <p>使用 SQLite 文件数据库验证事务，因为 SQLite 支持 BEGIN/COMMIT/ROLLBACK。</p>
 */
@Slf4j
public class DynamicAndTxTest extends SpingDbBaseTest {

    private static final String TEST_DS_NAME = "tx_test_ds";
    private static final String DB_FILE = "./test/tx_test.sqlite3";

    @Before
    public void setup() {
        // 清理已有数据源
        try {
            DB.Dynamic.removeDataSource(TEST_DS_NAME);
        } catch (Exception ignore) {
        }

        // 注册 SQLite 文件数据源
        DataSourceProperty properties = new DataSourceProperty();
        properties.setName(TEST_DS_NAME);
        properties.setDriverClassName("org.sqlite.JDBC");
        properties.setUrl("jdbc:sqlite:" + DB_FILE);
        DB.Dynamic.setDataSource(properties);

        // 清空并重建表
        DB.Dynamic.use(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("DROP TABLE IF EXISTS tx_user");
            DB.Jdbc.execute("CREATE TABLE tx_user (id INTEGER PRIMARY KEY, name TEXT)");
        });
    }

    @After
    public void teardown() {
        DB.Dynamic.removeDataSource(TEST_DS_NAME);
    }

    // ============== DB.Dynamic.use ：纯切换 ==============

    @Test
    public void testDynamicUse_switchOnly() {
        DB.Dynamic.use(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("INSERT INTO tx_user (id, name) VALUES (?, ?)", 1, "alice");
        });

        int count = DB.Dynamic.use(TEST_DS_NAME, () ->
                DB.Jdbc.select("SELECT COUNT(*) FROM tx_user WHERE id = ?", 1).count()
        );
        assertEquals(1, count);
    }

    @Test
    public void testDynamicUse_dataSourceNotExist() {
        try {
            DB.Dynamic.use("not_exist", () -> DB.Jdbc.select("SELECT 1").queryList());
            fail("应抛出异常：数据源不存在");
        } catch (Exception e) {
            String msg = e.getMessage();
            assertTrue("异常信息应包含'数据源不存在'，实际: " + msg,
                    msg.contains("数据源不存在") || (e.getCause() != null && e.getCause().getMessage().contains("数据源不存在")));
        }
    }

    @Test
    public void testDynamicUse_nestedSwitch() {
        // 嵌套切换：内层切完应恢复到外层数据源
        DB.Dynamic.use(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("INSERT INTO tx_user (id, name) VALUES (?, ?)", 10, "outer");
            assertEquals(TEST_DS_NAME, DB.Dynamic.getUsedDataSourceName());

            // 内层切换到 default
            DB.Dynamic.use("default", () -> {
                assertEquals("default", DB.Dynamic.getUsedDataSourceName());
            });

            // 退出内层后应恢复到 TEST_DS_NAME
            assertEquals(TEST_DS_NAME, DB.Dynamic.getUsedDataSourceName());
        });
    }

    // ============== DB.Tx.run(name, ...) ：切换数据源 + 事务 ==============

    @Test
    public void testTxRun_commit() {
        DB.Tx.run(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("INSERT INTO tx_user (id, name) VALUES (?, ?)", 100, "committed");
        });

        int count = DB.Dynamic.use(TEST_DS_NAME, () ->
                DB.Jdbc.select("SELECT COUNT(*) FROM tx_user WHERE id = ?", 100).count()
        );
        assertEquals("事务提交后应能查到数据", 1, count);
    }

    @Test
    public void testTxRun_rollbackOnException() {
        Integer count1 = DB.Dynamic.use(TEST_DS_NAME, () ->
                DB.Jdbc.select("SELECT COUNT(*) FROM tx_user WHERE id = ?", 200).count()
        );
        assertEquals("异常应触发回滚，数据不应持久化", Integer.valueOf(0), count1);
        try {
            DB.Tx.run(TEST_DS_NAME, () -> {
                DB.Jdbc.execute("INSERT INTO tx_user (id, name) VALUES (?, ?)", 200, "rollback");
                throw new RuntimeException("模拟业务异常");
            });
            fail("应抛出异常");
        } catch (Exception e) {
            // 框架会包装为 DbException，原始消息保留在 message 中
            assertTrue("异常应包含 '模拟业务异常'，实际: " + e.getMessage(),
                    e.getMessage().contains("模拟业务异常"));
        }

        Integer count2 = DB.Dynamic.use(TEST_DS_NAME, () ->
            DB.Jdbc.select("SELECT COUNT(*) FROM tx_user WHERE id = ?", 200).count()
        );
        assertEquals("异常应触发回滚，数据不应持久化", Integer.valueOf(0), count2);
    }

    @Test
    public void testTxRun_supplierReturnValue() {
        Integer result = DB.Tx.run(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("INSERT INTO tx_user (id, name) VALUES (?, ?)", 300, "ret");
            return 42;
        });
        assertEquals(Integer.valueOf(42), result);

        int count = DB.Dynamic.use(TEST_DS_NAME, () ->
                DB.Jdbc.select("SELECT COUNT(*) FROM tx_user WHERE id = ?", 300).count()
        );
        assertEquals(1, count);
    }

    // ============== DB.Tx.run(...) ：当前/默认数据源事务 ==============

    @Test
    public void testTxRun_inDynamicUse_useCurrentDataSource() {
        // 在 Dynamic.use 切换后调用 Tx.run()，应在切换后的数据源上开事务
        DB.Dynamic.use(TEST_DS_NAME, () -> {
            DB.Tx.run(() -> {
                DB.Jdbc.execute("INSERT INTO tx_user (id, name) VALUES (?, ?)", 400, "nested_tx");
            });
        });

        int count = DB.Dynamic.use(TEST_DS_NAME, () ->
            DB.Jdbc.select("SELECT COUNT(*) FROM tx_user WHERE id = ?", 400).count()
        );
        assertEquals(1, count);
    }

    @Test
    public void testTxRun_runnableOverload() {
        AtomicInteger executed = new AtomicInteger(0);
        DB.Tx.run(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("INSERT INTO tx_user (id, name) VALUES (?, ?)", 500, "runnable");
            executed.incrementAndGet();
        });
        assertEquals(1, executed.get());
    }
}
