package com.dlz.test.db.cases.page;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/**
 * 分页专题测试
 * 覆盖 Jdbc/Pojo/Sql 的分页查询
 */
public class PageTest extends BaseDBTest {

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
    public void jdbc_page() {
        Page page = DB.Jdbc.select("SELECT * FROM user WHERE DELETED =0")
                .page(1, 2, Order.asc("age"))
                .queryPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    public void jdbc_pageBean() {
        Page<User> page = DB.Jdbc.select("SELECT * FROM user WHERE DELETED =0")
                .page(1, 2, Order.asc("age"))
                .queryPage(User.class);
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    public void pojo_queryPage() {
        Page<User> page = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0")
                .page(Page.build(1, 2, Order.asc("age"))).queryBeanPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    public void page_without_order() {
        Page<User> page = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0")
                .page(1, 2).queryBeanPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    public void page_last_page() {
        Page<User> page = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0")
                .page(2, 2, Order.asc("age")).queryBeanPage();
        assertEquals(3, page.getTotal());
        assertEquals(1, page.getRecords().size());
    }

    @Test
    public void pojo_page_with_order() {
        Page<User> page = DB.Pojo.selectW(User.class).eq(User::getDeleted, "0")
                .page(1, 2).orderByAsc(User::getAge).queryBeanPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
        assertEquals("alice", page.getRecords().get(0).getName());
    }

    @Test
    public void jdbc_page_with_order() {
        Page<ResultMap> page = DB.Jdbc.select("SELECT * FROM user WHERE DELETED =0")
                .page(1, 2).orderByAsc("age").queryPage();
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }
}
