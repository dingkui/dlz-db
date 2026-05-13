package com.dlz.test.db.inf;

import com.dlz.db.modal.condition.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Condition 条件构造器测试
 * 测试各种条件构造方法
 */
@DisplayName("Condition 条件构造器测试")
class ConditionTest {

    private Condition condition;

    @BeforeEach
    void setUp() {
        condition = Condition.where();
    }

    // ========== EQ / NE 测试 ==========

    @Test
    @DisplayName("测试 eq() 方法")
    void testEq() {
        Condition result = condition.eq("status", 1);
        
        assertNotNull(result);
        assertSame(condition, result, "应该返回同一个对象");
    }

    @Test
    @DisplayName("测试 eq() 动态条件 - true")
    void testEqDynamicTrue() {
        Condition result = condition.eq(true, "status", 1);
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 eq() 动态条件 - false")
    void testEqDynamicFalse() {
        Condition result = condition.eq(false, "status", 1);
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 ne() 方法")
    void testNe() {
        Condition result = condition.ne("status", 0);
        
        assertNotNull(result);
    }

    // ========== 大小比较测试 ==========

    @Test
    @DisplayName("测试 gt() 方法")
    void testGt() {
        Condition result = condition.gt("age", 18);
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 ge() 方法")
    void testGe() {
        Condition result = condition.ge("age", 18);
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 lt() 方法")
    void testLt() {
        Condition result = condition.lt("age", 60);
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 le() 方法")
    void testLe() {
        Condition result = condition.le("age", 60);
        
        assertNotNull(result);
    }

    // ========== LIKE 测试 ==========

    @Test
    @DisplayName("测试 like() 方法")
    void testLike() {
        Condition result = condition.like("name", "%张三%");
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 likeLeft() 方法")
    void testLikeLeft() {
        Condition result = condition.likeLeft("name", "张三%");
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 likeRight() 方法")
    void testLikeRight() {
        Condition result = condition.likeRight("name", "%张三");
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 notLike() 方法")
    void testNotLike() {
        Condition result = condition.notLike("name", "%测试%");
        
        assertNotNull(result);
    }

    // ========== IN / NOT IN 测试 ==========

    @Test
    @DisplayName("测试 in() 方法 - 单值字符串")
    void testInSingleValue() {
        Condition result = condition.in("status", "1,2,3");
        
        assertNotNull(result);
    }



    // ========== BETWEEN 测试 ==========

    @Test
    @DisplayName("测试 between() 方法 - 两个参数")
    void testBetweenTwoParams() {
        Condition result = condition.between("age", 18, 60);
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 between() 方法 - 单值字符串")
    void testBetweenSingleValue() {
        Condition result = condition.between("age", "18,60");
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 notBetween() 方法")
    void testNotBetween() {
        Condition result = condition.notBetween("age", 18, 60);
        
        assertNotNull(result);
    }

    // ========== IS NULL / IS NOT NULL 测试 ==========

    @Test
    @DisplayName("测试 isNull() 方法")
    void testIsNull() {
        Condition result = condition.isNull("delete_time");
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 isNotNull() 方法")
    void testIsNotNull() {
        Condition result = condition.isNotNull("email");
        
        assertNotNull(result);
    }

    // ========== AND/OR 嵌套测试 ==========

    @Test
    @DisplayName("测试 and() 嵌套")
    void testAnd() {
        Condition result = condition.and(c -> c.eq("status", 1).gt("age", 18));
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 or() 嵌套")
    void testOr() {
        Condition result = condition.or(c -> c.eq("status", 1).eq("status", 2));
        
        assertNotNull(result);
    }

    // ========== auto() 自动条件测试 ==========



    // ========== sql/apply 原生 SQL 测试 ==========

    @Test
    @DisplayName("测试 sql() 方法")
    void testSql() {
        com.dlz.kit.json.JSONMap paras = new com.dlz.kit.json.JSONMap();
        Condition result = condition.sql("AND status = 1", paras);
        
        assertNotNull(result);
    }

    // ========== 链式调用测试 ==========

    @Test
    @DisplayName("测试链式调用 - 多条件组合")
    void testChainedCall() {
        Condition result = condition
                .eq("status", 1)
                .gt("age", 18)
                .like("name", "%张%")
                .in("role", "admin,user");
        
        assertNotNull(result);
        assertSame(condition, result);
    }

    @Test
    @DisplayName("测试链式调用 - 复杂条件")
    void testComplexCondition() {
        Condition result = condition
                .eq("tenant_id", 100)
                .and(c -> c
                    .eq("status", 1)
                    .or(o -> o
                        .gt("age", 18)
                        .isNotNull("email")
                    )
                );
        
        assertNotNull(result);
    }

    // ========== clone 测试 ==========

    @Test
    @DisplayName("测试 clone() 方法")
    void testClone() {
        condition.eq("status", 1);
        
        Condition cloned = condition.clone();
        
        assertNotNull(cloned);
        assertNotSame(condition, cloned, "克隆对象应该是新的实例");
    }

    // ========== addPara/addParas 测试 ==========

    @Test
    @DisplayName("测试 addPara() 方法")
    void testAddPara() {
        Condition result = condition.addPara("key1", "value1");
        
        assertNotNull(result);
        assertSame(condition, result);
    }

    @Test
    @DisplayName("测试 addParas() 方法")
    void testAddParas() {
        com.dlz.kit.json.JSONMap paras = new com.dlz.kit.json.JSONMap();
        paras.put("key1", "value1");
        paras.put("key2", "value2");
        
        Condition result = condition.addParas(paras);
        
        assertNotNull(result);
    }

    // ========== setRunSql 测试 ==========

    @Test
    @DisplayName("测试 setRunSql() 方法")
    void testSetRunSql() {
        Condition result = condition.setRunSql("custom sql");
        
        assertNotNull(result);
        assertSame(condition, result);
    }

    // ========== me() 测试 ==========

    @Test
    @DisplayName("测试 me() 方法")
    void testMe() {
        Condition result = condition.me();
        
        assertNotNull(result);
        assertSame(condition, result, "me() 应该返回自身");
    }

    // ========== where() 静态方法测试 ==========

    @Test
    @DisplayName("测试 Condition.where() 静态方法")
    void testWhereStatic() {
        Condition where = Condition.where();
        
        assertNotNull(where);
    }
}
