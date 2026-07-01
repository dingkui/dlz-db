package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbTable 表操作执行测试（DB.Table 实际 CRUD 执行）
 * <p>
 * 覆盖范围：
 * - INSERT：wrapper execute / insertWithAutoKey / 一步式 insert / insertWithAutoKey / insertOrUpdate
 * - DELETE：wrapper execute / deleteById / deleteByIds（String + List）/ ignoreLogicDelete
 * - UPDATE：wrapper execute（单字段 + Map 批量）
 * - SELECT：selectById / selectByIds / queryOne / queryList / queryPage / count / queryStr / queryLong / queryInt
 */
@DisplayName("DbTable 表操作执行测试")
class DbTableTest extends BaseDBTest {

    // ==================== INSERT 执行 ====================

    @Test
    @DisplayName("insert - Wrapper execute 插入并返回影响行数")
    void testInsertExecute() {
        int rows = DB.Table.insertW("user")
                .value("name", "测试用户")
                .value("age", 25)
                .execute();
        assertEquals(1, rows);
    }

    @Test
    @DisplayName("insert - insertWithAutoKey 返回自增主键")
    void testInsertWithAutoKey() {
        Long key = DB.Table.insertW("user")
                .value("name", "自增用户")
                .insertWithAutoKey();
        assertNotNull(key);
        assertTrue(key > 0, "自增主键应大于 0");
    }

    @Test
    @DisplayName("insert - 一步式 insert + JSONMap")
    void testInsertDirect() {
        int rows = DB.Table.insert("user",
                new JSONMap().put("name", "一步插入").put("age", 30));
        assertEquals(1, rows);
    }

