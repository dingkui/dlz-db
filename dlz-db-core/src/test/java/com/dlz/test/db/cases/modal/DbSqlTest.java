package com.dlz.test.db.cases.modal;

import com.dlz.db.exception.DbException;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.wrapper.SqlQuery;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbSql SQL 操作测试（含 SQL 生成 + 执行操作）
 */
@DisplayName("DbSql SQL 操作测试")
class DbSqlTest extends BaseDBTest {

    // ========== Wrapper API 入口验证（不执行） ==========

    @Test
    @DisplayName("测试 select 方法")
    void testSelect() {
        SqlQuery query = DB.sql.select("SELECT * FROM user WHERE id = ?");
        assertNotNull(query);
        assertTrue(query instanceof SqlQuery);
    }

    @Test
    @DisplayName("测试不同 SQL 语句")
    void testDifferentSqlStatements() {
        SqlQuery query1 = DB.sql.select("SELECT * FROM user");
        SqlQuery query2 = DB.sql.select("SELECT COUNT(*) FROM user");
        SqlQuery query3 = DB.sql.select("SELECT u.name, o.amount FROM user u JOIN orders o ON u.id = o.user_id");
        assertNotNull(query1);
        assertNotNull(query2);
        assertNotNull(query3);
    }

    @Test
    @DisplayName("测试空 SQL")
    void testEmptySql() {
        SqlQuery query = DB.sql.select("");
        assertNotNull(query);
    }

    @Test
    @DisplayName("测试 null SQL")
    void testNullSql() {
        SqlQuery query = DB.sql.select(null);
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
        SqlQuery query = DB.sql.select(complexSql);
        assertNotNull(query);
    }

    // ========== 执行操作测试 ==========

    @Test
    @DisplayName("测试 execute 方法")
    void testExecute() {
        assertNotNull(DB.sql.execute("DELETE FROM user WHERE id=#{id}", new JSONMap("id", 1)));
        assertThrows(DbException.class, () -> DB.sql.execute("SELECT 1", new JSONMap()));
    }

    @Test
    public void PageTest() {
        DB.sql.select("SELECT t.* FROM Goods t WHERE t.goods_id=310")
                .page(Page.build(1, 2, Order.asc("id")))
                .queryPage();
    }

    @Test
    public void PageTest2() {
        DB.sql.select("SELECT t.* FROM GOODS_PRICE t WHERE t.goods_id=#{goodsId}")
                .page(Page.build(1, 2, Order.asc("id"), Order.desc("xx2")))
                .addPara("goodsId", 123).queryOne();
    }

    @Test
    public void PageTest3() {
        DB.sql.select("SELECT t.* FROM goods_price t WHERE t.goods_id=#{goodsId}")
                .page(null)
                .addPara("goodsId", 123).queryOne();
    }
}
