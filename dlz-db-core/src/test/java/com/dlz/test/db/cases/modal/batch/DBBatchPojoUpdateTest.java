package com.dlz.test.db.cases.modal.batch;

import com.dlz.db.modal.DB;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.TestUser;
import com.dlz.test.db.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 批量操作专题测试
 * 覆盖批量插入/更新
 */
public class DBBatchPojoUpdateTest extends BaseDBTest {



    @Test
    @DisplayName("测试  UPDATE - 空列表返回 false")
    void testPojoUpdateEmptyList() {
        List<TestUser> users = Collections.emptyList();

        boolean result = DB.batch.update(users).isSuccess();

        assertFalse(result);
    }

    @Test
    @DisplayName("测试  UPDATE - null 列表抛出异常")
    void testPojoUpdateNullList() {
        assertThrows(NullPointerException.class, () -> {
            DB.batch.update(null);
        });
    }

    @Test
    @DisplayName("测试  UPDATE - 带批次大小参数")
    void testPojoUpdateWithBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());
        assertThrows(SystemException.class, () -> {
            DB.batch.update(users, 100);
        });
    }

    @Test
    @DisplayName("测试  UPDATE - 默认批次大小")
    void testPojoUpdateDefaultBatchSize() {
        List<TestUser> users = Arrays.asList(new TestUser(), new TestUser());

        assertThrows(SystemException.class, () -> {
            DB.batch.update(users);
        });
    }

    @Test
    @DisplayName("测试  UPDATE - 空参数列表")
    void testPojoUpdateWithEmptyBeans() {
        final User insert1 = DB.pojo.insert(new User());
        final User insert2 = DB.pojo.insert(new User());
        List<User> users = Arrays.asList(insert1, insert2);
        insert1.setAge(12);
        insert2.setAge(6);
        // 空列表不会进入循环，直接返回 true
        boolean result = DB.batch.update(users, 10).isSuccess();

        assertTrue(result);
    }
}
