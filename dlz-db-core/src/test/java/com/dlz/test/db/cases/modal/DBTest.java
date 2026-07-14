package com.dlz.test.db.cases.modal;

import com.dlz.db.ds.DBDynamic;
import com.dlz.db.ds.DBTx;
import com.dlz.db.modal.*;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DB 数据库操作入口测试
 */
@DisplayName("DB 数据库操作入口测试")
class DBTest extends BaseDBTest {

    @Test
    @DisplayName("测试 Jdbc 静态字段不为 null")
    void testJdbcNotNull() {
        assertNotNull(DB.jdbc);
        assertTrue(DB.jdbc instanceof DbJdbc);
    }

    @Test
    @DisplayName("测试 Table 静态字段不为 null")
    void testTableNotNull() {
        assertNotNull(DB.table);
        assertTrue(DB.table instanceof DbTable);
    }

    @Test
    @DisplayName("测试 Sql 静态字段不为 null")
    void testSqlNotNull() {
        assertNotNull(DB.sql);
        assertTrue(DB.sql instanceof DbSql);
    }

    @Test
    @DisplayName("测试 Pojo 静态字段不为 null")
    void testPojoNotNull() {
        assertNotNull(DB.pojo);
        assertTrue(DB.pojo instanceof DbPojo);
    }

    @Test
    @DisplayName("测试 Batch 静态字段不为 null")
    void testBatchNotNull() {
        assertNotNull(DB.batch);
        assertTrue(DB.batch instanceof DbBatch);
    }

    @Test
    @DisplayName("测试 Dynamic 静态字段不为 null")
    void testDynamicNotNull() {
        assertNotNull(DB.ds);
        assertTrue(DB.ds instanceof DBDynamic);
    }

    @Test
    @DisplayName("测试 Tx 静态字段不为 null")
    void testTxNotNull() {
        assertNotNull(DB.tx);
        assertTrue(DB.tx instanceof DBTx);
    }

    @Test
    @DisplayName("测试所有静态字段都是 final")
    void testAllFieldsAreFinal() {
        // 验证所有字段都是单例且不可变
        assertSame(DB.jdbc, DB.jdbc);
        assertSame(DB.table, DB.table);
        assertSame(DB.sql, DB.sql);
        assertSame(DB.pojo, DB.pojo);
        assertSame(DB.batch, DB.batch);
        assertSame(DB.ds, DB.ds);
        assertSame(DB.tx, DB.tx);
    }
}
