package com.dlz.test.db.cases.multi_ds;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 多数据源专题测试
 * 覆盖动态数据源切换
 */
public class MultiDataSourceTest extends BaseDBTest {

    @Before
    public void setUp() {
        DB.Jdbc.execute("delete from user");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, is_deleted INTEGER DEFAULT 0)");
    }

    @After
    public void tearDown() {
        DB.Jdbc.execute("delete from user");
        DB.Jdbc.execute("delete from dyn_t");
    }

    @Test
    public void dynamic_use() {
        String ds = DB.Dynamic.use("default", () -> {
            // DB.Jdbc.execute("CREATE TABLE IF NOT EXISTS dyn_t(id INTEGER PRIMARY KEY, val TEXT)");
            DB.Jdbc.update("INSERT INTO dyn_t(id,val) VALUES(?,?)", 1, "hello");
            return DB.Dynamic.getUsedDataSourceName();
        });
        assertEquals("default", ds);
        assertEquals(1, DB.Jdbc.select("SELECT COUNT(*) FROM dyn_t WHERE id=1").count());
        DB.Jdbc.execute("delete from dyn_t");
    }

    @Test
    public void dynamic_getUsedDataSourceName() {
        assertNull(DB.Dynamic.getUsedDataSourceName());
    }
}
