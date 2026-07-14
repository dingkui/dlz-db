package com.dlz.test.db.cases.modal.batch;

import com.dlz.db.modal.DB;
import com.dlz.kit.exception.SystemException;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 批量操作专题测试
 * 覆盖批量插入/更新
 */
public class DBBatchTableUpdateTest extends BaseDBTest {
    @Test
    @DisplayName("测试  UPDATE - 空列表返回 false")
    void testPojoUpdateEmptyList() {
        List<JSONMap> users = Collections.emptyList();
        assertFalse(DB.batch.update("Test_User",users).isSuccess());

        assertThrows(NullPointerException.class, () -> {
            assertFalse(DB.batch.update("Test_User",null).isSuccess());
        });
    }

    @Test
    @DisplayName("测试  UPDATE - 带批次大小参数")
    void testPojoUpdateWithBatchSize() {
        List<JSONMap> users = Arrays.asList(new JSONMap(), new JSONMap());
        assertThrows(SystemException.class, () -> {
            DB.batch.update("Test_User",users, 100);
        });
        assertThrows(SystemException.class, () -> {
            DB.batch.update("Test_User",users);
        });
    }

    @Test
    @DisplayName("测试  UPDATE - 空参数列表")
    void testPojoUpdateWithEmptyBeans() {
        final User insert1 = new User();
        final User insert2 = new User();
        insert1.setAge(12);
        insert1.setId(1l);
        insert2.setId(2l);
        insert2.setAge(6);
        List<JSONMap> users = Arrays.asList(new JSONMap(insert1), new JSONMap(insert2));
        // 空列表不会进入循环，直接返回 true
        boolean result = DB.batch.update("Test_User", users, 10).isSuccess();

        assertTrue(result);
    }
}
