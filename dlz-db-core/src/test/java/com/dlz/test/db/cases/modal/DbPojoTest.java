package com.dlz.test.db.cases.modal;


import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.db.modal.DbPojo;
import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbPojo POJO 操作测试
 */
@DisplayName("DbPojo POJO 操作测试")
class DbPojoTest extends BaseDBTest {

    private DbPojo dbPojo;

    @BeforeEach
    void setUp() {
        dbPojo = new DbPojo();

        // 清除所有缓存，避免与其他测试类的 TestUser 冲突
        BeanInfoHolder.clearAll();

        // 为 TestUser 类预置表字段信息到缓存中
        // 表名是 TEST_USER（根据类名转换）
        HashMap<String, Integer> tableColumns = new HashMap<>();
        tableColumns.put("ID", 1);      // BIGINT
        tableColumns.put("NAME", 2);    // VARCHAR
        tableColumns.put("AGE", 3);     // INTEGER
        tableColumns.put("EMAIL", 4);   // VARCHAR

        // CacheMap 继承自 ConcurrentHashMap，可以直接 put
        try {
            java.lang.reflect.Field cacheField = BeanInfoHolder.class.getDeclaredField("tableColumnsInfoCache");
            cacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.concurrent.ConcurrentHashMap<String, HashMap<String, Integer>> cache =
                    (java.util.concurrent.ConcurrentHashMap<String, HashMap<String, Integer>>) cacheField.get(null);
            cache.put("TEST_USER", tableColumns);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup mock table columns", e);
        }
    }

    // ========== SELECT 方法测试 ==========

    @Test
    @DisplayName("测试 select - Class 参数")
    void testSelectWithClass() {
        PojoQuery<TestUser> query = dbPojo.select(TestUser.class);

        assertNotNull(query);
        assertTrue(query instanceof PojoQuery);
    }

    @Test
    @DisplayName("测试 select - 条件 Bean 参数")
    void testSelectWithConditionBean() {
        TestUser condition = new TestUser();
        condition.setName("张三");

        PojoQuery<TestUser> query = dbPojo.select(condition);

        assertNotNull(query);
        assertTrue(query instanceof PojoQuery);
    }

    @Test
    @DisplayName("测试 select - null 条件 Bean")
    void testSelectWithNullCondition() {
        // select(null) 会抛出 DbException
        assertThrows(Exception.class, () -> {
            dbPojo.select((TestUser) null);
        });
    }

    // ========== DELETE 方法测试 ==========

    @Test
    @DisplayName("测试 delete - Class 参数")
    void testDeleteWithClass() {
        PojoDelete<TestUser> delete = dbPojo.delete(TestUser.class);

        assertNotNull(delete);
        assertTrue(delete instanceof PojoDelete);
    }

    @Test
    @DisplayName("测试 delete - 条件 Bean 参数")
    void testDeleteWithConditionBean() {
        TestUser condition = new TestUser();
        condition.setId(1L);

        PojoDelete<TestUser> delete = dbPojo.delete(condition);

        assertNotNull(delete);
        assertTrue(delete instanceof PojoDelete);
    }

    // ========== UPDATE 方法测试 ==========

    @Test
    @DisplayName("测试 update - Class 参数")
    void testUpdateWithClass() {
        PojoUpdate<TestUser> update = dbPojo.update(TestUser.class);

        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试 update - Bean 参数")
    void testUpdateWithBean() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("新名字");

        PojoUpdate<TestUser> update = dbPojo.update(user);

        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试 update - Bean 参数带忽略规则")
    void testUpdateWithBeanAndIgnore() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("新名字");

        PojoUpdate<TestUser> update = dbPojo.update(user, name -> "password".equals(name));

        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试 update - null 忽略规则")
    void testUpdateWithNullIgnore() {
        TestUser user = new TestUser();
        user.setId(1L);

        PojoUpdate<TestUser> update = dbPojo.update(user, (java.util.function.Function<String, Boolean>) null);

        assertNotNull(update);
    }

    // ========== INSERT 方法测试 ==========

    @Test
    @DisplayName("测试 insert 方法返回类型")
    void testInsertReturnType() {
        TestUser user = new TestUser();
        user.setName("张三");

        // insert 会尝试执行 SQL，在没有数据库的情况下会抛出异常
        dbPojo.insert(user);
    }

    @Test
    @DisplayName("测试 insertBatch - 带批次大小")
    void testInsertBatchWithSize() {
        List<TestUser> users = Arrays.asList(
                new TestUser(),
                new TestUser()
        );

        // insertBatch 会尝试执行 SQL
        dbPojo.insertBatch(users, 100);
    }

    @Test
    @DisplayName("测试 insertBatch - 默认批次大小")
    void testInsertBatchDefaultSize() {
        List<TestUser> users = Arrays.asList(
                new TestUser(),
                new TestUser()
        );

        dbPojo.insertBatch(users);
    }

    @Test
    @DisplayName("测试 insertBatch - 空列表")
    void testInsertBatchEmptyList() {
        List<TestUser> users = Collections.emptyList();

        boolean result = dbPojo.insertBatch(users);

        assertFalse(result);
    }

    @Test
    @DisplayName("测试 insertBatch - null 列表")
    void testInsertBatchNullList() {
        assertThrows(NullPointerException.class, () -> {
            dbPojo.insertBatch(null);
        });
    }

    // ========== INSERT OR UPDATE 测试 ==========

    @Test
    @DisplayName("测试 insertOrUpdate - ID 为空时执行 insert")
    void testInsertOrUpdateWithNullId() {
        TestUser user = new TestUser();
        user.setName("张三");
        // id 为 null

        dbPojo.insertOrUpdate(user);
    }

    @Test
    @DisplayName("测试 insertOrUpdate - ID 不为空时执行 update")
    void testInsertOrUpdateWithId() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");

        dbPojo.insertOrUpdate(user);
    }

