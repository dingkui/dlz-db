package com.dlz.test.db.cases.multi_ds;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 多数据源专题测试
 * 覆盖动态数据源切换
 */
public class MultiDataSourceTest extends BaseDBTest {

    @BeforeEach
    public void setUp() {
        DB.jdbc.execute("DELETE FROM user");
        DB.jdbc.execute("DELETE FROM dyn_t");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, deleted  INTEGER DEFAULT 0)");
    }

    @AfterEach
    public void tearDown() {
        DB.jdbc.execute("DELETE FROM user");
        DB.jdbc.execute("DELETE FROM dyn_t");
    }

    @Test
    public void dynamic_use() {
        String ds = DB.ds.use("default", () -> {
             DB.jdbc.execute("CREATE TABLE IF NOT EXISTS dyn_t(id INTEGER PRIMARY KEY, val TEXT)");
            DB.jdbc.execute("INSERT INTO dyn_t(id,val) VALUES(?,?)", 1, "hello");
            return DB.ds.getUsedDataSourceName();
        });
        assertEquals("default", ds);
        assertEquals(1, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM dyn_t WHERE id=1").count());
        DB.jdbc.execute("DELETE FROM dyn_t");
    }

    @Test
    public void dynamic_getUsedDataSourceName() {
        assertNull(DB.ds.getUsedDataSourceName());
    }
}