    @Test
    @DisplayName("insert - 一步式 insertWithAutoKey + JSONMap")
    void testInsertWithAutoKeyDirect() {
        Long key = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "一步自增").put("age", 28));
        assertNotNull(key);
        assertTrue(key > 0, "自增主键应大于 0");
    }

    @Test
    @DisplayName("insertOrUpdate - 插入新记录")
    void testInsertOrUpdate() {
        int rows = DB.Table.insertOrUpdate("user",
                new JSONMap().put("name", "upsert").put("age", 35));
        assertEquals(1, rows, "新记录插入应返回影响行数 1");
    }

    // ==================== DELETE 执行 ====================

    @Test
    @DisplayName("delete - Wrapper execute 条件删除")
    void testDeleteExecute() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "待删Wrapper"));

        int rows = DB.Table.deleteW("user").eq("id", id).execute();
        assertEquals(1, rows);
    }

    @Test
    @DisplayName("delete - deleteById 按主键删除")
    void testDeleteById() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "待删ById"));

        int rows = DB.Table.deleteById("user", id);
        assertEquals(1, rows);

        // 验证已删除
        ResultMap r = DB.Table.selectById("user", id);
        assertNull(r);
    }

    @Test
    @DisplayName("delete - deleteByIds 字符串参数批量删除")
    void testDeleteByIdsString() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "批量删1"));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "批量删2"));

        int rows = DB.Table.deleteByIds("user", id1 + "," + id2);
        assertEquals(2, rows);
    }

    @Test
    @DisplayName("delete - deleteByIds List 参数批量删除")
    void testDeleteByIdsList() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "List删1"));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "List删2"));

        int rows = DB.Table.deleteByIds("user", Arrays.asList(id1, id2));
        assertEquals(2, rows);
    }

    @Test
    @DisplayName("delete - ignoreLogicDelete 强制物理删除（无逻辑删除字段的表无影响）")
    void testDeleteIgnoreLogicDelete() {
        // user 表无逻辑删除字段，ignoreLogicDelete 不影响正常删除
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "物理删"));

        int rows = DB.Table.deleteW("user")
                .eq("id", id)
                .ignoreLogicDelete(true)
                .execute();
        assertEquals(1, rows);
    }

    // ==================== UPDATE 执行 ====================

    @Test
    @DisplayName("update - Wrapper execute 条件更新并验证")
    void testUpdateExecute() {
        // 先插入
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "待更新").put("age", 18));

        // 更新单个字段
        int rows = DB.Table.updateW("user").set("name", "已更新").eq("id", id).execute();
        assertEquals(1, rows);

        // 验证：被更新字段已变更，未更新字段保持不变
        ResultMap r = DB.Table.selectById("user", id);
        assertEquals("已更新", r.get("name"));
        assertEquals(18, r.getInt("age"), "未更新字段应保持不变");
    }

    @Test
    @DisplayName("update - Map 批量设置更新字段")
    void testUpdateWithMap() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "Map更新"));

        JSONMap updates = new JSONMap();
        updates.put("name", "Map更新后");
        updates.put("age", 50);
        int rows = DB.Table.updateW("user").set(updates).eq("id", id).execute();
        assertEquals(1, rows);

        ResultMap r = DB.Table.selectById("user", id);
        assertEquals("Map更新后", r.get("name"));
        assertEquals(50, r.getInt("age"));
    }

    // ==================== SELECT 执行 ====================

    @Test
    @DisplayName("selectById - 按主键查询单条")
    void testSelectById() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "ById查询"));

        ResultMap r = DB.Table.selectById("user", id);
        assertNotNull(r);
        assertEquals("ById查询", r.get("name"));
        assertNotNull(r.get("id"));
    }

    @Test
    @DisplayName("selectByIds - 批量查询")
    void testSelectByIds() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "批量查A"));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "批量查B"));

        List<ResultMap> list = DB.Table.selectByIds("user", id1 + "," + id2);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("queryOne - Wrapper 查询单条")
    void testQueryOne() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "Q1用户"));

        ResultMap r = DB.Table.selectW("user").eq("id", id).queryOne();
        assertNotNull(r);
        assertEquals("Q1用户", r.get("name"));
    }

    @Test
    @DisplayName("queryList - Wrapper 查询列表")
    void testQueryList() {
        // 先插入确保有数据
        DB.Table.insert("user", new JSONMap().put("name", "列表测试"));

        List<ResultMap> list = DB.Table.selectW("user").queryList();
        assertNotNull(list);
        assertTrue(list.size() >= 1, "列表应至少包含一条记录");
    }

    @Test
    @DisplayName("queryPage - Wrapper 分页查询")
    void testQueryPage() {
        DB.Table.insert("user", new JSONMap().put("name", "分页数据"));

        Page<ResultMap> page = DB.Table.selectW("user")
                .page(1, 10, Order.asc("id"))
                .queryPage();
        assertNotNull(page);
        assertTrue(page.getSize() >= 1);
        assertNotNull(page.getRecords());
        assertTrue(page.getRecords().size() >= 1);
    }

    @Test
    @DisplayName("count - Wrapper 计数查询")
    void testCount() {
        long count = DB.Table.selectW("user").count();
        assertTrue(count >= 0);
    }

    @Test
    @DisplayName("queryStr - 指定列查询单值字符串")
    void testQueryStr() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "StrVal"));

        String name = DB.Table.selectW("user")
                .select("name")
                .eq("id", id)
                .queryStr();
        assertEquals("StrVal", name);
    }

    @Test
    @DisplayName("queryLong - 指定列查询单值 Long")
    void testQueryLong() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "Long测试").put("age", 33));

        Long age = DB.Table.selectW("user")
                .select("age")
                .eq("id", id)
                .queryLong();
        assertEquals(Long.valueOf(33), age);
    }

    @Test
    @DisplayName("queryInt - 指定列查询单值 Integer")
    void testQueryInt() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "Int测试").put("age", 44));

        Integer age = DB.Table.selectW("user")
                .select("age")
                .eq("id", id)
                .queryInt();
        assertEquals(Integer.valueOf(44), age);
    }
}
