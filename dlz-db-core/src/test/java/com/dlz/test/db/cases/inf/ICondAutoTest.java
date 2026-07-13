package com.dlz.test.db.cases.inf;

import com.dlz.db.inf.ICondAuto;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.exception.ValidateException;
import com.dlz.kit.json.JSONMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ICondAuto Map自动条件测试")
class ICondAutoTest {

    private StubCondAuto stub;

    @BeforeEach
    void setUp() {
        stub = new StubCondAuto();
    }

    private String getSql() {
        final ParaMap pm = new ParaMap();
        pm.getSqlItem().setSqlRun(stub.condition.getRunsql(pm));
        final String runSql = pm.jdbcSql().toRunSql();
        return SqlUtil.replaceSql(runSql, pm.getPara(), 1);
    }

    // ========== auto(Map) 基础测试 ==========

    @Test
    @DisplayName("auto(Map) - 单个eq条件")
    void testAutoSingleEq() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        stub.auto(req);
        assertEquals("WHERE status = 1", getSql());
    }

    @Test
    @DisplayName("auto(Map) - 多个eq条件")
    void testAutoMultipleEq() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        req.put("age", 25);
        stub.auto(req);
        String sql = getSql();
        assertTrue(sql.contains("status = 1"));
        assertTrue(sql.contains("age = 25"));
    }

    @Test
    @DisplayName("auto(Map) - null map")
    void testAutoNullMap() {
        StubCondAuto result = stub.auto(null);
        assertNotNull(result);
        assertEquals("", getSql());
    }

    @Test
    @DisplayName("auto(Map) - 空map")
    void testAutoEmptyMap() {
        StubCondAuto result = stub.auto(new HashMap<>());
        assertNotNull(result);
        assertEquals("", getSql());
    }

    // ========== 操作符前缀解析 ==========

    @Test
    @DisplayName("auto - _gt_ 前缀解析")
    void testAutoGtPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_gt_age", 18);
        stub.auto(req);
        assertEquals("WHERE age > 18", getSql());
    }

    @Test
    @DisplayName("auto - _lt_ 前缀解析")
    void testAutoLtPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_lt_age", 60);
        stub.auto(req);
        assertEquals("WHERE age < 60", getSql());
    }

    @Test
    @DisplayName("auto - _ge_ 前缀解析")
    void testAutoGePrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_ge_score", 80);
        stub.auto(req);
        assertEquals("WHERE score >= 80", getSql());
    }

    @Test
    @DisplayName("auto - _le_ 前缀解析")
    void testAutoLePrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_le_score", 100);
        stub.auto(req);
        assertEquals("WHERE score <= 100", getSql());
    }

    @Test
    @DisplayName("auto - _ne_ 前缀解析")
    void testAutoNePrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_ne_status", 0);
        stub.auto(req);
        assertEquals("WHERE status <> 0", getSql());
    }

    @Test
    @DisplayName("auto - _eq_ 前缀显式指定")
    void testAutoEqPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_eq_status", 1);
        stub.auto(req);
        assertEquals("WHERE status = 1", getSql());
    }

    @Test
    @DisplayName("auto - _like_ 前缀解析")
    void testAutoLikePrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_like_name", "张");
        stub.auto(req);
        assertTrue(getSql().contains("name LIKE"));
    }

    @Test
    @DisplayName("auto - _likeLeft_ 前缀解析")
    void testAutoLikeLeftPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_likeLeft_name", "张");
        stub.auto(req);
        assertTrue(getSql().contains("name LIKE"));
    }

    @Test
    @DisplayName("auto - _likeRight_ 前缀解析")
    void testAutoLikeRightPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_likeRight_email", "@qq.com");
        stub.auto(req);
        assertTrue(getSql().contains("email LIKE"));
    }

    @Test
    @DisplayName("auto - _notLike_ 前缀解析")
    void testAutoNotLikePrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_notLike_name", "test");
        stub.auto(req);
        assertTrue(getSql().contains("name NOT LIKE"));
    }

    @Test
    @DisplayName("auto - _in_ 前缀解析")
    void testAutoInPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_in_status", "1,2,3");
        stub.auto(req);
        assertTrue(getSql().contains("status IN"));
    }

    @Test
    @DisplayName("auto - _notIn_ 前缀解析")
    void testAutoNotInPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_notIn_status", "1,2,3");
        stub.auto(req);
        assertTrue(getSql().contains("status NOT IN"));
    }

    @Test
    @DisplayName("auto - _isNull_ 前缀解析")
    void testAutoIsNullPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_isNull_email", null);
        stub.auto(req);
        assertEquals("WHERE email IS NULL", getSql());
    }

    @Test
    @DisplayName("auto - _isNotNull_ 前缀解析")
    void testAutoIsNotNullPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_isNotNull_email", null);
        stub.auto(req);
        assertEquals("WHERE email IS NOT NULL", getSql());
    }

    @Test
    @DisplayName("auto - _between_ 前缀解析")
    void testAutoBetweenPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_between_age", "18,60");
        stub.auto(req);
        assertTrue(getSql().contains("age BETWEEN"));
    }

    @Test
    @DisplayName("auto - _notBetween_ 前缀解析")
    void testAutoNotBetweenPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_notBetween_age", "18,60");
        stub.auto(req);
        assertTrue(getSql().contains("age NOT BETWEEN"));
    }

    // ========== 前缀边界/异常 ==========

    @Test
    @DisplayName("auto - 无第二个下划线时跳过")
    void testAutoNoSecondUnderscore() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_invalidkey", 1);
        stub.auto(req);
        assertEquals("", getSql());
    }

    @Test
    @DisplayName("auto - 前缀后列名为空时跳过")
    void testAutoEmptyColumnAfterPrefix() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_eq_", 1);
        stub.auto(req);
        assertEquals("", getSql());
    }

    @Test
    @DisplayName("auto - 混合普通和前缀key")
    void testAutoMixedKeys() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        req.put("_gt_age", 18);
        stub.auto(req);
        String sql = getSql();
        assertTrue(sql.contains("status = 1"));
        assertTrue(sql.contains("age > 18"));
    }

    // ========== auto(Map, Function) 过滤器测试 ==========

    @Test
    @DisplayName("auto(Map, Function) - 过滤器接受所有")
    void testAutoFilterAcceptAll() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        req.put("age", 25);
        stub.auto(req, col -> true);
        String sql = getSql();
        assertTrue(sql.contains("status = 1"));
        assertTrue(sql.contains("age = 25"));
    }

    @Test
    @DisplayName("auto(Map, Function) - 过滤器排除指定列")
    void testAutoFilterExcludeColumn() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        req.put("password", "secret");
        stub.auto(req, col -> !"password".equals(col));
        String sql = getSql();
        assertTrue(sql.contains("status = 1"));
        assertFalse(sql.contains("password"));
    }

    @Test
    @DisplayName("auto(Map, Function) - 过滤器拒绝所有")
    void testAutoFilterRejectAll() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        req.put("age", 25);
        stub.auto(req, col -> false);
        assertEquals("", getSql());
    }

    @Test
    @DisplayName("auto(Map, Function) - null过滤器等同auto(Map)")
    void testAutoNullFilter() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        stub.auto(req, (java.util.function.Function<String, Boolean>) null);
        assertEquals("WHERE status = 1", getSql());
    }

    @Test
    @DisplayName("auto(Map, Function) - 过滤器作用于去前缀后的列名")
    void testAutoFilterOnStrippedColumn() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_gt_age", 18);
        req.put("_eq_password", "secret");
        stub.auto(req, col -> !"password".equals(col));
        String sql = getSql();
        assertTrue(sql.contains("age > 18"));
        assertFalse(sql.contains("password"));
        //异常key
        assertThrows(ValidateException.class,()-> stub.auto(new JSONMap("_errorOp_xx",1)));
    }

    // ========== auto(Map, Set) 排除集测试 ==========

    @Test
    @DisplayName("auto(Map, Set) - 排除指定列")
    void testAutoExcludeSet() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        req.put("password", "secret");
        req.put("age", 25);
        Set<String> exclude = new HashSet<>(Arrays.asList("password"));
        stub.auto(req, exclude);
        String sql = getSql();
        assertTrue(sql.contains("status = 1"));
        assertTrue(sql.contains("age = 25"));
        assertFalse(sql.contains("password"));
    }

    @Test
    @DisplayName("auto(Map, Set) - 空排除集")
    void testAutoEmptyExcludeSet() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        Set<String> exclude = new HashSet<>();
        stub.auto(req, exclude);
        assertEquals("WHERE status = 1", getSql());
    }

    @Test
    @DisplayName("auto(Map, Set) - null排除集")
    void testAutoNullExcludeSet() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        stub.auto(req, (Set<String>) null);
        assertEquals("", getSql());
    }

    @Test
    @DisplayName("auto(Map, Set) - 排除多个列")
    void testAutoExcludeMultiple() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("status", 1);
        req.put("password", "secret");
        req.put("token", "abc");
        req.put("age", 25);
        Set<String> exclude = new HashSet<>(Arrays.asList("password", "token"));
        stub.auto(req, exclude);
        String sql = getSql();
        assertTrue(sql.contains("status = 1"));
        assertTrue(sql.contains("age = 25"));
        assertFalse(sql.contains("password"));
        assertFalse(sql.contains("token"));
    }

    @Test
    @DisplayName("auto(Map, Set) - 排除集作用于前缀key的去前缀列名")
    void testAutoExcludeSetWithPrefixKeys() {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("_gt_age", 18);
        req.put("_eq_password", "secret");
        Set<String> exclude = new HashSet<>(Arrays.asList("password"));
        stub.auto(req, exclude);
        String sql = getSql();
        assertTrue(sql.contains("age > 18"));
        assertFalse(sql.contains("password"));
    }

    // ========== 链式调用测试 ==========

    @Test
    @DisplayName("auto - 返回值支持链式调用")
    void testAutoChaining() {
        Map<String, Object> req1 = new LinkedHashMap<>();
        req1.put("status", 1);
        StubCondAuto result = stub.auto(req1);
        assertSame(stub, result);
    }

    // ========== Stub实现 ==========

    static class StubCondAuto implements ICondAuto<StubCondAuto> {
        final Condition condition = Condition.where();

        @Override
        public void addChildren(com.dlz.db.modal.condition.Condition child) {
            condition.addChildren(child);
        }

        @Override
        public String getTableName() {
            return null;
        }

        @Override
        public StubCondAuto me() {
            return this;
        }
    }
}
