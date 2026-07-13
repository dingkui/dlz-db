package com.dlz.test.db.cases.convertor;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.support.PojoCache;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.MapColumnBean;
import com.dlz.test.db.entity.TestBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 DB.Pojo 中带非简单属性（Bean 类型）的建表、保存和查询。
 * <p>
 * MapColumnBean.t1 是 TestBean 类型（非简单类型），
 * INSERT 时走 TableColumnMapper.cover() 序列化为 JSON 字符串存入 VARCHAR 列，
 * SELECT 时通过 ValUtil.toObj() 反序列化回 TestBean，保持原结构。
 */
@DisplayName("MapColumnBean Bean属性集成测试")
@Slf4j
class MapColumnBeanTest extends BaseDBTest {
    @BeforeEach
    void clear1() {
        DB.Jdbc.execute("DELETE FROM MAP_COLUMN_BEAN");
    }

    @AfterEach
    void clear2() {
        DB.Jdbc.execute("DELETE FROM MAP_COLUMN_BEAN");
    }

    // ========== 建表验证 ==========

    @Test
    @DisplayName("建表 - autoUpdate 应自动创建 MAP_COLUMN_BEAN 表")
    void tableCreated() {
        // BaseDBTest 初始化时 autoUpdate=true，应已自动建表
        Map<String, Integer> columns = PojoCache.getTableColumnsInfo("MAP_COLUMN_BEAN");
        assertNotNull(columns, "MAP_COLUMN_BEAN 表应已自动创建");
        log.info("MAP_COLUMN_BEAN 表字段信息: {}", columns);

        // 验证核心字段存在
        assertTrue(columns.containsKey("id"), "应包含 id 字段");
        assertTrue(columns.containsKey("T1"), "应包含 T1 字段（Bean 属性列）");
    }

    // ========== 保存 + 查询验证 ==========

    @Test
    @DisplayName("保存并查询 - Bean 属性应序列化存入并反序列化还原")
    void insertAndSelect_withBeanProperty() {
        // 构造 TestBean
        TestBean testBean = new TestBean();
        testBean.setId(100L);
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("key1", "value1");
        jsonMap.put("key2", 42);
        testBean.setT(jsonMap);

        // 构造 MapColumnBean
        MapColumnBean bean = new MapColumnBean();
        bean.setT1(testBean);

        // 插入
        MapColumnBean inserted = DB.Pojo.add(bean);
        assertNotNull(inserted, "插入后应返回对象");
        assertNotNull(inserted.getId(), "id 应自动回填");
        log.info("插入成功, id={}", inserted.getId());

        // 查询回来
        MapColumnBean found = DB.Pojo.selectById(MapColumnBean.class, inserted.getId());
        assertNotNull(found, "应能查询到插入的记录");
        assertNotNull(found.getT1(), "t1（Bean 属性）不应为 null");

        // 验证 TestBean 结构保持
        assertEquals(100L, found.getT1().getId(), "TestBean.id 应保持原值");
        assertNotNull(found.getT1().getT(), "TestBean.t（JSONMap）不应为 null");
        assertEquals("value1", found.getT1().getT().get("key1"), "JSONMap.key1 应保持原值");
        assertEquals(42, found.getT1().getT().getInt("key2"), "JSONMap.key2 应保持原值");
        log.info("查询成功, t1={}", found.getT1());
    }

    @Test
    @DisplayName("保存并查询 - t1 为 null 时应正常处理")
    void insertAndSelect_withNullBeanProperty() {
        MapColumnBean bean = new MapColumnBean();
        bean.setT1(null);

        MapColumnBean inserted = DB.Pojo.add(bean);
        assertNotNull(inserted.getId(), "id 应自动回填");

        MapColumnBean found = DB.Pojo.selectById(MapColumnBean.class, inserted.getId());
        assertNotNull(found, "应能查询到插入的记录");
        assertNull(found.getT1(), "t1 为 null 时查询回来也应为 null");
    }

    @Test
    @DisplayName("更新 - 修改 Bean 属性后应正确保存")
    void update_withBeanProperty() {
        // 先插入
        TestBean testBean = new TestBean();
        testBean.setId(1L);
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("name", "original");
        testBean.setT(jsonMap);

        MapColumnBean bean = new MapColumnBean();
        bean.setT1(testBean);
        MapColumnBean inserted = DB.Pojo.add(bean);

        // 修改 Bean 属性
        TestBean newBean = new TestBean();
        newBean.setId(2L);
        JSONMap newMap = new JSONMap();
        newMap.put("name", "updated");
        newBean.setT(newMap);
        inserted.setT1(newBean);

        // 更新
        DB.Pojo.updateById(inserted);

        // 查询验证
        MapColumnBean found = DB.Pojo.selectById(MapColumnBean.class, inserted.getId());
        assertNotNull(found.getT1(), "更新后 t1 不应为 null");
        assertEquals(2L, found.getT1().getId(), "TestBean.id 应更新");
        assertEquals("updated", found.getT1().getT().get("name"), "JSONMap.name 应更新");
    }


    @Test
    @DisplayName("更新 - 修改 Bean 属性后应正确保存")
    void update_withBeanProperty2() {
        // 先插入
        TestBean testBean = new TestBean();
        testBean.setId(1L);
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("name", "original");
        testBean.setT(jsonMap);

        MapColumnBean bean = new MapColumnBean();
        bean.setT1(testBean);
        MapColumnBean inserted = DB.Pojo.add(bean);

        // 修改 Bean 属性
        TestBean newBean = new TestBean();
        newBean.setId(2L);
        JSONMap newMap = new JSONMap();
        newMap.put("name", "updated");
        newBean.setT(newMap);
        inserted.setT1(newBean);

        // 更新
        DB.Pojo.updateById(inserted);

        JSONMap newMap2 = new JSONMap();
        newMap2.put("namexxx", "updated");

        // 更新
       DB.Pojo.update(MapColumnBean.class)
                .set(MapColumnBean::getT2, newMap2)
                .eq(MapColumnBean::getId, inserted.getId())
                .execute();

        final ResultMap map = DB.Pojo.select(MapColumnBean.class).eq(MapColumnBean::getId, inserted.getId()).queryOne();
        final MapColumnBean mapColumnBean = map.as(MapColumnBean.class);

        // 查询验证
        MapColumnBean found = DB.Pojo.selectById(MapColumnBean.class,  inserted.getId());
        assertNotNull(found.getT1(), "更新后 t1 不应为 null");
        assertEquals("updated", found.getT2().get("namexxx"), "JSONMap.name 应更新");
    }
}
