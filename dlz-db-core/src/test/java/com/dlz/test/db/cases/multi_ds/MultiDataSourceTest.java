package com.dlz.test.db.cases.multi_ds;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.After;
import org.junit.Before;
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
        DB.Jdbc.execute("DELETE FROM user");
        DB.Jdbc.execute("DELETE FROM dyn_t");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, deleted  INTEGER DEFAULT 0)");
    }

    @AfterEach
    public void tearDown() {
        DB.Jdbc.execute("DELETE FROM user");
        DB.Jdbc.execute("DELETE FROM dyn_t");
    }

    @Test
    public void dynamic_use() {
        String ds = DB.Dynamic.use("default", () -> {
             DB.Jdbc.execute("CREATE TABLE IF NOT EXISTS dyn_t(id INTEGER PRIMARY KEY, val TEXT)");
            DB.Jdbc.execute("INSERT INTO dyn_t(id,val) VALUES(?,?)", 1, "hello");
            return DB.Dynamic.getUsedDataSourceName();
        });
        assertEquals("default", ds);
        assertEquals(1, DB.Jdbc.select("SELECT COUNT(*) FROM dyn_t WHERE id=1").count());
        DB.Jdbc.execute("DELETE FROM dyn_t");
    }

    @Test
    public void dynamic_getUsedDataSourceName() {
        assertNull(DB.Dynamic.getUsedDataSourceName());
    }
}
