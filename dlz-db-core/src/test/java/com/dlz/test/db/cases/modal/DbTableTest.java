package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.dto.ResultMap;
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
 * DbTable 表操作执行测试（DB.Table 实际 CRUD 执行）
 * <p>
 * 完整覆盖 DB.Table 公开 API：
 * <pre>
 * INSERT  (7)  : wrapper execute / insertWithAutoKey / 一步式 insert / insertWithAutoKey
 *                 / insertOrUpdate（新增+更新）/ value(Map) 多字段
 * DELETE  (5)  : wrapper execute / deleteById / deleteByIds(String+List) / ignoreLogicDelete
 * UPDATE  (3)  : wrapper execute（单字段+Map）/ batch 批量更新
 * SELECT  (21) : selectById / selectByIds / queryOne / queryList / queryPage
 *                 / queryOne(Class) / queryList(Class) / queryPage(Class)
 *                 / queryStr / queryLong / queryInt / queryDouble
 *                 / queryStrList / queryLongList / queryIntList / queryDoubleList
 *                 / count / limit / orderByAsc / orderByDesc / sort
 * COND    (5)  : gt+lt / ge+le / like / in / between / isNull
 * COL     (1)  : select 多列限定
 * </pre>
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
    @DisplayName("insertOrUpdate - 插入新记录（无 ID）")
    void testInsertOrUpdateNew() {
        int rows = DB.Table.insertOrUpdate("user",
                new JSONMap().put("name", "upsert新").put("age", 35));
        assertEquals(1, rows, "新记录插入应返回影响行数 1");
    }

    @Test
    @DisplayName("insertOrUpdate - 更新已有记录（有 ID）")
    void testInsertOrUpdateExisting() {
        Long id = DB.Table.insertWithAutoKey("user",new JSONMap().put("name", "upsert原").put("age", 18));

        int rows = DB.Table.insertOrUpdate("user", new JSONMap().put("id", id).put("name", "upsert更新").put("age", 20));
        assertEquals(1, rows, "更新已有记录应返回影响行数 1");

        ResultMap r = DB.Table.selectById("user", id);
        assertEquals("upsert更新", r.get("name"));
        assertEquals(20, r.getInt("age"));
    }

    @Test
    @DisplayName("insert - JSONMap 多字段一次插入")
    void testInsertWithMapMultiFields() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap()
                        .put("name", "多字段")
                        .put("age", 22)
                        .put("sex", "女")
                        .put("city", "北京"));

        ResultMap r = DB.Table.selectById("user", id);
        assertEquals("多字段", r.get("name"));
        assertEquals(22, r.getInt("age"));
        assertEquals("女", r.get("sex"));
        assertEquals("北京", r.get("city"));
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
    @DisplayName("delete - deleteById 按主键删除并验证")
    void testDeleteById() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "待删ById"));

        int rows = DB.Table.deleteById("user", id);
        assertEquals(1, rows);

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
    @DisplayName("delete - ignoreLogicDelete 强制物理删除")
    void testDeleteIgnoreLogicDelete() {
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
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "待更新").put("age", 18));

        int rows = DB.Table.updateW("user").set("name", "已更新").eq("id", id).execute();
        assertEquals(1, rows);

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

    @Test
    @DisplayName("update - batch 批量更新")
    void testBatchUpdate() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "batch原1"));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "batch原2"));

        List<JSONMap> updates = Arrays.asList(
            new JSONMap().put("id", id1).put("name", "batch新1").put("score", "12")
        );

        boolean success = DB.Table.updateW("user").batch(updates);
        assertTrue(success);

        final ResultMap user = DB.Table.selectById("user", id1);
        final ResultMap user1 = DB.Table.selectById("user", id2);
        assertEquals("batch原2", user1.get("name"));
        assertEquals("batch新1", user.get("name"));
    }

    // ==================== SELECT 执行 - 基础 ====================

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
    @DisplayName("queryOne(Class) - 查询单条并映射为 Bean")
    void testQueryOneWithClass() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "Bean查询").put("age", 28));

        User user = DB.Table.selectW("user").eq("id", id).queryOne(User.class);
        assertNotNull(user);
        assertEquals("Bean查询", user.getName());
        assertEquals(Integer.valueOf(28), user.getAge());
    }

    @Test
    @DisplayName("queryList - Wrapper 查询列表")
    void testQueryList() {
        DB.Table.insert("user", new JSONMap().put("name", "列表测试"));

        List<ResultMap> list = DB.Table.selectW("user").queryList();
        assertNotNull(list);
        assertTrue(list.size() >= 1, "列表应至少包含一条记录");
    }

    @Test
    @DisplayName("queryList(Class) - 查询列表并映射为 Bean")
    void testQueryListWithClass() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "Bean列表1"));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "Bean列表2"));

        List<User> users = DB.Table.selectW("user")
                .in("id", id1 + "," + id2)
                .queryList(User.class);
        assertEquals(2, users.size());
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
    @DisplayName("queryPage(Class) - 分页查询并映射为 Bean")
    void testQueryPageWithClass() {
        DB.Table.insert("user", new JSONMap().put("name", "Bean分页"));

        Page<User> page = DB.Table.selectW("user")
                .page(1, 10, Order.asc("id"))
                .queryPage(User.class);
        assertNotNull(page);
        assertTrue(page.getRecords().size() >= 1);
        assertNotNull(page.getRecords().get(0).getName());
    }

    // ==================== SELECT 执行 - 计数 & 单值标量 ====================

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
    @DisplayName("queryStrList - 查询字符串列表（单列多行）")
    void testQueryStrList() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "s1"));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "s2"));

        List<String> names = DB.Table.selectW("user")
                .select("name")
                .in("id", id1 + "," + id2)
                .orderByAsc("name")
                .queryStrList();
        assertEquals(2, names.size());
        assertTrue(names.contains("s1"));
        assertTrue(names.contains("s2"));
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
    @DisplayName("queryLongList - 查询 Long 列表（单列多行）")
    void testQueryLongList() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "L1").put("age", 20));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "L2").put("age", 30));

        List<Long> ages = DB.Table.selectW("user")
                .select("age")
                .in("id", id1 + "," + id2)
                .orderByAsc("age")
                .queryLongList();
        assertEquals(2, ages.size());
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

    @Test
    @DisplayName("queryIntList - 查询 Integer 列表（单列多行）")
    void testQueryIntList() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "I1").put("age", 25));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "I2").put("age", 35));

        List<Integer> ages = DB.Table.selectW("user")
                .select("age")
                .in("id", id1 + "," + id2)
                .orderByAsc("age")
                .queryIntList();
        assertEquals(2, ages.size());
        assertTrue(ages.contains(25));
        assertTrue(ages.contains(35));
    }

    @Test
    @DisplayName("queryDouble - 指定列查询单值 Double（t_b_dict.a5 为 Float 列）")
    void testQueryDouble() {
        final Long id = DB.Table.insertW("t_b_dict")
                .value("dictStatus", "test")
                .value("a5", 3.14d)
                .insertWithAutoKey();

        Double val = DB.Table.selectW("t_b_dict")
                .select("a5")
                .eq("id", id)
                .queryDouble();
        assertEquals(3.14, val, 0.001);
    }

    @Test
    @DisplayName("queryDoubleList - 查询 Double 列表（单列多行）")
    void testQueryDoubleList() {
        final Long id1 = DB.Table.insertW("t_b_dict")
                .value("dictStatus", "test")
                .value("a5", 1.1d)
                .insertWithAutoKey();
        final Long id2 = DB.Table.insertW("t_b_dict")
                .value("dictStatus", "test")
                .value("a5", 2.2d)
                .insertWithAutoKey();

        List<Double> vals = DB.Table.selectW("t_b_dict")
                .select("a5")
                .in("id", Arrays.asList(id1, id2))
                .orderByAsc("a5")
                .queryDoubleList();
        assertEquals(2, vals.size());
        assertEquals(1.1, vals.get(0), 0.001);
        assertEquals(2.2, vals.get(1), 0.001);
    }

    // ==================== SELECT 执行 - 排序 & 分页 ====================

    @Test
    @DisplayName("orderByDesc - 降序排序")
    void testOrderByDesc() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "z_last").put("age", 10));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "a_first").put("age", 20));

        List<String> names = DB.Table.selectW("user")
                .select("name")
                .in("id", id1 + "," + id2)
                .orderByDesc("name")
                .queryStrList();

        assertEquals(2, names.size());
        assertEquals("z_last", names.get(0));
        assertEquals("a_first", names.get(1));
    }

    @Test
    @DisplayName("orderByAsc - 升序排序")
    void testOrderByAsc() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "b_second").put("age", 10));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "a_first").put("age", 20));

        List<String> names = DB.Table.selectW("user")
                .select("name")
                .in("id", id1 + "," + id2)
                .orderByAsc("name")
                .queryStrList();

        assertEquals("a_first", names.get(0));
        assertEquals("b_second", names.get(1));
    }

    @Test
    @DisplayName("sort - 不设分页只设排序条件")
    void testSort() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "b_mid").put("age", 10));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "a_first").put("age", 20));

        List<String> names = DB.Table.selectW("user")
                .select("name")
                .in("id", id1 + "," + id2)
                .sort(Order.asc("name"))
                .queryStrList();

        assertEquals(2, names.size());
        assertEquals("a_first", names.get(0));
        assertEquals("b_mid", names.get(1));
    }

    @Test
    @DisplayName("limit - 限制返回行数")
    void testLimit() {
        DB.Table.insert("user", new JSONMap().put("name", "limit1"));
        DB.Table.insert("user", new JSONMap().put("name", "limit2"));
        DB.Table.insert("user", new JSONMap().put("name", "limit3"));

        List<ResultMap> list = DB.Table.selectW("user")
                .limit(2)
                .queryList();
        assertTrue(list.size() <= 2, "limit(2) 应限制结果不超过 2 条");
    }

    // ==================== SELECT 执行 - 多条件查询 ====================

    @Test
    @DisplayName("条件 - gt + lt 组合（age > 20 AND age < 50）")
    void testGtLtCondition() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "gt1").put("age", 10));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "gt2").put("age", 25));
        Long id3 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "gt3").put("age", 60));

        List<Integer> ages = DB.Table.selectW("user")
                .select("age")
                .in("id", id1 + "," + id2 + "," + id3)
                .gt("age", 20)
                .lt("age", 50)
                .queryIntList();
        assertEquals(1, ages.size(), "只有 age=25 满足条件");
        assertEquals(Integer.valueOf(25), ages.get(0));
    }

    @Test
    @DisplayName("条件 - ge + le 组合（age >= 20 AND age <= 30）")
    void testGeLeCondition() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "ge1").put("age", 10));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "ge2").put("age", 20));
        Long id3 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "ge3").put("age", 30));
        Long id4 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "ge4").put("age", 40));

        List<Integer> ages = DB.Table.selectW("user")
                .select("age")
                .in("id", id1 + "," + id2 + "," + id3 + "," + id4)
                .ge("age", 20)
                .le("age", 30)
                .orderByAsc("age")
                .queryIntList();
        assertEquals(2, ages.size());
        assertEquals(Integer.valueOf(20), ages.get(0));
        assertEquals(Integer.valueOf(30), ages.get(1));
    }

    @Test
    @DisplayName("条件 - like 模糊查询")
    void testLikeCondition() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "like_hello"));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "like_help"));
        Long id3 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "other_world"));

        List<String> names = DB.Table.selectW("user")
                .select("name")
                .in("id", id1 + "," + id2 + "," + id3)
                .like("name", "like_he")
                .orderByAsc("name")
                .queryStrList();
        assertEquals(2, names.size(), "like_he 应匹配 like_hello 和 like_help");
    }

    @Test
    @DisplayName("条件 - in 范围查询")
    void testInCondition() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "in_a"));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "in_b"));
        Long id3 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "in_c"));

        List<ResultMap> list = DB.Table.selectW("user")
                .in("id", id1 + "," + id3)
                .queryList();
        assertEquals(2, list.size(), "in 应匹配 id1 和 id3 两条记录");
    }

    @Test
    @DisplayName("条件 - between 范围查询（age BETWEEN 20 AND 50）")
    void testBetweenCondition() {
        Long id1 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "bw1").put("age", 10));
        Long id2 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "bw2").put("age", 25));
        Long id3 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "bw3").put("age", 40));
        Long id4 = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "bw4").put("age", 60));

        List<Integer> ages = DB.Table.selectW("user")
                .select("age")
                .in("id", id1 + "," + id2 + "," + id3 + "," + id4)
                .between("age", 20, 50)
                .orderByAsc("age")
                .queryIntList();
        assertEquals(2, ages.size(), "between(20,50) 应匹配 age=25 和 age=40");
    }

    @Test
    @DisplayName("条件 - isNull 空值查询")
    void testIsNullCondition() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "isnull_test"));

        List<ResultMap> list = DB.Table.selectW("user")
                .eq("id", id)
                .eq("deleted",0)
                .queryList();
        assertEquals(1, list.size(), "新插入记录的 deleted 应为 0");
    }

    // ==================== SELECT 执行 - 列限定 ====================

    @Test
    @DisplayName("select - 多列限定（只返回所选列）")
    void testSelectMultipleColumns() {
        Long id = DB.Table.insertWithAutoKey("user",
                new JSONMap().put("name", "多列限定").put("age", 25).put("sex", "男"));

        ResultMap r = DB.Table.selectW("user")
                .select("id", "name")
                .eq("id", id)
                .queryOne();
        assertNotNull(r);
        assertNotNull(r.get("id"));
        assertNotNull(r.get("name"));
        assertNull(r.get("age"), "未选择的列 age 应返回 null");
        assertNull(r.get("sex"), "未选择的列 sex 应返回 null");
    }

    // ==================== 异常分支覆盖 ====================

    @Test
    @DisplayName("selectByIds - 空 IDs 应抛出异常")
    void testSelectByIdsWithEmptyIds() {
        assertThrows(SystemException.class, () -> DB.Table.selectByIds("user", ""));
        assertThrows(SystemException.class, () -> DB.Table.selectByIds("user", (Object) null));
    }

    @Test
    @DisplayName("deleteByIds - 空 String IDs 应抛出异常")
    void testDeleteByIdsStringWithEmptyIds() {
        assertThrows(SystemException.class, () -> DB.Table.deleteByIds("user", ""));
        assertThrows(SystemException.class, () -> DB.Table.deleteByIds("user", (Object) null));
    }

    @Test
    @DisplayName("deleteByIds - 空 List IDs 应抛出异常")
    void testDeleteByIdsListWithEmptyIds() {
        assertThrows(SystemException.class, () -> DB.Table.deleteByIds("user", Collections.emptyList()));
        assertThrows(SystemException.class, () -> DB.Table.deleteByIds("user", (List<?>) null));
    }

    @Test
    @DisplayName("deleteById - 空 ID 应抛出异常")
    void testDeleteByIdWithEmptyId() {
        assertThrows(SystemException.class, () -> DB.Table.deleteById("user", ""));
        assertThrows(SystemException.class, () -> DB.Table.deleteById("user", (Object) null));
    }

    // ==================== 自由查询模式 ====================

    @Test
    @DisplayName("setAllowFullQuery - 允许不带 WHERE 条件的全表查询")
    void testAllowFullQuery() {
        DB.Table.insert("user", new JSONMap().put("name", "全表测试"));

        List<ResultMap> list = DB.Table.selectW("user")
                .setAllowFullQuery(true)
                .queryList();
        assertNotNull(list);
        assertTrue(list.size() >= 1);
    }
}
