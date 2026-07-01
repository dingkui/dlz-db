package com.dlz.test.db.cases.modal.batch;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Orders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 批量操作专题测试
 * 覆盖批量插入/更新
 */
public class DBBatchJdbcTest extends BaseDBTest {
    @Test
    @DisplayName("测试 update - SQL 和参数列表带批次大小")
    void testUpdateWithSqlAndParamsAndBatchSize() {
        String sql = "UPDATE user SET name = ? WHERE id = ?";
        List<Object[]> params = Arrays.asList(
                new Object[]{"张三", 1},
                new Object[]{"李四", 2}
        );

        DB.Batch.jdbcExecute(sql, params, 100);
        DB.Batch.jdbcExecute(sql, params, 1);
        DB.Batch.jdbcExecute(sql, params, 2);
        DB.Batch.jdbcExecute(sql, params, 0);
        DB.Batch.jdbcExecute(sql, Arrays.asList(), 100);
    }

    @Test
    @DisplayName("测试 update - 空参数列表")
    void testUpdateWithEmptyParams() {
        String sql = "UPDATE user SET name = ? WHERE id = ?";
        List<Object[]> params = Collections.emptyList();

        // 空列表不会进入循环，直接返回 true
        boolean result = DB.Batch.jdbcExecute(sql, params);

        assertTrue(result);
    }
    @Test
    @DisplayName("测试 update - SQL 和参数列表")
    void testUpdateWithSqlAndParams() {
        String sql = "UPDATE user SET name = ? WHERE id = ?";
        List<Object[]> params = Arrays.asList(
                new Object[]{"张三", 1},
                new Object[]{"李四", 2}
        );

        // 需要 DBHolder.getSqlExecutor()，会抛出异常
        DB.Batch.jdbcExecute(sql, params);
    }
}
