package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DbSql;
import com.dlz.db.modal.wrapper.SqlExecute;
import com.dlz.db.modal.wrapper.SqlQuery;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbSql SQL 操作测试
 */
@DisplayName("DbSql SQL 操作测试")
class DbSqlTest extends BaseDBTest {

    private DbSql dbSql;

    @BeforeEach
    void setUp() {
        dbSql = new DbSql();
    }

    @Test
    @DisplayName("测试 select 方法")
    void testSelect() {
        SqlQuery query = dbSql.select("SELECT * FROM user WHERE id = ?");
        
        assertNotNull(query);
        assertTrue(query instanceof SqlQuery);
    }

    @Test
    @DisplayName("测试 insert 方法")
    void testInsert() {
        SqlExecute execute = dbSql.insert("INSERT INTO user (name) VALUES (?)");
        
        assertNotNull(execute);
        assertTrue(execute instanceof SqlExecute);
    }

    @Test
    @DisplayName("测试 update 方法")
    void testUpdate() {
        SqlExecute execute = dbSql.update("UPDATE user SET name = ? WHERE id = ?");
        
        assertNotNull(execute);
        assertTrue(execute instanceof SqlExecute);
    }

    @Test
    @DisplayName("测试 delete 方法")
    void testDelete() {
        SqlExecute execute = dbSql.delete("DELETE FROM user WHERE id = ?");
        
        assertNotNull(execute);
        assertTrue(execute instanceof SqlExecute);
    }

    @Test
    @DisplayName("测试 executer 方法")
    void testExecuter() {
        SqlExecute execute = dbSql.executer("CREATE TABLE test (id INT)");
        
        assertNotNull(execute);
        assertTrue(execute instanceof SqlExecute);
    }

    @Test
    @DisplayName("测试 execute 方法")
    void testExecute() {
        // Mock 环境下 execute 不会抛异常，返回 0
        int result = dbSql.execute("SELECT 1");
        assertEquals(0, result);
    }

    @Test
    @DisplayName("测试不同 SQL 语句")
    void testDifferentSqlStatements() {
        SqlQuery query1 = dbSql.select("SELECT * FROM user");
        SqlQuery query2 = dbSql.select("SELECT COUNT(*) FROM user");
        SqlQuery query3 = dbSql.select("SELECT u.name, o.amount FROM user u JOIN orders o ON u.id = o.user_id");
        
        assertNotNull(query1);
        assertNotNull(query2);
        assertNotNull(query3);
    }

    @Test
    @DisplayName("测试空 SQL")
    void testEmptySql() {
        SqlQuery query = dbSql.select("");
        
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试 null SQL")
    void testNullSql() {
        SqlQuery query = dbSql.select(null);
        
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试复杂 SQL")
    void testComplexSql() {
        String complexSql = "SELECT u.*, COUNT(o.id) as order_count " +
                           "FROM user u " +
                           "LEFT JOIN orders o ON u.id = o.user_id " +
                           "WHERE u.status = ? " +
                           "GROUP BY u.id " +
                           "HAVING order_count > ? " +
                           "ORDER BY u.create_time DESC";
        
        SqlQuery query = dbSql.select(complexSql);
        
        assertNotNull(query);
    }
}
