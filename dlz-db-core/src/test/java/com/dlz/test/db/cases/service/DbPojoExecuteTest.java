package com.dlz.test.db.cases.service;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.support.PojoCache;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.TestAutoEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbPojo 增删改操作测试
 * 基于 DbPojo API 的正确使用方式
 */
@DisplayName("DbPojo - 增删改操作测试")
class DbPojoExecuteTest extends BaseDBTest {

    @BeforeAll
    static void initTestTable() throws Exception {
        // 为内部类 TestAutoEntity 预置表字段信息到缓存中
        HashMap<String, Integer> tableColumns = new HashMap<>();
        tableColumns.put("ID", -5);      // BIGINT
        tableColumns.put("NAME", 12);    // VARCHAR
        tableColumns.put("AGE", 4);      // INTEGER

        // 通过反射获取 PojoCache 的 tableColumnsInfoCache 字段
        Field cacheField = PojoCache.class.getDeclaredField("tableColumnsInfoCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, HashMap<String, Integer>> cache =
                (java.util.concurrent.ConcurrentHashMap<String, HashMap<String, Integer>>) cacheField.get(null);
        
        // 注册 TEST_AUTO_ENTITY 表的字段信息
        cache.put("TEST_AUTO_ENTITY", tableColumns);
    }

    // ==================== 插入操作测试 ====================

    @Test
    @DisplayName("插入操作 - 应成功插入并返回对象")
    void testAddSuccess() {
        TestAutoEntity entity = new TestAutoEntity();
        entity.setName("张三");
        entity.setAge(25);

        TestAutoEntity result = DB.Pojo.add(entity);

        assertNotNull(result, "插入后应返回对象");
        assertNotNull(result.getId(), "实体ID应被自动回填");
        assertTrue(result.getId() > 0, "ID应大于0");
        assertEquals("张三", result.getName(), "姓名应一致");
    }

    @Test
    @DisplayName("插入操作 - 插入后应能查询到数据")
    void testAddThenSelect() {
        TestAutoEntity entity = new TestAutoEntity();
        entity.setName("王五");
        entity.setAge(30);

        DB.Pojo.add(entity);

        // 查询验证
        TestAutoEntity found = DB.Pojo.selectById(TestAutoEntity.class, entity.getId());
        assertNotNull(found, "应能查询到插入的数据");
        assertEquals("王五", found.getName(), "姓名应一致");
        assertEquals(30, found.getAge(), "年龄应一致");
    }


    // ==================== 更新操作测试 ====================

    @Test
    @DisplayName("更新操作 - 应根据ID更新记录")
    void testUpdateById() {
        // 先插入
        TestAutoEntity entity = new TestAutoEntity();
        entity.setName("原始名称");
        entity.setAge(25);
        DB.Pojo.add(entity);

        // 修改并更新
        entity.setName("新名称");
        entity.setAge(26);
        TestAutoEntity result = DB.Pojo.updateById(entity);

        assertNotNull(result, "更新后应返回对象");

        // 验证更新结果
        TestAutoEntity updated = DB.Pojo.selectById(TestAutoEntity.class, entity.getId());
        assertEquals("新名称", updated.getName(), "姓名应已更新");
        assertEquals(26, updated.getAge(), "年龄应已更新");
    }

    // ==================== 删除操作测试 ====================

    @Test
    @DisplayName("删除操作 - 应根据ID删除记录")
    void testDeleteById() {
        // 先插入
        TestAutoEntity entity = new TestAutoEntity();
        entity.setName("待删除");
        entity.setAge(25);
        DB.Pojo.add(entity);

        // 删除
        int rows = DB.Pojo.deleteById(TestAutoEntity.class, entity.getId());

        assertEquals(1, rows, "应删除1条记录");

        // 验证删除结果
        TestAutoEntity found = DB.Pojo.selectById(TestAutoEntity.class, entity.getId());
        assertNull(found, "删除后应查询不到数据");
    }

    @Test
    @DisplayName("批量删除 - 应成功删除多条记录")
    void testDeleteByIds() {
        // 插入多条
        List<Long> ids = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TestAutoEntity entity = new TestAutoEntity();
            entity.setName("批量删除" + i);
            DB.Pojo.add(entity);
            ids.add(entity.getId());
        }

        // 批量删除
        int rows = DB.Pojo.deleteByIds(TestAutoEntity.class, ids);

        assertEquals(3, rows, "应删除3条记录");

        // 验证删除结果
        for (Long id : ids) {
            TestAutoEntity found = DB.Pojo.selectById(TestAutoEntity.class, id);
            assertNull(found, "删除后应查询不到数据");
        }
    }

    // ==================== 条件查询测试 ====================

    @Test
    @DisplayName("条件查询 - 应根据条件查询记录")
    void testSelectByCondition() {
        // 插入测试数据
        TestAutoEntity entity = new TestAutoEntity();
        entity.setName("条件测试");
        entity.setAge(25);
        DB.Pojo.add(entity);

        // 条件查询
        PojoQuery<TestAutoEntity> wrapper = DB.Pojo.select(TestAutoEntity.class);
        wrapper.eq("name", "条件测试");
        
        List<TestAutoEntity> list = wrapper.queryBeanList();

        assertNotNull(list, "应返回列表");
        assertFalse(list.isEmpty(), "列表不应为空");
        assertEquals("条件测试", list.get(0).getName(), "姓名应一致");
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("插入操作 - 字段为null应正常处理")
    void testAddWithNullFields() {
        TestAutoEntity entity = new TestAutoEntity();
        entity.setName("null测试");
        entity.setAge(null);

        TestAutoEntity result = DB.Pojo.add(entity);

        assertNotNull(result, "即使有null字段也应成功插入");
    }
}
