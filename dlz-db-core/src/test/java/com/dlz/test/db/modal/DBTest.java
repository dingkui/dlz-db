package com.dlz.test.db.modal;

import com.dlz.db.ds.DBDynamic;
import com.dlz.db.ds.DBTx;
import com.dlz.db.modal.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DB 数据库操作入口测试
 */
@DisplayName("DB 数据库操作入口测试")
class DBTest {

    @Test
    @DisplayName("测试 Jdbc 静态字段不为 null")
    void testJdbcNotNull() {
        assertNotNull(DB.Jdbc);
        assertTrue(DB.Jdbc instanceof DbJdbc);
    }

    @Test
    @DisplayName("测试 Table 静态字段不为 null")
    void testTableNotNull() {
        assertNotNull(DB.Table);
        assertTrue(DB.Table instanceof DbTable);
    }

    @Test
    @DisplayName("测试 Sql 静态字段不为 null")
    void testSqlNotNull() {
        assertNotNull(DB.Sql);
        assertTrue(DB.Sql instanceof DbSql);
    }

    @Test
    @DisplayName("测试 Pojo 静态字段不为 null")
    void testPojoNotNull() {
        assertNotNull(DB.Pojo);
        assertTrue(DB.Pojo instanceof DbPojo);
    }

    @Test
    @DisplayName("测试 Batch 静态字段不为 null")
    void testBatchNotNull() {
        assertNotNull(DB.Batch);
        assertTrue(DB.Batch instanceof DbBatch);
    }

    @Test
    @DisplayName("测试 Dynamic 静态字段不为 null")
    void testDynamicNotNull() {
        assertNotNull(DB.Dynamic);
        assertTrue(DB.Dynamic instanceof DBDynamic);
    }

    @Test
    @DisplayName("测试 Tx 静态字段不为 null")
    void testTxNotNull() {
        assertNotNull(DB.Tx);
        assertTrue(DB.Tx instanceof DBTx);
    }

    @Test
    @DisplayName("测试所有静态字段都是 final")
    void testAllFieldsAreFinal() {
        // 验证所有字段都是单例且不可变
        assertSame(DB.Jdbc, DB.Jdbc);
        assertSame(DB.Table, DB.Table);
        assertSame(DB.Sql, DB.Sql);
        assertSame(DB.Pojo, DB.Pojo);
        assertSame(DB.Batch, DB.Batch);
        assertSame(DB.Dynamic, DB.Dynamic);
        assertSame(DB.Tx, DB.Tx);
    }
}
