package com.dlz.test.db.cases.inf;

import com.dlz.db.enums.DbOperateEnum;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.test.db.entity.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ICondAddByLamda 接口测试 - 基于Lambda字段引用的条件构造
 */
@DisplayName("ICondAddByLamda Lambda条件测试")
class ICondAddByLamdaTest {

    private Condition condition;

    @BeforeEach
    void setUp() {
        condition = Condition.where(null);
    }

    public String getSql(Condition result) {
        final ParaMap pm = new ParaMap();
        pm.getSqlItem().setSqlRun(result.getRunsql(pm));
        final String runSql = pm.jdbcSql().toRunSql();
        return SqlUtil.replaceSql(runSql, pm.getPara(), 1);
    }

    // ========== EQ / NE 测试 ==========

    @Test
    @DisplayName("测试 eq() - Lambda引用")
    void testEq() {
        Condition result = condition.eq(TestUser::getStatus, 1);
        
        assertNotNull(result);
        assertEquals("WHERE status = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 eq() - 动态条件true")
    void testEqDynamicTrue() {
        Condition result = condition.eq(true, TestUser::getStatus, 1);
        
        assertNotNull(result);
        assertEquals("WHERE status = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 eq() - 动态条件false")
    void testEqDynamicFalse() {
        Condition result = condition.eq(false, TestUser::getStatus, 1);
        
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 ne() - 不等于")
    void testNe() {
        Condition result = condition.ne(TestUser::getStatus, 0);
        
        assertNotNull(result);
        assertEquals("WHERE status <> 0", getSql(result));
    }

    // ========== 大小比较测试 ==========

    @Test
    @DisplayName("测试 gt() - 大于")
    void testGt() {
        Condition result = condition.gt(TestUser::getAge, 18);
        
        assertNotNull(result);
        assertEquals("WHERE age > 18", getSql(result));
    }

    @Test
    @DisplayName("测试 ge() - 大于等于")
    void testGe() {
        Condition result = condition.ge(TestUser::getAge, 18);
        
        assertNotNull(result);
        assertEquals("WHERE age >= 18", getSql(result));
    }

    @Test
    @DisplayName("测试 lt() - 小于")
    void testLt() {
        Condition result = condition.lt(TestUser::getAge, 60);
        
        assertNotNull(result);
        assertEquals("WHERE age < 60", getSql(result));
    }

    @Test
    @DisplayName("测试 le() - 小于等于")
    void testLe() {
        Condition result = condition.le(TestUser::getAge, 60);
        
        assertNotNull(result);
        assertEquals("WHERE age <= 60", getSql(result));
    }

    // ========== LIKE 系列测试 ==========

    @Test
    @DisplayName("测试 like() - 双侧模糊")
    void testLike() {
        Condition result = condition.like(TestUser::getName, "张");
        
        assertNotNull(result);
        assertEquals("WHERE name LIKE '%张%'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeLeft() - 右模糊")
    void testLikeLeft() {
        Condition result = condition.likeLeft(TestUser::getName, "张");
        
        assertNotNull(result);
        assertEquals("WHERE name LIKE '%张'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeRight() - 左模糊")
    void testLikeRight() {
        Condition result = condition.likeRight(TestUser::getEmail, "@qq.com");
        
        assertNotNull(result);
        assertEquals("WHERE email LIKE '@qq.com%'", getSql(result));
    }

    @Test
    @DisplayName("测试 notLike() - 不匹配")
    void testNotLike() {
        Condition result = condition.notLike(TestUser::getName, "测试");
        
        assertNotNull(result);
        assertEquals("WHERE name NOT LIKE '%测试%'", getSql(result));
    }

    // ========== IN / NOT IN 测试 ==========

    @Test
    @DisplayName("测试 in() - 逗号分隔字符串")
    void testInString() {
        Condition result = condition.in(TestUser::getId, "1,2,3");

        assertNotNull(result);
        assertEquals("WHERE id IN (1,2,3)", getSql(result));
    }

    @Test
    @DisplayName("测试 in() - List集合")
    void testInList() {
        Condition result = condition.in(TestUser::getId, Arrays.asList(1, 2, 3));

        assertNotNull(result);
        assertTrue(getSql(result).contains("id IN"));
    }

    @Test
    @DisplayName("测试 notIn() - 不在范围内")
    void testNotIn() {
        Condition result = condition.notIn(TestUser::getStatus, "1,2,3");

        assertNotNull(result);
        assertTrue(getSql(result).contains("status NOT IN"));
    }

    // ========== BETWEEN / NOT BETWEEN 测试 ==========

    @Test
    @DisplayName("测试 between() - 两个参数")
    void testBetweenTwoParams() {
        Condition result = condition.between(TestUser::getAge, 18, 60);
        
        assertNotNull(result);
        assertEquals("WHERE age BETWEEN 18 AND 60", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - 单值字符串")
    void testBetweenSingleValue() {
        Condition result = condition.between(TestUser::getAge, "18,60");
        
        assertNotNull(result);
        assertEquals("WHERE age BETWEEN '18' AND '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 不在范围内")
    void testNotBetween() {
        Condition result = condition.notBetween(TestUser::getAge, 18, 60);
        
        assertNotNull(result);
        assertEquals("WHERE age NOT BETWEEN 18 AND 60", getSql(result));
    }

    // ========== IS NULL / IS NOT NULL 测试 ==========

    @Test
    @DisplayName("测试 isNull() - 为空")
    void testIsNull() {
        Condition result = condition.isNull(TestUser::getEmail);
        
        assertNotNull(result);
        assertEquals("WHERE email IS NULL", getSql(result));
    }

    @Test
    @DisplayName("测试 isNotNull() - 不为空")
    void testIsNotNull() {
        Condition result = condition.isNotNull(TestUser::getEmail);
        
        assertNotNull(result);
        assertEquals("WHERE email IS NOT NULL", getSql(result));
    }

    // ========== 自定义操作符测试 ==========

    @Test
    @DisplayName("测试 op() - 自定义eq操作符")
    void testOpEq() {
        Condition result = condition.op(TestUser::getStatus, DbOperateEnum.eq, 1);
        
        assertNotNull(result);
        assertEquals("WHERE status = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 op() - 自定义gt操作符")
    void testOpGt() {
        Condition result = condition.op(TestUser::getAge, DbOperateEnum.gt, 18);
        
        assertNotNull(result);
        assertEquals("WHERE age > 18", getSql(result));
    }

    // ========== 链式调用测试 ==========

    @Test
    @DisplayName("测试链式调用 - 多条件组合")
    void testChainedCall() {
        Condition result = condition
                .eq(TestUser::getStatus, 1)
                .gt(TestUser::getAge, 18)
                .like(TestUser::getName, "张")
                .isNotNull(TestUser::getEmail);
        
        assertNotNull(result);
        String sql = getSql(result);
        assertTrue(sql.contains("status = 1"));
        assertTrue(sql.contains("age > 18"));
        assertTrue(sql.contains("name LIKE"));
        assertTrue(sql.contains("email IS NOT NULL"));
    }

    @Test
    @DisplayName("测试链式调用 - 动态条件混合")
    void testChainedWithDynamic() {
        String name = null;
        Integer age = 25;
        
        Condition result = condition
                .eq(TestUser::getStatus, 1)
                .eq(name != null, TestUser::getName, name)
                .gt(age != null, TestUser::getAge, age);
        
        assertNotNull(result);
        String sql = getSql(result);
        assertTrue(sql.contains("status = 1"));
        assertFalse(sql.contains("name"), "name为null应该跳过");
        assertTrue(sql.contains("age > 25"));
    }

    @Test
    @DisplayName("测试链式调用 - 复杂嵌套条件")
    void testComplexCondition() {
        Condition result = condition
                .eq(TestUser::getId, 100L)
                .ands(c -> c
                    .eq(TestUser::getStatus, 1)
                    .ors(o -> o
                        .gt(TestUser::getAge, 18)
                        .isNotNull(TestUser::getEmail)
                    )
                );
        
        assertNotNull(result);
        assertEquals("WHERE id = 100 AND (status = 1 AND (age > 18 OR email IS NOT NULL))", getSql(result));
    }

    // ========== 动态条件false分支 ==========

    @Test
    @DisplayName("测试 ne() - 动态条件true")
    void testNeDynamicTrue() {
        Condition result = condition.ne(true, TestUser::getStatus, 0);
        assertNotNull(result);
        assertEquals("WHERE status <> 0", getSql(result));
    }

    @Test
    @DisplayName("测试 ne() - 动态条件false")
    void testNeDynamicFalse() {
        Condition result = condition.ne(false, TestUser::getStatus, 0);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 gt() - 动态条件true")
    void testGtDynamicTrue() {
        Condition result = condition.gt(true, TestUser::getAge, 18);
        assertNotNull(result);
        assertEquals("WHERE age > 18", getSql(result));
    }

    @Test
    @DisplayName("测试 gt() - 动态条件false")
    void testGtDynamicFalse() {
        Condition result = condition.gt(false, TestUser::getAge, 18);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 ge() - 动态条件true")
    void testGeDynamicTrue() {
        Condition result = condition.ge(true, TestUser::getAge, 18);
        assertNotNull(result);
        assertEquals("WHERE age >= 18", getSql(result));
    }

    @Test
    @DisplayName("测试 ge() - 动态条件false")
    void testGeDynamicFalse() {
        Condition result = condition.ge(false, TestUser::getAge, 18);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 lt() - 动态条件true")
    void testLtDynamicTrue() {
        Condition result = condition.lt(true, TestUser::getAge, 60);
        assertNotNull(result);
        assertEquals("WHERE age < 60", getSql(result));
    }

    @Test
    @DisplayName("测试 lt() - 动态条件false")
    void testLtDynamicFalse() {
        Condition result = condition.lt(false, TestUser::getAge, 60);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 le() - 动态条件true")
    void testLeDynamicTrue() {
        Condition result = condition.le(true, TestUser::getAge, 60);
        assertNotNull(result);
        assertEquals("WHERE age <= 60", getSql(result));
    }

    @Test
    @DisplayName("测试 le() - 动态条件false")
    void testLeDynamicFalse() {
        Condition result = condition.le(false, TestUser::getAge, 60);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== LIKE 动态分支 ==========

    @Test
    @DisplayName("测试 like() - 动态条件true")
    void testLikeDynamicTrue() {
        Condition result = condition.like(true, TestUser::getName, "张");
        assertNotNull(result);
        assertEquals("WHERE name LIKE '%张%'", getSql(result));
    }

    @Test
    @DisplayName("测试 like() - 动态条件false")
    void testLikeDynamicFalse() {
        Condition result = condition.like(false, TestUser::getName, "张");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 likeLeft() - 动态条件true")
    void testLikeLeftDynamicTrue() {
        Condition result = condition.likeLeft(true, TestUser::getName, "张");
        assertNotNull(result);
        assertEquals("WHERE name LIKE '%张'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeLeft() - 动态条件false")
    void testLikeLeftDynamicFalse() {
        Condition result = condition.likeLeft(false, TestUser::getName, "张");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 likeRight() - 动态条件true")
    void testLikeRightDynamicTrue() {
        Condition result = condition.likeRight(true, TestUser::getEmail, "@qq.com");
        assertNotNull(result);
        assertEquals("WHERE email LIKE '@qq.com%'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeRight() - 动态条件false")
    void testLikeRightDynamicFalse() {
        Condition result = condition.likeRight(false, TestUser::getEmail, "@qq.com");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 notLike() - 动态条件true")
    void testNotLikeDynamicTrue() {
        Condition result = condition.notLike(true, TestUser::getName, "测试");
        assertNotNull(result);
        assertEquals("WHERE name NOT LIKE '%测试%'", getSql(result));
    }

    @Test
    @DisplayName("测试 notLike() - 动态条件false")
    void testNotLikeDynamicFalse() {
        Condition result = condition.notLike(false, TestUser::getName, "测试");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== IN/NOT IN 动态分支 ==========

    @Test
    @DisplayName("测试 in() - 动态条件true")
    void testInDynamicTrue() {
        Condition result = condition.in(true, TestUser::getId, "1,2,3");
        assertNotNull(result);
        assertEquals("WHERE id IN (1,2,3)", getSql(result));
    }

    @Test
    @DisplayName("测试 in() - 动态条件false")
    void testInDynamicFalse() {
        Condition result = condition.in(false, TestUser::getId, "1,2,3");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 notIn() - 动态条件true")
    void testNotInDynamicTrue() {
        Condition result = condition.notIn(true, TestUser::getStatus, "1,2,3");
        assertNotNull(result);
        assertTrue(getSql(result).contains("status NOT IN"));
    }

    @Test
    @DisplayName("测试 notIn() - 动态条件false")
    void testNotInDynamicFalse() {
        Condition result = condition.notIn(false, TestUser::getStatus, "1,2,3");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== BETWEEN/NOT BETWEEN 补充 ==========

    @Test
    @DisplayName("测试 between() - 动态条件true")
    void testBetweenDynamicTrue() {
        Condition result = condition.between(true, TestUser::getAge, 18, 60);
        assertNotNull(result);
        assertEquals("WHERE age BETWEEN 18 AND 60", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - 动态条件false")
    void testBetweenDynamicFalse() {
        Condition result = condition.between(false, TestUser::getAge, 18, 60);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - 单值动态true")
    void testBetweenSingleValueDynamicTrue() {
        Condition result = condition.between(true, TestUser::getAge, "18,60");
        assertNotNull(result);
        assertEquals("WHERE age BETWEEN '18' AND '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - 单值动态false")
    void testBetweenSingleValueDynamicFalse() {
        Condition result = condition.between(false, TestUser::getAge, "18,60");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 动态条件true")
    void testNotBetweenDynamicTrue() {
        Condition result = condition.notBetween(true, TestUser::getAge, 18, 60);
        assertNotNull(result);
        assertEquals("WHERE age NOT BETWEEN 18 AND 60", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 动态条件false")
    void testNotBetweenDynamicFalse() {
        Condition result = condition.notBetween(false, TestUser::getAge, 18, 60);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 单值")
    void testNotBetweenSingleValue() {
        Condition result = condition.notBetween(TestUser::getAge, "18,60");
        assertNotNull(result);
        assertEquals("WHERE age NOT BETWEEN '18' AND '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 单值动态true")
    void testNotBetweenSingleValueDynamicTrue() {
        Condition result = condition.notBetween(true, TestUser::getAge, "18,60");
        assertNotNull(result);
        assertEquals("WHERE age NOT BETWEEN '18' AND '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 单值动态false")
    void testNotBetweenSingleValueDynamicFalse() {
        Condition result = condition.notBetween(false, TestUser::getAge, "18,60");
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== IS NULL / IS NOT NULL 动态分支 ==========

    @Test
    @DisplayName("测试 isNull() - 动态条件true")
    void testIsNullDynamicTrue() {
        Condition result = condition.isNull(true, TestUser::getEmail);
        assertNotNull(result);
        assertEquals("WHERE email IS NULL", getSql(result));
    }

    @Test
    @DisplayName("测试 isNull() - 动态条件false")
    void testIsNullDynamicFalse() {
        Condition result = condition.isNull(false, TestUser::getEmail);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    @Test
    @DisplayName("测试 isNotNull() - 动态条件true")
    void testIsNotNullDynamicTrue() {
        Condition result = condition.isNotNull(true, TestUser::getEmail);
        assertNotNull(result);
        assertEquals("WHERE email IS NOT NULL", getSql(result));
    }

    @Test
    @DisplayName("测试 isNotNull() - 动态条件false")
    void testIsNotNullDynamicFalse() {
        Condition result = condition.isNotNull(false, TestUser::getEmail);
        assertNotNull(result);
        assertEquals("", getSql(result));
    }

    // ========== 自定义操作符补充 ==========

    @Test
    @DisplayName("测试 op() - like操作符")
    void testOpLike() {
        Condition result = condition.op(TestUser::getName, DbOperateEnum.like, "张");
        assertNotNull(result);
        assertTrue(getSql(result).contains("name LIKE"));
    }

    @Test
    @DisplayName("测试 op() - isNull操作符")
    void testOpIsNull() {
        Condition result = condition.op(TestUser::getEmail, DbOperateEnum.isNull, null);
        assertNotNull(result);
        assertEquals("WHERE email IS NULL", getSql(result));
    }

    // ========== 边界情况测试 ==========

    @Test
    @DisplayName("测试 - null值")
    void testNullValue() {
        Condition result = condition.eq(TestUser::getStatus, null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 - Lambda类型安全")
    void testLambdaTypeSafety() {
        Condition result = condition.eq(TestUser::getName, "test");
        assertNotNull(result);
        assertEquals("WHERE name = 'test'", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - List参数")
    void testBetweenList() {
        Condition result = condition.between(TestUser::getAge, Arrays.asList(18, 60));
        assertNotNull(result);
        assertTrue(getSql(result).contains("age BETWEEN"));
    }

    @Test
    @DisplayName("测试 in() - sql子查询")
    void testInSqlSubquery() {
        Condition result = condition.in(TestUser::getId, "sql:SELECT id FROM vip");
        assertNotNull(result);
        assertTrue(getSql(result).contains("id IN"));
    }
}
