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
        assertEquals("where STATUS = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 eq() - 动态条件true")
    void testEqDynamicTrue() {
        Condition result = condition.eq(true, TestUser::getStatus, 1);
        
        assertNotNull(result);
        assertEquals("where STATUS = 1", getSql(result));
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
        assertEquals("where STATUS <> 0", getSql(result));
    }

    // ========== 大小比较测试 ==========

    @Test
    @DisplayName("测试 gt() - 大于")
    void testGt() {
        Condition result = condition.gt(TestUser::getAge, 18);
        
        assertNotNull(result);
        assertEquals("where AGE > 18", getSql(result));
    }

    @Test
    @DisplayName("测试 ge() - 大于等于")
    void testGe() {
        Condition result = condition.ge(TestUser::getAge, 18);
        
        assertNotNull(result);
        assertEquals("where AGE >= 18", getSql(result));
    }

    @Test
    @DisplayName("测试 lt() - 小于")
    void testLt() {
        Condition result = condition.lt(TestUser::getAge, 60);
        
        assertNotNull(result);
        assertEquals("where AGE < 60", getSql(result));
    }

    @Test
    @DisplayName("测试 le() - 小于等于")
    void testLe() {
        Condition result = condition.le(TestUser::getAge, 60);
        
        assertNotNull(result);
        assertEquals("where AGE <= 60", getSql(result));
    }

    // ========== LIKE 系列测试 ==========

    @Test
    @DisplayName("测试 like() - 双侧模糊")
    void testLike() {
        Condition result = condition.like(TestUser::getName, "张");
        
        assertNotNull(result);
        assertEquals("where NAME like '%张%'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeLeft() - 右模糊")
    void testLikeLeft() {
        Condition result = condition.likeLeft(TestUser::getName, "张");
        
        assertNotNull(result);
        assertEquals("where NAME like '%张'", getSql(result));
    }

    @Test
    @DisplayName("测试 likeRight() - 左模糊")
    void testLikeRight() {
        Condition result = condition.likeRight(TestUser::getEmail, "@qq.com");
        
        assertNotNull(result);
        assertEquals("where EMAIL like '@qq.com%'", getSql(result));
    }

    @Test
    @DisplayName("测试 notLike() - 不匹配")
    void testNotLike() {
        Condition result = condition.notLike(TestUser::getName, "测试");
        
        assertNotNull(result);
        assertEquals("where NAME not like '%测试%'", getSql(result));
    }

    // ========== IN / NOT IN 测试 ==========

    @Test
    @DisplayName("测试 in() - 逗号分隔字符串")
    void testInString() {
        Condition result = condition.in(TestUser::getId, "1,2,3");
        
        assertNotNull(result);
        assertEquals("where ID in (1,2,3)", getSql(result));
    }

    @Test
    @DisplayName("测试 in() - List集合")
    void testInList() {
        Condition result = condition.in(TestUser::getId, Arrays.asList(1, 2, 3));
        
        assertNotNull(result);
        assertTrue(getSql(result).contains("ID in"));
    }

    @Test
    @DisplayName("测试 notIn() - 不在范围内")
    void testNotIn() {
        Condition result = condition.notIn(TestUser::getStatus, "1,2,3");
        
        assertNotNull(result);
        assertTrue(getSql(result).contains("STATUS not in"));
    }

    // ========== BETWEEN / NOT BETWEEN 测试 ==========

    @Test
    @DisplayName("测试 between() - 两个参数")
    void testBetweenTwoParams() {
        Condition result = condition.between(TestUser::getAge, 18, 60);
        
        assertNotNull(result);
        assertEquals("where AGE between 18 and 60", getSql(result));
    }

    @Test
    @DisplayName("测试 between() - 单值字符串")
    void testBetweenSingleValue() {
        Condition result = condition.between(TestUser::getAge, "18,60");
        
        assertNotNull(result);
        assertEquals("where AGE between '18' and '60'", getSql(result));
    }

    @Test
    @DisplayName("测试 notBetween() - 不在范围内")
    void testNotBetween() {
        Condition result = condition.notBetween(TestUser::getAge, 18, 60);
        
        assertNotNull(result);
        assertEquals("where AGE not between 18 and 60", getSql(result));
    }

    // ========== IS NULL / IS NOT NULL 测试 ==========

    @Test
    @DisplayName("测试 isNull() - 为空")
    void testIsNull() {
        Condition result = condition.isNull(TestUser::getEmail);
        
        assertNotNull(result);
        assertEquals("where EMAIL is null", getSql(result));
    }

    @Test
    @DisplayName("测试 isNotNull() - 不为空")
    void testIsNotNull() {
        Condition result = condition.isNotNull(TestUser::getEmail);
        
        assertNotNull(result);
        assertEquals("where EMAIL is not null", getSql(result));
    }

    // ========== 自定义操作符测试 ==========

    @Test
    @DisplayName("测试 op() - 自定义eq操作符")
    void testOpEq() {
        Condition result = condition.op(TestUser::getStatus, DbOperateEnum.eq, 1);
        
        assertNotNull(result);
        assertEquals("where STATUS = 1", getSql(result));
    }

    @Test
    @DisplayName("测试 op() - 自定义gt操作符")
    void testOpGt() {
        Condition result = condition.op(TestUser::getAge, DbOperateEnum.gt, 18);
        
        assertNotNull(result);
        assertEquals("where AGE > 18", getSql(result));
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
        assertTrue(sql.contains("STATUS = 1"));
        assertTrue(sql.contains("AGE > 18"));
        assertTrue(sql.contains("NAME like"));
        assertTrue(sql.contains("EMAIL is not null"));
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
        assertTrue(sql.contains("STATUS = 1"));
        assertFalse(sql.contains("NAME"), "name为null应该跳过");
        assertTrue(sql.contains("AGE > 25"));
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
        assertEquals("where ID = 100 and (STATUS = 1 and (AGE > 18 or EMAIL is not null))", getSql(result));
    }

    // ========== 边界情况测试 ==========

    @Test
    @DisplayName("测试 - null值")
    void testNullValue() {
        Condition result = condition.eq(TestUser::getStatus, null);
        
        assertNotNull(result);
        // null值会被正确处理
    }

    @Test
    @DisplayName("测试 - Lambda类型安全")
    void testLambdaTypeSafety() {
        // 编译期类型检查，如果属性不存在会编译错误
        Condition result = condition.eq(TestUser::getName, "test");
        
        assertNotNull(result);
        assertEquals("where NAME = 'test'", getSql(result));
    }
}
