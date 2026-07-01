package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.TestUser;
import com.dlz.test.db.entity.User;
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
    void testPojoInsertEmptyList() {
        List<TestUser> users = Collections.emptyList();
        boolean result = DB.Batch.pojoInsert(users);
        assertFalse(result);
        assertFalse(new PojoInsert(TestUser.class).batch(users));
    }

    @Test
    @DisplayName("测试 insert - null 列表抛出异常")
    void testPojoInsertNullList() {
        assertThrows(NullPointerException.class, () -> {
            DB.Batch.pojoInsert(null);
        });
    }

    @Test
    @DisplayName("测试 insert - 带批次大小参数")
    void testPojoInsertWithBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());

        // 会触发 WrapperBuildUtil 初始化错误
        DB.Batch.pojoInsert(users, 100);
    }

    @Test
    @DisplayName("测试 insert - 默认批次大小")
    void testPojoInsertDefaultBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());

        DB.Batch.pojoInsert(users);
    }

    @Test
    @DisplayName("测试 update - 空列表返回 false")
    void testPojoUpdateEmptyList() {
        List<TestUser> users = Collections.emptyList();

        boolean result = DB.Batch.pojoUpdate(users);

        assertFalse(result);
    }

    @Test
    @DisplayName("测试 update - null 列表抛出异常")
    void testPojoUpdateNullList() {
        assertThrows(NullPointerException.class, () -> {
            DB.Batch.pojoUpdate(null);
        });
    }

    @Test
    @DisplayName("测试 update - 带批次大小参数")
    void testPojoUpdateWithBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());
        assertThrows(SystemException.class, () -> {
            DB.Batch.pojoUpdate(users, 100);
        });
    }

    @Test
    @DisplayName("测试 update - 默认批次大小")
    void testPojoUpdateDefaultBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());

        assertThrows(SystemException.class, () -> {
            DB.Batch.pojoUpdate(users);
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
        DB.Batch.jdbcExecute(sql, params);
    }

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
    @DisplayName("测试 update - 空参数列表")
    void testPojoUpdateWithEmptyBeans() {
        final User insert1 = DB.Pojo.insert(new User());
        final User insert2 = DB.Pojo.insert(new User());
        List<User> users = Arrays.asList(insert1, insert2);
        insert1.setAge(12);
        insert2.setAge(6);
        // 空列表不会进入循环，直接返回 true
        boolean result = DB.Batch.pojoUpdate(users, 10);

        assertTrue(result);
    }

}
