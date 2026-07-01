package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.DbJdbc;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.wrapper.JdbcQuery;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DbJdbc 原生 JDBC 操作测试
 */
@DisplayName("DbJdbc 原生 JDBC 操作测试")
class DbJdbcTest extends BaseDBTest {

    private DbJdbc dbJdbc = new DbJdbc();

    @Test
    @DisplayName("测试 select 方法返回 JdbcQuery")
    void testSelect() {
        JdbcQuery query = dbJdbc.select("SELECT * FROM user WHERE id = ?", 1);
        
        assertNotNull(query);
        assertTrue(query instanceof JdbcQuery);
    }

    @Test
    @DisplayName("测试 select 方法传递 SQL 和参数")
    void testSelectWithParameters() {
        JdbcQuery query = dbJdbc.select("SELECT * FROM user WHERE name = ? AND age = ?", "张三", 25);
        
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试 select 方法传递单个参数")
    void testSelectWithSingleParameter() {
        JdbcQuery query = dbJdbc.select("SELECT * FROM user WHERE id = ?", 1);
        
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试 select 方法无参数")
    void testSelectWithoutParameters() {
        JdbcQuery query = dbJdbc.select("SELECT COUNT(*) FROM user");
        
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试方法返回值类型验证")
    void testMethodReturnTypes() {
        // select 返回 JdbcQuery
        assertTrue(dbJdbc.select("SELECT * FROM user") instanceof JdbcQuery);
    }

    @Test
    @DisplayName("测试空 SQL 参数处理")
    void testEmptySql() {
        JdbcQuery query = dbJdbc.select("");
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试 null SQL 参数处理")
    void testNullSql() {
        JdbcQuery query = dbJdbc.select(null);
        // 这里可能在实际执行时失败，但构造应该没问题
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试各种类型的参数")
    void testVariousParameterTypes() {
        // 测试字符串参数
        JdbcQuery query1 = dbJdbc.select("SELECT * FROM user WHERE name = ?", "张三");
        assertNotNull(query1);
        
        // 测试整数参数
        JdbcQuery query2 = dbJdbc.select("SELECT * FROM user WHERE id = ?", 1);
        assertNotNull(query2);
        
        // 测试布尔参数
        JdbcQuery query3 = dbJdbc.select("SELECT * FROM user WHERE active = ?", true);
        assertNotNull(query3);
        
        // 测试浮点参数
        JdbcQuery query4 = dbJdbc.select("SELECT * FROM user WHERE score = ?", 95.5);
        assertNotNull(query4);
    }


    @Test
    public void jdbcPageTest1() {
        final JdbcQuery page = DB.Jdbc.select("select 1 from dual where ?=1", 1)
                .page(Page.build(1, 2));
        showSql(page, "jdbcPageTest1", "select 1 from dual where 1=1 LIMIT 0,2");
    }

    @Test
    public void jdbcPageTest2() {
        final JdbcQuery page = DB.Jdbc.select("select 1 from dual where ?=1", 1)
                .page(Page.build(1, 2, Order.descs("x1", "x2")));
        showSql(page, "jdbcPageTest2", "select 1 from dual where 1=1 order by X1 desc,X2 desc LIMIT 0,2");
    }

    @Test
    public void jdbcPageTest3() {
        final JdbcQuery page = DB.Jdbc.select("select 1 from dual where ?=1", 1)
                .page(1, 20, Order.descs("x1", "x2"))
                .page(Page.build(Order.descs("x1", "x2")));
        showSql(page, "jdbcPageTest3", "select 1 from dual where 1=1 order by X1 desc,X2 desc");
    }

    @Test
    public void jdbcExecute() {
        final int execute = DB.Jdbc.execute("delete from user");
        Assert.assertNotNull("Jdbc 执行并返回影响行数", execute);
    }

    @Test
    public void jdbcDelete() {
        final int execute = DB.Jdbc.execute("delete from user where id=?",1);
        Assert.assertNotNull("Jdbc 执行并返回影响行数", execute);
    }
}
