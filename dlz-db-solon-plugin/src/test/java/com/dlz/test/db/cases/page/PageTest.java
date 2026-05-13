package com.dlz.test.db.cases.page;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.test.db.config.SolonDbBaseTest;
import com.dlz.test.db.entity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 分页专题测试
 * 覆盖 Jdbc/Pojo/Sql 的分页查询
 */
public class PageTest extends SolonDbBaseTest {

    @Before
    public void setUp() {
        DB.Jdbc.execute("delete from user");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, is_deleted INTEGER DEFAULT 0)");
        DB.Jdbc.execute("INSERT INTO user(name,age,status,is_deleted) VALUES(?,?,?,?)", "alice", 25, "1", 0);
        DB.Jdbc.execute("INSERT INTO user(name,age,status,is_deleted) VALUES(?,?,?,?)", "bob", 30, "1", 0);
        DB.Jdbc.execute("INSERT INTO user(name,age,status,is_deleted) VALUES(?,?,?,?)", "charlie", 35, "0", 0);
    }

    @After
    public void tearDown() {
        DB.Jdbc.execute("delete from user");
    }

    @Test
    public void jdbc_page() {
        Page page = DB.Jdbc.select("SELECT * FROM user WHERE is_deleted=0")
                .page(1, 2, Order.asc("age"))
                .queryPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    public void jdbc_pageBean() {
        Page<User> page = DB.Jdbc.select("SELECT * FROM user WHERE is_deleted=0")
                .page(1, 2, Order.asc("age"))
                .queryPage(User.class);
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    public void pojo_queryPage() {
        Page<User> page = DB.Pojo.select(User.class).eq(User::getIsDeleted, "0")
                .page(Page.build(1, 2, Order.asc("age"))).queryBeanPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    public void page_without_order() {
        Page<User> page = DB.Pojo.select(User.class).eq(User::getIsDeleted, "0")
                .page(1, 2).queryBeanPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    public void page_last_page() {
        Page<User> page = DB.Pojo.select(User.class).eq(User::getIsDeleted, "0")
                .page(2, 2, Order.asc("age")).queryBeanPage();
        assertEquals(3, page.getTotal());
        assertEquals(1, page.getRecords().size());
    }

    @Test
    public void pojo_page_with_order() {
        Page<User> page = DB.Pojo.select(User.class).eq(User::getIsDeleted, "0")
                .page(1, 2).orderByAsc(User::getAge).queryBeanPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
        assertEquals("alice", page.getRecords().get(0).getName());
    }

    @Test
    public void jdbc_page_with_order() {
        Page<ResultMap> page = DB.Jdbc.select("SELECT * FROM user WHERE is_deleted=0")
                .page(1, 2).orderByAsc("age").queryPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }
}
