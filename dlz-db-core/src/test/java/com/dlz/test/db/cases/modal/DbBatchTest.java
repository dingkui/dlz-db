package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbBatch 批量操作测试
 */
@DisplayName("DbBatch 批量操作测试")
class DbBatchTest extends BaseDBTest {

    @Test
    @DisplayName("测试 insert - 空列表返回 false")
    void testInsertEmptyList() {
        List<TestUser> users = Collections.emptyList();

        boolean result = DB.Batch.insert(users);

        assertFalse(result);
    }

    @Test
    @DisplayName("测试 insert - null 列表抛出异常")
    void testInsertNullList() {
        assertThrows(NullPointerException.class, () -> {
            DB.Batch.insert(null);
        });
    }

    @Test
    @DisplayName("测试 insert - 带批次大小参数")
    void testInsertWithBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());

        // 会触发 WrapperBuildUtil 初始化错误
        DB.Batch.insert(users, 100);
    }

    @Test
    @DisplayName("测试 insert - 默认批次大小")
    void testInsertDefaultBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());

        DB.Batch.insert(users);
    }

    @Test
    @DisplayName("测试 update - 空列表返回 false")
    void testUpdateEmptyList() {
        List<TestUser> users = Collections.emptyList();

        boolean result = DB.Batch.update(users);

        assertFalse(result);
    }

    @Test
    @DisplayName("测试 update - null 列表抛出异常")
    void testUpdateNullList() {
        assertThrows(NullPointerException.class, () -> {
            DB.Batch.update(null);
        });
    }

    @Test
    @DisplayName("测试 update - 带批次大小参数")
    void testUpdateWithBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());
        assertThrows(SystemException.class, () -> {
            DB.Batch.update(users, 100);
        });
    }

    @Test
    @DisplayName("测试 update - 默认批次大小")
    void testUpdateDefaultBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());

        assertThrows(SystemException.class, () -> {
            DB.Batch.update(users);
        });
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
        DB.Batch.update(sql, params);
    }

    @Test
    @DisplayName("测试 update - SQL 和参数列表带批次大小")
    void testUpdateWithSqlAndParamsAndBatchSize() {
        String sql = "UPDATE user SET name = ? WHERE id = ?";
        List<Object[]> params = Arrays.asList(
                new Object[]{"张三", 1},
                new Object[]{"李四", 2}
        );

        DB.Batch.update(sql, params, 100);
    }

    @Test
    @DisplayName("测试 update - 空参数列表")
    void testUpdateWithEmptyParams() {
        String sql = "UPDATE user SET name = ? WHERE id = ?";
        List<Object[]> params = Collections.emptyList();

        // 空列表不会进入循环，直接返回 true
        boolean result = DB.Batch.update(sql, params);

        assertTrue(result);
    }
}
