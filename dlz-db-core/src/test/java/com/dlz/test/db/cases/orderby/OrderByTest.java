package com.dlz.test.db.cases.orderby;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * 排序专题测试
 * 覆盖链式排序、Page 内排序
 */
public class OrderByTest extends BaseDBTest {

    @BeforeEach
    public void setUp() {
        DB.Jdbc.execute("delete from user");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, DELETED  INTEGER DEFAULT 0)");
        DB.Jdbc.execute("INSERT INTO user(name,age,status,DELETED ) VALUES(?,?,?,?)", "alice", 25, "1", 0);
        DB.Jdbc.execute("INSERT INTO user(name,age,status,DELETED ) VALUES(?,?,?,?)", "bob", 30, "1", 0);
        DB.Jdbc.execute("INSERT INTO user(name,age,status,DELETED ) VALUES(?,?,?,?)", "charlie", 35, "0", 0);
    }

    @AfterEach
    public void tearDown() {
        DB.Jdbc.execute("delete from user");
    }

    @Test
    public void jdbc_order_asc() {
        List<User> users = DB.Jdbc.select("SELECT * FROM user WHERE DELETED =0 ORDER BY age ASC").queryList(User.class);
        assertEquals("alice", users.get(0).getName());
        assertEquals("charlie", users.get(2).getName());
    }

    @Test
    public void jdbc_order_desc() {
        List<User> users = DB.Jdbc.select("SELECT * FROM user WHERE DELETED =0 ORDER BY age DESC").queryList(User.class);
        assertEquals("charlie", users.get(0).getName());
        assertEquals("alice", users.get(2).getName());
    }

    @Test
    public void pojo_orderByAsc() {
        List<User> users = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0").orderByAsc(User::getAge).queryBeanList();
        assertEquals("alice", users.get(0).getName());
        assertEquals("charlie", users.get(2).getName());
    }

    @Test
    public void pojo_orderByDesc() {
        List<User> users = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0").orderByDesc(User::getAge).queryBeanList();
        assertEquals("charlie", users.get(0).getName());
        assertEquals("alice", users.get(2).getName());
    }

    @Test
    public void pojo_order_multiple() {
        List<User> users = DB.Pojo.selectW(User.class)
                .orderByAsc(User::getStatus)
                .orderByDesc(User::getAge)
                .queryBeanList();
        assertEquals("charlie", users.get(0).getName());
        assertEquals("bob", users.get(1).getName());
    }

    @Test
    public void page_order_asc() {
        List<User> users = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0")
                .sort(Order.asc("age")).queryBeanList();
        assertEquals("alice", users.get(0).getName());
    }

    @Test
    public void page_order_desc() {
        List<User> users = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0")
                .sort(Order.desc("age")).queryBeanList();
        assertEquals("charlie", users.get(0).getName());
    }

    @Test
    public void pojo_orderByAsc_lambda() {
        List<User> users = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0").orderByAsc(User::getAge).queryBeanList();
        assertEquals("alice", users.get(0).getName());
    }

    @Test
    public void pojo_orderByDesc_lambda() {
        List<User> users = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0").orderByDesc(User::getAge).queryBeanList();
        assertEquals("charlie", users.get(0).getName());
    }
}
