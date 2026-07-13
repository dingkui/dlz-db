package com.dlz.test.db.cases.inf;

import com.dlz.db.enums.DbBuildEnum;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.json.JSONMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ICondAndOr 接口测试 - AND/OR 嵌套条件构造
 */
@DisplayName("ICondAndOr AND/OR 嵌套测试")
class ICondAndOrTest {

    private Condition condition;

    @BeforeEach
    void setUp() {
        condition = Condition.where();
    }

    public String getSql(Condition result) {
        final ParaMap pm = new ParaMap();
        pm.getSqlItem().setSqlRun(result.getRunsql(pm));
        final String runSql = pm.jdbcSql().toRunSql();
        return SqlUtil.replaceSql(runSql, pm.getPara(), 1);
    }

    // ========== AND 嵌套测试 ==========

    @Test
    @DisplayName("测试 ands() - 简单AND嵌套")
    void testAndSimple() {
        Condition result = condition.ands(c -> c.eq("status", 1).gt("age", 18));
        assertNotNull(result);
        assertEquals("WHERE (status = 1 AND age > 18)", getSql(result));
    }

    @Test
    @DisplayName("测试 ands() - 单个条件")
    void testAndSingleCondition() {
        Condition result = condition.ands(c -> c.eq("status", 1));
        
        assertNotNull(result);
        assertEquals("WHERE status = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 ands() - 多个AND嵌套")
    void testAndMultiple() {
        Condition result = condition
                .eq("tenantId", 100)
                .ands(c -> c.eq("status", 1).gt("age", 18))
                .ands(c -> c.like("name", "张").isNotNull("email"));
        
        assertNotNull(result);
        assertEquals("WHERE tenant_id = 100 AND (status = 1 AND age > 18) AND (name LIKE '%张%' AND email IS NOT NULL)", getSql(result));
    }

    // ========== OR 嵌套测试 ==========

    @Test
    @DisplayName("测试 ors() - 简单OR嵌套")
    void testOrSimple() {
        Condition result = condition.ors(c -> c.eq("status", 1).eq("status", 2));
        
        assertNotNull(result);
        assertEquals("WHERE (status = 1 OR status = 2)", getSql(result));
    }

    @Test
    @DisplayName("测试 ors() - 单个条件")
    void testOrSingleCondition() {
        Condition result = condition.ors(c -> c.eq("status", 1));
        
        assertNotNull(result);
        assertEquals("WHERE status = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 ors() - 多个OR嵌套")
    void testOrMultiple() {
        Condition result = condition
                .ors(c -> c.eq("type", 1).eq("type", 2))
                .ors(c -> c.gt("age", 60).lt("age", 18));
        
        assertNotNull(result);
        assertEquals("WHERE (type = 1 OR type = 2) AND (age > 60 OR age < 18)", getSql(result));
    }

    // ========== AND/OR 混合嵌套测试 ==========

    @Test
    @DisplayName("测试 and/or 混合 - A AND (B OR C)")
    void testAndOrMixed1() {
        Condition result = condition
                .eq("status", 1)
                .ors(c -> c.like("name", "张").like("mobile", "138"));
        
        assertNotNull(result);
        assertEquals("WHERE status = 1 AND (name LIKE '%张%' OR mobile LIKE '%138%')", getSql(result));
    }

    @Test
    @DisplayName("测试 and/or 混合 - (A AND B) OR (C AND D)")
    void testAndOrMixed2() {
        Condition result = condition.ors(c -> 
            c.ands(a -> a.eq("type", 1).gt("age", 18))
             .ands(a -> a.eq("type", 2).lt("age", 60))
        );
        
        assertNotNull(result);
        assertEquals("WHERE ((type = 1 AND age > 18) OR (type = 2 AND age < 60))", getSql(result));
    }

    @Test
    @DisplayName("测试 and/or 混合 - 复杂嵌套")
    void testAndOrComplex() {
        Condition result = condition
                .eq("tenantId", 100)
                .ands(c -> c
                    .eq("status", 1)
                    .ors(o -> o
                        .gt("age", 18)
                        .isNotNull("email")
                    )
                );
        
        assertNotNull(result);
        assertEquals("WHERE tenant_id = 100 AND (status = 1 AND (age > 18 OR email IS NOT NULL))", getSql(result));
    }

    @Test
    @DisplayName("测试 and/or 混合 - 多层嵌套")
    void testAndOrMultiLevel() {
        Condition result = condition
                .eq("status", 1)
                .ors(c -> c
                    .ands(a -> a.gt("age", 18).lt("age", 60))
                    .ors(o -> o.isNotNull("vip_level").eq("level", 3))
                );
        
        assertNotNull(result);
        assertEquals("WHERE status = 1 AND ((age > 18 AND age < 60) OR (vip_level IS NOT NULL OR level = 3))", getSql(result));
    }

    // ========== 边界情况测试 ==========

    @Test
    @DisplayName("测试 ands() - 空lambda")
    void testAndEmptyLambda() {
        Condition result = condition.ands(c -> {});
        
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 ors() - 空lambda")
    void testOrEmptyLambda() {
        Condition result = condition.ors(c -> {});
        
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试链式调用 - and后继续链式")
    void testChainedAfterAnd() {
        Condition result = condition
                .ands(c -> c.eq("status", 1))
                .gt("age", 18)
                .like("name", "张");
        
        assertNotNull(result);
        assertEquals("WHERE status = 1 AND age > 18 AND name LIKE '%张%'", getSql(result));
    }

    @Test
    @DisplayName("测试链式调用 - or后继续链式")
    void testChainedAfterOr() {
        Condition result = condition
                .ors(c -> c.eq("type", 1))
                .eq("status", 1)
                .isNotNull("email");
        
        assertNotNull(result);
        assertEquals("WHERE type = 1 AND status = 1 AND email IS NOT NULL", getSql(result));
    }
}
