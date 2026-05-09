package com.dlz.test.db.cases.condition;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 条件构造专题测试
 * 覆盖 eq/ne/gt/like/in/or/and/nest 等条件构造
 */
public class ConditionTest extends SpringIntegrationTest {

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
    public void pojo_eq() {
        assertEquals(3, DB.Pojo.select(User.class).eq(User::getIsDeleted, "0").queryBeanList().size());
    }

    @Test
    public void pojo_ne() {
        assertEquals(2, DB.Pojo.select(User.class).ne(User::getStatus, "0").queryBeanList().size());
    }

    @Test
    public void pojo_gt() {
        assertEquals(2, DB.Pojo.select(User.class).gt(User::getAge, 28).queryBeanList().size());
    }

    @Test
    public void pojo_ge() {
        assertEquals(3, DB.Pojo.select(User.class).ge(User::getAge, 25).queryBeanList().size());
    }

    @Test
    public void pojo_lt() {
        assertEquals(1, DB.Pojo.select(User.class).lt(User::getAge, 30).queryBeanList().size());
    }

    @Test
    public void pojo_le() {
        assertEquals(2, DB.Pojo.select(User.class).le(User::getAge, 30).queryBeanList().size());
    }

    @Test
    public void pojo_lk() {
        assertEquals(2, DB.Pojo.select(User.class).like(User::getName, "a").queryBeanList().size());
    }

    @Test
    public void pojo_ll() {
        assertEquals(2, DB.Pojo.select(User.class).likeLeft(User::getName, "e").queryBeanList().size());
    }

    @Test
    public void pojo_lr() {
        assertEquals(1, DB.Pojo.select(User.class).likeRight(User::getName, "a").queryBeanList().size());
    }

    @Test
    public void pojo_nl() {
        assertEquals(1, DB.Pojo.select(User.class).notLike(User::getName, "a").queryBeanList().size());
    }

    @Test
    public void pojo_in() {
        assertEquals(2, DB.Pojo.select(User.class).in(User::getAge, "25, 30").queryBeanList().size());
    }

    @Test
    public void pojo_ni() {
        assertEquals(1, DB.Pojo.select(User.class).notIn(User::getAge, "25, 30").queryBeanList().size());
    }

    @Test
    public void pojo_bt() {
        assertEquals(2, DB.Pojo.select(User.class).between(User::getAge, 25, 30).queryBeanList().size());
    }

    @Test
    public void pojo_nb() {
        assertEquals(1, DB.Pojo.select(User.class).notBetween(User::getAge, 25, 30).queryBeanList().size());
    }

    @Test
    public void pojo_and() {
        assertEquals(1, DB.Pojo.select(User.class)
                .and(a -> a.eq(User::getStatus, "1").eq(User::getName, "alice"))
                .queryBeanList().size());
    }

    @Test
    public void pojo_or() {
        assertEquals(2, DB.Pojo.select(User.class)
                .or(q->q.eq(User::getName, "alice").eq(User::getName, "bob"))
                .queryBeanList().size());
    }

    @Test
    public void pojo_queryCount() {
        assertEquals(2, DB.Pojo.select(User.class).eq(User::getStatus, "1").count());
    }

    @Test
    public void pojo_queryList() {
        assertEquals(2, DB.Pojo.select(User.class).eq(User::getStatus, "1").queryList().size());
    }

    @Test
    public void table_eq() {
        assertEquals(3, DB.Table.select("user").eq("is_deleted", 0).queryList().size());
    }

    @Test
    public void table_and() {
        assertEquals(1, DB.Table.select("user").eq("status", "1").and(a->a.eq("name", "alice")).queryList().size());
    }

    @Test
    public void pojo_dynamic_eq() {
        String name = null;
        assertEquals(3, DB.Pojo.select(User.class).eq(name != null, User::getName, name).queryBeanList().size());
        name = "alice";
        assertEquals(1, DB.Pojo.select(User.class).eq(name != null, User::getName, name).queryBeanList().size());
    }

    @Test
    public void pojo_dynamic_in() {
        Integer[] ages = {25, 30};
        assertEquals(2, DB.Pojo.select(User.class).in(true, User::getAge, ages).queryBeanList().size());
        assertEquals(3, DB.Pojo.select(User.class).in(false, User::getAge, ages).queryBeanList().size());
    }

    @Test
    public void table_lk() {
        assertEquals(2, DB.Table.select("user").like("name", "a").queryList().size());
    }

    @Test
    public void table_in() {
        assertEquals(2, DB.Table.select("user").in("age", "25,30").queryList().size());
    }
}