    // ========== UPDATE BY ID 测试 ==========

    @Test
    @DisplayName("测试 updateById - ID 为空抛出异常")
    void testUpdateByIdWithNullId() {
        TestUser user = new TestUser();
        user.setName("张三");
        // id 为 null

        // StringUtils.isEmpty(null) 返回 true，所以会抛出 SystemException
        assertThrows(SystemException.class, () -> {
            dbPojo.updateById(user);
        });
    }

    @Test
    @DisplayName("测试 updateById - ID 不为空")
    void testUpdateByIdWithId() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");

        dbPojo.updateById(user);
    }

    // ========== SELECT BY ID 测试 ==========

    @Test
    @DisplayName("测试 selectById - ID 为空抛出异常")
    void testSelectByIdWithNullId() {
        // StringUtils.isEmpty(null) 返回 true，所以会抛出 SystemException
        assertThrows(SystemException.class, () -> {
            dbPojo.selectById(TestUser.class, null);
        });
    }

    @Test
    @DisplayName("测试 selectById - ID 不为空")
    void testSelectByIdWithId() {
        dbPojo.selectById(TestUser.class, 1L);
    }

    @Test
    @DisplayName("测试 selectById - 字符串 ID")
    void testSelectByIdWithStringId() {
        dbPojo.selectById(TestUser.class, "1");
    }

    // ========== SELECT BY IDS 测试 ==========

    @Test
    @DisplayName("测试 selectByIds - IDs 为空抛出异常")
    void testSelectByIdsWithNullIds() {
        assertThrows(SystemException.class, () -> {
            dbPojo.selectByIds(TestUser.class, null);
        });
    }

    @Test
    @DisplayName("测试 selectByIds - IDs 不为空")
    void testSelectByIdsWithIds() {
        dbPojo.selectByIds(TestUser.class, "1,2,3");
    }

    // ========== DELETE BY ID 测试 ==========

    @Test
    @DisplayName("测试 deleteById - ID 为空抛出异常")
    void testDeleteByIdWithNullId() {
        assertThrows(SystemException.class, () -> {
            dbPojo.deleteById(TestUser.class, null);
        });
    }

    @Test
    @DisplayName("测试 deleteById - ID 不为空")
    void testDeleteByIdWithId() {
        dbPojo.deleteById(TestUser.class, 1L);
    }

    // ========== DELETE BY IDS 测试 ==========

    @Test
    @DisplayName("测试 deleteByIds - String IDs 为空抛出异常")
    void testDeleteByIdsStringWithNullIds() {
        assertThrows(SystemException.class, () -> {
            dbPojo.deleteByIds(TestUser.class, (String) null);
        });
    }

    @Test
    @DisplayName("测试 deleteByIds - String IDs 不为空")
    void testDeleteByIdsStringWithIds() {
        dbPojo.deleteByIds(TestUser.class, "1,2,3");
    }

    @Test
    @DisplayName("测试 deleteByIds - List IDs 为空抛出异常")
    void testDeleteByIdsListWithNullIds() {
        assertThrows(SystemException.class, () -> {
            dbPojo.deleteByIds(TestUser.class, (List<?>) null);
        });
    }

    @Test
    @DisplayName("测试 deleteByIds - List IDs 不为空")
    void testDeleteByIdsListWithIds() {
        dbPojo.deleteByIds(TestUser.class, Arrays.asList(1, 2, 3));
    }

    @Test
    @DisplayName("测试 deleteByIds - 空列表抛出异常")
    void testDeleteByIdsListWithEmptyList() {
        // StringUtils.isEmpty(emptyList) 返回 false（因为不是 null 也不是空字符串）
        // 但由于没有主键字段，会抛出其他异常
        assertThrows(Exception.class, () -> {
            dbPojo.deleteByIds(TestUser.class, Collections.emptyList());
        });
    }

    /**
     * 测试用的 User 实体类
     */
    @Setter
    @Getter
    static class TestUser {
        private Long id;
        private String name;
        private Integer age;
        private String email;
    }
}
