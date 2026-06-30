package com.dlz.test.db.cases.modal;


import com.dlz.db.modal.DB;
import com.dlz.db.modal.DbPojo;
import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbPojo POJO 操作测试
 */
@DisplayName("DbPojo POJO 操作测试")
class DbPojoTest extends BaseDBTest {
    // ========== SELECT 方法测试 ==========

    @Test
    @DisplayName("测试 select - Class 参数")
    void testSelectWWithClass() {
        PojoQuery<TestUser> query = DB.Pojo.selectW(TestUser.class);

        assertNotNull(query);
        assertTrue(query instanceof PojoQuery);
    }

    @Test
    @DisplayName("测试 select - null 条件 Bean")
    void testSelectWWithNullCondition() {
        // select(null) 会抛出 DbException
        assertThrows(Exception.class, () -> {
            DB.Pojo.selectW(null);
        });
    }

    // ========== DELETE 方法测试 ==========

    @Test
    @DisplayName("测试 delete - Class 参数")
    void testDeleteWWithClass() {
        PojoDelete<TestUser> delete = DB.Pojo.deleteW(TestUser.class);

        assertNotNull(delete);
        assertTrue(delete instanceof PojoDelete);
    }

    // ========== UPDATE 方法测试 ==========

    @Test
    @DisplayName("测试 update - Class 参数")
    void testUpdateWWithClass() {
        PojoUpdate<TestUser> update = DB.Pojo.updateW(TestUser.class);

        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试 update - Bean 参数")
    void testUpdateWWithBean() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("新名字");

        PojoUpdate<TestUser> update = DB.Pojo.updateW(user);

        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试 update - Bean 参数带忽略规则")
    void testUpdateWWithBeanAndIgnore() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("新名字");

        PojoUpdate<TestUser> update = DB.Pojo.updateW(user, name -> "password".equals(name));

        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试 update - null 忽略规则")
    void testUpdateWWithNullIgnore() {
        TestUser user = new TestUser();
        user.setId(1L);

        PojoUpdate<TestUser> update = DB.Pojo.updateW(user, (java.util.function.Function<String, Boolean>) null);

        assertNotNull(update);
    }

    // ========== INSERT 方法测试 ==========

    @Test
    @DisplayName("测试 insert 方法返回类型")
    void testInsertReturnType() {
        TestUser user = new TestUser();
        user.setName("张三");

        // insert 会尝试执行 SQL，在没有数据库的情况下会抛出异常
        DB.Pojo.insert(user);
    }

    // ========== INSERT OR UPDATE 测试 ==========

    @Test
    @DisplayName("测试 insertOrUpdate - ID 为空时执行 insert")
    void testInsertOrUpdateWWithNullId() {
        TestUser user = new TestUser();
        user.setName("张三");
        // id 为 null
        DB.Pojo.insertOrUpdate(user);
    }

    @Test
    @DisplayName("测试 insertOrUpdate - ID 不为空时执行 update")
    void testInsertOrUpdateWWithId() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");

        DB.Pojo.insertOrUpdate(user);
    }

    // ========== UPDATE BY ID 测试 ==========

    @Test
    @DisplayName("测试 updateById - ID 为空抛出异常")
    void testUpdateWByIdWithNullId() {
        TestUser user = new TestUser();
        user.setName("张三");
        // id 为 null

        // StringUtils.isEmpty(null) 返回 true，所以会抛出 SystemException
        assertThrows(SystemException.class, () -> {
            DB.Pojo.updateById(user);
        });
    }

    @Test
    @DisplayName("测试 updateById - ID 不为空")
    void testUpdateWByIdWithId() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");

        DB.Pojo.updateById(user);
    }

    // ========== SELECT BY ID 测试 ==========

    @Test
    @DisplayName("测试 selectById - ID 为空抛出异常")
    void testSelectWByIdWithNullId() {
        // StringUtils.isEmpty(null) 返回 true，所以会抛出 SystemException
        assertThrows(SystemException.class, () -> {
            DB.Pojo.selectById(TestUser.class, null);
        });
    }

    @Test
    @DisplayName("测试 selectById - ID 不为空")
    void testSelectWByIdWithId() {
        DB.Pojo.selectById(TestUser.class, 1L);
    }

    @Test
    @DisplayName("测试 selectById - 字符串 ID")
    void testSelectWByIdWithStringId() {
        DB.Pojo.selectById(TestUser.class, "1");
    }

    // ========== SELECT BY IDS 测试 ==========

    @Test
    @DisplayName("测试 selectByIds - IDs 为空抛出异常")
    void testSelectWByIdsWithNullIds() {
        assertThrows(SystemException.class, () -> {
            DB.Pojo.selectByIds(TestUser.class, null);
        });
    }

    @Test
    @DisplayName("测试 selectByIds - IDs 不为空")
    void testSelectWByIdsWithIds() {
        DB.Pojo.selectByIds(TestUser.class, "1,2,3");
    }

    // ========== DELETE BY ID 测试 ==========

    @Test
    @DisplayName("测试 deleteById - ID 为空抛出异常")
    void testDeleteWByIdWithNullId() {
        assertThrows(SystemException.class, () -> {
            DB.Pojo.deleteById(TestUser.class, null);
        });
    }

    @Test
    @DisplayName("测试 deleteById - ID 不为空")
    void testDeleteWByIdWithId() {
        DB.Pojo.deleteById(TestUser.class, 1L);
    }

    // ========== DELETE BY IDS 测试 ==========

    @Test
    @DisplayName("测试 deleteByIds - String IDs 为空抛出异常")
    void testDeleteWByIdsStringWithNullIds() {
        assertThrows(SystemException.class, () -> {
            DB.Pojo.deleteByIds(TestUser.class, (String) null);
        });
    }

    @Test
    @DisplayName("测试 deleteByIds - String IDs 不为空")
    void testDeleteWByIdsStringWithIds() {
        DB.Pojo.deleteByIds(TestUser.class, "1,2,3");
    }

    @Test
    @DisplayName("测试 deleteByIds - List IDs 为空抛出异常")
    void testDeleteWByIdsListWithNullIds() {
        assertThrows(SystemException.class, () -> {
            DB.Pojo.deleteByIds(TestUser.class, (List<?>) null);
        });
    }

    @Test
    @DisplayName("测试 deleteByIds - List IDs 不为空")
    void testDeleteWByIdsListWithIds() {
        DB.Pojo.deleteByIds(TestUser.class, Arrays.asList(1, 2, 3));
    }

    @Test
    @DisplayName("测试 deleteByIds - 空列表抛出异常")
    void testDeleteWByIdsListWithEmptyList() {
        // StringUtils.isEmpty(emptyList) 返回 false（因为不是 null 也不是空字符串）
        // 但由于没有主键字段，会抛出其他异常
        assertThrows(Exception.class, () -> {
            DB.Pojo.deleteByIds(TestUser.class, Collections.emptyList());
        });
    }

}
