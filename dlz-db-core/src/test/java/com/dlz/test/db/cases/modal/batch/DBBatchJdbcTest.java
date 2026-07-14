package com.dlz.test.db.cases.modal.batch;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.dto.BatchStatus;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * 批量操作专题测试
 * 覆盖批量插入/更新
 */
public class DBBatchJdbcTest extends BaseDBTest {
    @Test
    @DisplayName("测试  UPDATE - SQL 和参数列表带批次大小")
    void testUpdateWithSqlAndParamsAndBatchSize() {
        String sql = "UPDATE user SET name = ? WHERE id = ?";
        List<Object[]> params = Arrays.asList(
                new Object[]{"张三", 1},
                new Object[]{"李四", 2}
        );


        DB.batch.execute(sql, params, 100);
        DB.batch.execute(sql, params, 1);
        DB.batch.execute(sql, params, 2);
        DB.batch.execute(sql, params, 0);
        DB.batch.execute(sql, Arrays.asList(), 100);
    }

    @Test
    @DisplayName("测试  UPDATE - 空参数列表")
    void testUpdateWithEmptyParams() {
        String sql = "UPDATE user SET name = ? WHERE id = ?";
        List<Object[]> params = Collections.emptyList();

        // 空列表不会进入循环，直接返回 true
        final BatchResult execute = DB.batch.execute(sql, params);

        assertEquals(execute.status(), BatchStatus.SUCCESS);
    }
    @Test
    @DisplayName("测试  UPDATE - SQL 和参数列表")
    void testUpdateWithSqlAndParams() {
        String sql = "UPDATE user SET name = ? WHERE id = ?";
        List<Object[]> params = Arrays.asList(
                new Object[]{"张三", 1},
                new Object[]{"李四", 2}
        );

        // 需要 DBHolder.getSqlExecutor()，会抛出异常
        DB.batch.execute(sql, params);
    }
}
