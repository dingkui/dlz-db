package com.dlz.test.db.cases.preset_sql;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 预设 SQL 专题测试
 * 覆盖 SQL 参数化查询（#{param}）
 */
public class PresetSqlTest extends BaseDBTest {

    @Before
    public void setUp() {
        DB.Jdbc.execute("delete from user");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, is_deleted INTEGER DEFAULT 0)");
        DB.Jdbc.execute("INSERT INTO user(name,age,status,is_deleted) VALUES(?,?,?,?)", "alice", 25, "1", 0);
        DB.Jdbc.execute("INSERT INTO user(name,age,status,is_deleted) VALUES(?,?,?,?)", "bob", 30, "1", 0);
    }

    @After
    public void tearDown() {
        DB.Jdbc.execute("delete from user");
    }

    @Test
    public void sql_select() {
        List<ResultMap> list = DB.Sql.select("SELECT * FROM user WHERE name=#{name}").addPara("name", "alice").queryList();
        assertEquals(1, list.size());
        assertEquals("alice", list.get(0).getStr("name"));
    }

    @Test
    public void sql_update() {
        DB.Sql.update("UPDATE user SET age=#{age} WHERE name=#{name}").addPara("age", 77).addPara("name", "alice").execute();
        assertEquals(Integer.valueOf(77),  DB.Jdbc.select("SELECT age FROM user WHERE name=?", "alice").queryInt());
    }

    @Test
    public void sql_insert() {
        DB.Sql.insert("INSERT INTO user(name,age,status,is_deleted) VALUES(#{name},#{age},#{status},#{isdel})")
                .addPara("name", "sql_ins").addPara("age", 11).addPara("status", "1").addPara("isdel", 0).execute();
        assertEquals(1, DB.Jdbc.select("SELECT COUNT(*) FROM user WHERE name=?", "sql_ins").count());
    }

    @Test
    public void sql_delete() {
        DB.Sql.delete("DELETE FROM user WHERE name=#{name}").addPara("name", "alice").execute();
        assertEquals(0, DB.Jdbc.select("SELECT COUNT(*) FROM user WHERE name=?", "alice").count());
    }

    @Test
    public void sql_multiple_params() {
        List<ResultMap> list = DB.Sql.select("SELECT * FROM user WHERE name=#{name} AND age=#{age}")
                .addPara("name", "alice").addPara("age", 25).queryList();
        assertEquals(1, list.size());
        assertEquals("alice", list.get(0).getStr("name"));
    }

    @Test
    public void sql_queryOne() {
        ResultMap row = DB.Sql.select("SELECT * FROM user WHERE name=#{name}")
                .addPara("name", "alice").queryOne();
        assertNotNull(row);
        assertEquals("alice", row.getStr("name"));
    }

    @Test
    public void sql_count() {
        long cnt = DB.Sql.select("SELECT COUNT(*) FROM user WHERE status=#{status}")
                .addPara("status", "1").count();
        assertEquals(2, cnt);
    }
}
