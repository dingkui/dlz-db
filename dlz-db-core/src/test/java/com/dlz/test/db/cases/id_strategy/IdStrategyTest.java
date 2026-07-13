package com.dlz.test.db.cases.id_strategy;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.AutoIdEntity;
import com.dlz.test.db.entity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * 主键策略专题测试
 * 覆盖 AUTO/ASSIGN_ID/INPUT 三种主键策略
 */
public class IdStrategyTest extends BaseDBTest {

    @Before
    public void setUp() {
        DB.Jdbc.execute("DELETE FROM user");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, deleted  INTEGER DEFAULT 0)");
        DB.Jdbc.execute("DELETE FROM test_auto_id");
        // DB.Jdbc.execute("CREATE TABLE test_auto_id (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
    }

    @After
    public void tearDown() {
        DB.Jdbc.execute("DELETE FROM user");
        DB.Jdbc.execute("DELETE FROM test_auto_id");
    }

    @Test
    public void pojo_insertAutoId() {
        AutoIdEntity e = new AutoIdEntity();
        e.setName("auto_test");
        assertNull(e.getId());
        DB.Pojo.add(e);
        assertNotNull(e.getId());
        assertTrue(e.getId() > 0);
    }

    @Test
    public void pojo_insert_with_auto_backfill() {
        User u = new User();
        u.setName("eve");
        u.setAge(22);
        u.setStatus("1");
        u.setDeleted("0");
        DB.Pojo.add(u);
        assertNotNull(u.getId());
        assertTrue(u.getId() > 0);
    }

    @Test
    public void batch_autoNotBackfill() {
        AutoIdEntity m1 = new AutoIdEntity();
        m1.setName("ba1");
        AutoIdEntity m2 = new AutoIdEntity();
        m2.setName("ba2");
        DB.Batch.pojoInsert(Arrays.asList(m1, m2), 100);
        assertNull("AUTO batch 不应回填", m1.getId());
        assertNull("AUTO batch 不应回填", m2.getId());
    }

    @Test
    public void pojo_insert_assignId() {
        User u = new User();
        u.setId(999L);
        u.setName("assign_test");
        u.setAge(40);
        u.setStatus("1");
        u.setDeleted("0");
        DB.Pojo.add(u);
        assertEquals(Long.valueOf(999L), u.getId());
        assertEquals(Long.valueOf(999L), DB.Pojo.selectById(User.class, 999L).getId());
    }
}
