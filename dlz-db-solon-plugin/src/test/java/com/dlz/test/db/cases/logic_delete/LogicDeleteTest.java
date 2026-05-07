package com.dlz.test.db.cases.logic_delete;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 逻辑删除专题测试
 * 覆盖软删除/恢复/查询过滤
 */
public class LogicDeleteTest extends SpingDbBaseTest {

    @Before
    public void setUp() {
        DB.Jdbc.execute("delete from user");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, is_deleted INTEGER DEFAULT 0)");
        DB.Jdbc.execute("INSERT INTO user(name,age,status,is_deleted) VALUES(?,?,?,?)", "alice", 25, "1", 0);
        DB.Jdbc.execute("INSERT INTO user(name,age,status,is_deleted) VALUES(?,?,?,?)", "bob", 30, "1", 0);
        DB.Jdbc.execute("INSERT INTO user(name,age,status,is_deleted) VALUES(?,?,?,?)", "deleted_user", 35, "0", 1);
    }

    @After
    public void tearDown() {
        DB.Jdbc.execute("delete from user");
    }

    @Test
    public void query_without_deleted() {
        assertEquals(2, DB.Pojo.select(User.class).eq(User::getIsDeleted, "0").count());
        assertEquals(2, DB.Pojo.select(User.class).count());
    }

    @Test
    public void query_with_deleted() {
        assertEquals(1, DB.Pojo.select(User.class).eq(User::getIsDeleted, "1").count());
    }

    @Test
    public void soft_delete_by_update() {
        User u = DB.Pojo.select(User.class).eq(User::getName, "alice").queryBean();
        DB.Pojo.update(User.class).set(User::getIsDeleted, "1").eq(User::getId, u.getId()).execute();
        assertEquals(2, DB.Pojo.select(User.class).eq(User::getIsDeleted, "1").count());
        assertEquals(1, DB.Pojo.select(User.class).eq(User::getIsDeleted, "0").count());
        assertEquals(1, DB.Pojo.select(User.class).count());
    }

    @Test
    public void restore_by_update() {
        User u = DB.Pojo.select(User.class).eq(User::getName, "deleted_user").eq(User::getIsDeleted, "1").queryBean();
        DB.Pojo.update(User.class).set(User::getIsDeleted, "0").eq(User::getId, u.getId()).eq(User::getIsDeleted, "1").execute();
        assertEquals(0, DB.Pojo.select(User.class).eq(User::getIsDeleted, "1").count());
        assertEquals(3, DB.Pojo.select(User.class).eq(User::getIsDeleted, "0").count());
    }

    @Test
    public void physical_delete() {
        DB.Pojo.delete(User.class).setLogicDelete(false).eq(User::getName, "deleted_user").execute();
        assertEquals(0, DB.Jdbc.select("SELECT COUNT(*) FROM user WHERE name=?", "deleted_user").count());
    }
}
