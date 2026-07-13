package com.dlz.test.db.cases.inf;

import com.dlz.db.enums.DbOperateEnum;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.exception.ValidateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ICondAddByKey 接口测试 - 基于字符串列名的条件构造
 */
@DisplayName("ICondAddByKey 字符串列名条件测试")
class ICondAddByKeyTest {

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

    // ========== EQ / NE 测试 ==========

    @Test
    @DisplayName("测试 eq() - 基础相等")
    void testEq() {
        Condition result = condition.eq("status", 1);
        
        assertNotNull(result);
        assertEquals("WHERE status = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 eq() - 动态条件true")
    void testEqDynamicTrue() {
        Condition result = condition.eq(true, "status", 1);
        
        assertNotNull(result);
        assertEquals("WHERE status = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 eq() - 动态条件false")
    void testEqDynamicFalse() {
        Condition result = condition.eq(false, "status", 1);
        
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 ne() - 不等于")
    void testNe() {
        Condition result = condition.ne("status", 0);
        
        assertNotNull(result);
        assertEquals("WHERE status <> 0", getSql(result));
    }

    @Test
    @DisplayName("测试 ne() - 动态条件")
    void testNeDynamic() {
        Condition result = condition.ne(true, "status", 0);
        
        assertNotNull(result);
        assertEquals("WHERE status <> 0", getSql(result));
    }

    // ========== 大小比较测试 ==========

    @Test
    @DisplayName("测试 gt() - 大于")
    void testGt() {
        Condition result = condition.gt("age", 18);
        
        assertNotNull(result);
        assertEquals("WHERE age > 18", getSql(result));
    }

    @Test
    @DisplayName("测试 ge() - 大于等于")
    void testGe() {
        Condition result = condition.ge("age", 18);
        
        assertNotNull(result);
        assertEquals("WHERE age >= 18", getSql(result));
    }

    @Test
    @DisplayName("测试 lt() - 小于")
    void testLt() {
        Condition result = condition.lt("age", 60);
        
        assertNotNull(result);
        assertEquals("WHERE age < 60", getSql(result));
    }

    @Test
    @DisplayName("测试 le() - 小于等于")
    void testLe() {
        Condition result = condition.le("age", 60);
        
        assertNotNull(result);
        assertEquals("WHERE age <= 60", getSql(result));
    }

    @Test
    @DisplayName("测试 gt() - 动态条件")
    void testGtDynamic() {
        Condition result = condition.gt(true, "age", 18);
        
        assertNotNull(result);
        assertEquals("WHERE age > 18", getSql(result));
    }

    // ========== LIKE 系列测试 ==========

    @Test
    @DisplayName("测试 like() - 双侧模糊")
    void testLike() {
        Condition result = condition.like("name", "张");
        
        assertNotNull(result);
        assertEquals("WHERE name LIKE '%张%'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeLeft() - 右模糊")
    void testLikeLeft() {
        Condition result = condition.likeLeft("name", "张");
        
        assertNotNull(result);
        assertEquals("WHERE name LIKE '%张'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeRight() - 左模糊")
    void testLikeRight() {
        Condition result = condition.likeRight("email", "@qq.com");
        
        assertNotNull(result);
        assertEquals("WHERE email LIKE '@qq.com%'", getSql(result));
    }

    @Test
    @DisplayName("测试 notLike() - 不匹配")
    void testNotLike() {
        Condition result = condition.notLike("name", "测试");
        
        assertNotNull(result);
        assertEquals("WHERE name NOT LIKE '%测试%'", getSql(result));
    }

    @Test
    @DisplayName("测试 like() - 动态条件")
    void testLikeDynamic() {
        Condition result = condition.like(true, "name", "张");
        
        assertNotNull(result);
        assertEquals("WHERE name LIKE '%张%'", getSql(result));
    }

    // ========== IN / NOT IN 测试 ==========

    @Test
    @DisplayName("测试 in() - 逗号分隔字符串")
    void testInString() {
        Condition result = condition.in("status", "1,2,3");
        
        assertNotNull(result);
        assertEquals("WHERE status IN (1,2,3)", getSql(result));
    }

    @Test
    @DisplayName("测试 in() - List集合")
    void testInList() {
        Condition result = condition.in("status", Arrays.asList(1, 2, 3));
        
        assertNotNull(result);
        assertTrue(getSql(result).contains("status IN"));
    }

    @Test
    @DisplayName("测试 in() - 动态条件")
    void testInDynamic() {
        Condition result = condition.in(true, "status", "1,2,3");
        
        assertNotNull(result);
        assertEquals("WHERE status IN (1,2,3)", getSql(result));
    }

    @Test
    @DisplayName("测试 notIn() - 不在范围内")
    void testNotIn() {
        Condition result = condition.notIn("status", "1,2,3");
        
        assertNotNull(result);
        assertEquals("WHERE status NOT IN (1,2,3)", getSql(result));
    }

    // ========== BETWEEN / NOT BETWEEN 测试 ==========

    @Test
    @DisplayName("测试 between() - 两个参数")
    void testBetweenTwoParams() {
        Condition result = condition.between("age", 18, 60);
        
        assertNotNull(result);
        assertEquals("WHERE age BETWEEN 18 AND 60", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - 单值字符串")
    void testBetweenSingleValue() {
        Condition result = condition.between("age", "18,60");
        
        assertNotNull(result);
        assertEquals("WHERE age BETWEEN '18' AND '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - List")
    void testBetweenList() {
        Condition result = condition.between("age", Arrays.asList(18, 60));
        
        assertNotNull(result);
        assertTrue(getSql(result).contains("age BETWEEN"));
    }

    @Test
    @DisplayName("测试 between() - 动态条件")
    void testBetweenDynamic() {
        Condition result = condition.between(true, "age", 18, 60);
        
        assertNotNull(result);
        assertEquals("WHERE age BETWEEN 18 AND 60", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 不在范围内")
    void testNotBetween() {
        Condition result = condition.notBetween("age", 18, 60);
        
        assertNotNull(result);
        assertEquals("WHERE age NOT BETWEEN 18 AND 60", getSql(result));
    }

    // ========== IS NULL / IS NOT NULL 测试 ==========

    @Test
    @DisplayName("测试 isNull() - 为空")
    void testIsNull() {
        Condition result = condition.isNull("delete_time");
        
        assertNotNull(result);
        assertEquals("WHERE delete_time IS NULL", getSql(result));
    }

    @Test
    @DisplayName("测试 isNull() - 动态条件")
    void testIsNullDynamic() {
        Condition result = condition.isNull(true, "delete_time");
        
        assertNotNull(result);
        assertEquals("WHERE delete_time IS NULL", getSql(result));
    }

    @Test
    @DisplayName("测试 isNotNull() - 不为空")
    void testIsNotNull() {
        Condition result = condition.isNotNull("email");
        
        assertNotNull(result);
        assertEquals("WHERE email IS NOT NULL", getSql(result));
    }

    @Test
    @DisplayName("测试 isNotNull() - 动态条件")
    void testIsNotNullDynamic() {
        Condition result = condition.isNotNull(true, "email");
        
        assertNotNull(result);
        assertEquals("WHERE email IS NOT NULL", getSql(result));
    }

    // ========== 自定义操作符测试 ==========

    @Test
    @DisplayName("测试 op() - 自定义eq操作符")
    void testOpEq() {
        Condition result = condition.op("status", DbOperateEnum.eq, 1);
        
        assertNotNull(result);
        assertEquals("WHERE status = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 op() - 自定义gt操作符")
    void testOpGt() {
        Condition result = condition.op("age", DbOperateEnum.gt, 18);
        
        assertNotNull(result);
        assertEquals("WHERE age > 18", getSql(result));
    }

    // ========== 链式调用测试 ==========

    @Test
    @DisplayName("测试链式调用 - 多条件组合")
    void testChainedCall() {
        Condition result = condition
                .eq("status", 1)
                .gt("age", 18)
                .like("name", "张")
                .in("role", "admin,user");
        
        assertNotNull(result);
        String sql = getSql(result);
        assertTrue(sql.contains("status = 1"));
        assertTrue(sql.contains("age > 18"));
        assertTrue(sql.contains("name LIKE"));
        assertTrue(sql.contains("role IN"));
    }

    @Test
    @DisplayName("测试链式调用 - 动态条件混合")
    void testChainedWithDynamic() {
        String name = null;
        Integer age = 25;
        
        Condition result = condition
                .eq("status", 1)
                .eq(name != null, "name", name)
                .gt(age != null, "age", age);
        
        assertNotNull(result);
        String sql = getSql(result);
        assertTrue(sql.contains("status = 1"));
        assertFalse(sql.contains("name"), "name为null应该跳过");
        assertTrue(sql.contains("age > 25"));
    }

    @Test
    @DisplayName("测试链式调用 - 复杂条件")
    void testComplexCondition() {
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

    // ========== 动态条件false分支 ==========

    @Test
    @DisplayName("测试 ne() - 动态条件false")
    void testNeDynamicFalse() {
        Condition result = condition.ne(false, "status", 0);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 gt() - 动态条件false")
    void testGtDynamicFalse() {
        Condition result = condition.gt(false, "age", 18);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 ge() - 动态条件true")
    void testGeDynamicTrue() {
        Condition result = condition.ge(true, "age", 18);
        assertNotNull(result);
        assertEquals("WHERE age >= 18", getSql(result));
    }

    @Test
    @DisplayName("测试 ge() - 动态条件false")
    void testGeDynamicFalse() {
        Condition result = condition.ge(false, "age", 18);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 lt() - 动态条件true")
    void testLtDynamicTrue() {
        Condition result = condition.lt(true, "age", 60);
        assertNotNull(result);
        assertEquals("WHERE age < 60", getSql(result));
    }

    @Test
    @DisplayName("测试 lt() - 动态条件false")
    void testLtDynamicFalse() {
        Condition result = condition.lt(false, "age", 60);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 le() - 动态条件true")
    void testLeDynamicTrue() {
        Condition result = condition.le(true, "age", 60);
        assertNotNull(result);
        assertEquals("WHERE age <= 60", getSql(result));
    }

    @Test
    @DisplayName("测试 le() - 动态条件false")
    void testLeDynamicFalse() {
        Condition result = condition.le(false, "age", 60);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== LIKE 动态false ==========

    @Test
    @DisplayName("测试 like() - 动态条件false")
    void testLikeDynamicFalse() {
        Condition result = condition.like(false, "name", "张");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 likeLeft() - 动态条件true")
    void testLikeLeftDynamicTrue() {
        Condition result = condition.likeLeft(true, "name", "张");
        assertNotNull(result);
        assertEquals("WHERE name LIKE '%张'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeLeft() - 动态条件false")
    void testLikeLeftDynamicFalse() {
        Condition result = condition.likeLeft(false, "name", "张");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 likeRight() - 动态条件true")
    void testLikeRightDynamicTrue() {
        Condition result = condition.likeRight(true, "email", "@qq.com");
        assertNotNull(result);
        assertEquals("WHERE email LIKE '@qq.com%'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeRight() - 动态条件false")
    void testLikeRightDynamicFalse() {
        Condition result = condition.likeRight(false, "email", "@qq.com");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 notLike() - 动态条件true")
    void testNotLikeDynamicTrue() {
        Condition result = condition.notLike(true, "name", "测试");
        assertNotNull(result);
        assertEquals("WHERE name NOT LIKE '%测试%'", getSql(result));
    }

    @Test
    @DisplayName("测试 notLike() - 动态条件false")
    void testNotLikeDynamicFalse() {
        Condition result = condition.notLike(false, "name", "测试");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== IN/NOT IN 动态false和补充 ==========

    @Test
    @DisplayName("测试 in() - 动态条件false")
    void testInDynamicFalse() {
        Condition result = condition.in(false, "status", "1,2,3");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 in() - sql子查询")
    void testInSqlSubquery() {
        Condition result = condition.in("status", "sql:SELECT id FROM vip");
        assertNotNull(result);
        assertTrue(getSql(result).contains("status IN"));
    }

    @Test
    @DisplayName("测试 notIn() - 动态条件true")
    void testNotInDynamicTrue() {
        Condition result = condition.notIn(true, "status", "1,2,3");
        assertNotNull(result);
        assertEquals("WHERE status NOT IN (1,2,3)", getSql(result));
    }

    @Test
    @DisplayName("测试 notIn() - 动态条件false")
    void testNotInDynamicFalse() {
        Condition result = condition.notIn(false, "status", "1,2,3");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== BETWEEN/NOT BETWEEN 补充 ==========

    @Test
    @DisplayName("测试 between() - 动态条件两参数false")
    void testBetweenDynamicFalse() {
        Condition result = condition.between(false, "age", 18, 60);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - 单值动态true")
    void testBetweenSingleValueDynamicTrue() {
        Condition result = condition.between(true, "age", "18,60");
        assertNotNull(result);
        assertEquals("WHERE age BETWEEN '18' AND '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - 单值动态false")
    void testBetweenSingleValueDynamicFalse() {
        Condition result = condition.between(false, "age", "18,60");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 动态条件true")
    void testNotBetweenDynamicTrue() {
        Condition result = condition.notBetween(true, "age", 18, 60);
        assertNotNull(result);
        assertEquals("WHERE age NOT BETWEEN 18 AND 60", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 动态条件false")
    void testNotBetweenDynamicFalse() {
        Condition result = condition.notBetween(false, "age", 18, 60);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 单值")
    void testNotBetweenSingleValue() {
        Condition result = condition.notBetween("age", "18,60");
        assertNotNull(result);
        assertEquals("WHERE age NOT BETWEEN '18' AND '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 单值动态true")
    void testNotBetweenSingleValueDynamicTrue() {
        Condition result = condition.notBetween(true, "age", "18,60");
        assertNotNull(result);
        assertEquals("WHERE age NOT BETWEEN '18' AND '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 单值动态false")
    void testNotBetweenSingleValueDynamicFalse() {
        Condition result = condition.notBetween(false, "age", "18,60");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== IS NULL / IS NOT NULL 动态false ==========

    @Test
    @DisplayName("测试 isNull() - 动态条件false")
    void testIsNullDynamicFalse() {
        Condition result = condition.isNull(false, "delete_time");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 isNotNull() - 动态条件false")
    void testIsNotNullDynamicFalse() {
        Condition result = condition.isNotNull(false, "email");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== 自定义操作符补充 ==========

    @Test
    @DisplayName("测试 op() - like操作符")
    void testOpLike() {
        Condition result = condition.op("name", DbOperateEnum.like, "张");
        assertNotNull(result);
        assertTrue(getSql(result).contains("name LIKE"));
    }

    @Test
    @DisplayName("测试 op() - in操作符")
    void testOpIn() {
        Condition result = condition.op("status", DbOperateEnum.in, "1,2,3");
        assertNotNull(result);
        assertTrue(getSql(result).contains("status IN"));
    }

    @Test
    @DisplayName("测试 op() - isNull操作符")
    void testOpIsNull() {
        Condition result = condition.op("email", DbOperateEnum.isNull, null);
        assertNotNull(result);
        assertEquals("WHERE email IS NULL", getSql(result));
    }

    // ========== 边界情况测试 ==========

    @Test
    @DisplayName("测试 - 空字符串列名")
    void testEmptyColumnName() {
        assertThrows(ValidateException.class, () -> condition.eq("", "value"));
    }

    @Test
    @DisplayName("测试 - null值")
    void testNullValue() {
        Condition result = condition.eq("status", null);
        assertNotNull(result);
    }
}
