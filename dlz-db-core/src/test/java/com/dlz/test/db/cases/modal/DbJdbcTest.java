package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.DbJdbc;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.wrapper.JdbcSelect;
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
        JdbcSelect query = dbJdbc.select("SELECT * FROM user WHERE id = ?", 1);
        
        assertNotNull(query);
        assertTrue(query instanceof JdbcSelect);
    }

    @Test
    @DisplayName("测试 select 方法传递 SQL 和参数")
    void testSelectWithParameters() {
        JdbcSelect query = dbJdbc.select("SELECT * FROM user WHERE name = ? AND age = ?", "张三", 25);
        
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试 select 方法传递单个参数")
    void testSelectWithSingleParameter() {
        JdbcSelect query = dbJdbc.select("SELECT * FROM user WHERE id = ?", 1);
        
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试 select 方法无参数")
    void testSelectWithoutParameters() {
        JdbcSelect query = dbJdbc.select("SELECT COUNT(*) FROM user");
        
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试方法返回值类型验证")
    void testMethodReturnTypes() {
        // select 返回 JdbcQuery
        assertTrue(dbJdbc.select("SELECT * FROM user") instanceof JdbcSelect);
    }

    @Test
    @DisplayName("测试空 SQL 参数处理")
    void testEmptySql() {
        JdbcSelect query = dbJdbc.select("");
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试 null SQL 参数处理")
    void testNullSql() {
        JdbcSelect query = dbJdbc.select(null);
        // 这里可能在实际执行时失败，但构造应该没问题
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试各种类型的参数")
    void testVariousParameterTypes() {
        // 测试字符串参数
        JdbcSelect query1 = dbJdbc.select("SELECT * FROM user WHERE name = ?", "张三");
        assertNotNull(query1);
        
        // 测试整数参数
        JdbcSelect query2 = dbJdbc.select("SELECT * FROM user WHERE id = ?", 1);
        assertNotNull(query2);
        
        // 测试布尔参数
        JdbcSelect query3 = dbJdbc.select("SELECT * FROM user WHERE active = ?", true);
        assertNotNull(query3);
        
        // 测试浮点参数
        JdbcSelect query4 = dbJdbc.select("SELECT * FROM user WHERE score = ?", 95.5);
        assertNotNull(query4);
    }


    @Test
    public void jdbcPageTest1() {
        final JdbcSelect page = DB.Jdbc.select("SELECT 1 FROM dual WHERE ?=1", 1)
                .page(Page.build(1, 2));
        showSql(page, "jdbcPageTest1", "SELECT 1 FROM dual WHERE 1=1 LIMIT 0,2");
    }

    @Test
    public void jdbcPageTest2() {
        final JdbcSelect page = DB.Jdbc.select("SELECT 1 FROM dual WHERE ?=1", 1)
                .page(Page.build(1, 2, Order.descs("x1", "x2")));
        showSql(page, "jdbcPageTest2", "SELECT 1 FROM dual WHERE 1=1 order by X1 desc,X2 desc LIMIT 0,2");
    }

    @Test
    public void jdbcPageTest3() {
        final JdbcSelect page = DB.Jdbc.select("SELECT 1 FROM dual WHERE ?=1", 1)
                .page(1, 20, Order.descs("x1", "x2"))
                .page(Page.build(Order.descs("x1", "x2")));
        showSql(page, "jdbcPageTest3", "SELECT 1 FROM dual WHERE 1=1 order by X1 desc,X2 desc");
    }

    @Test
    public void jdbcExecute() {
        final int execute = DB.Jdbc.execute("DELETE FROM user");
        Assert.assertNotNull("Jdbc 执行并返回影响行数", execute);
    }

    @Test
    public void jdbcDelete() {
        final int execute = DB.Jdbc.execute("DELETE FROM user where id=?",1);
        Assert.assertNotNull("Jdbc 执行并返回影响行数", execute);
    }
}
