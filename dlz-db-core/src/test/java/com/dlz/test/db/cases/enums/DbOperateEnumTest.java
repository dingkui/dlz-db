package com.dlz.test.db.cases.enums;

import com.dlz.db.enums.DbOperateEnum;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.exception.SystemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbOperateEnum 测试类
 * 
 * @author test
 */
@DisplayName("数据库操作枚举测试")
class DbOperateEnumTest {
    private void checkConditionSql(String targetSql, Condition condition){
        assertNotNull(condition);
        final ParaMap pm = new ParaMap();
        String sqlDeal = condition.getRunsql(pm);
        sqlDeal = SqlUtil.replaceSql(sqlDeal, pm.getPara(), 0);
        pm.getSqlItem().setSqlDeal(sqlDeal);
        assertEquals(targetSql, pm.jdbcSql().toRunSql());
    }

    @Test
    @DisplayName("测试 equals 操作符")
    void testEqOperator() {
        Condition condition = DbOperateEnum.eq.mk("user_name", "test",null);
        checkConditionSql("user_name = 'test'",condition);
    }

    @Test
    @DisplayName("测试 not equals 操作符")
    void testNeOperator() {
        Condition condition = DbOperateEnum.ne.mk("user_name", "test",null);
        checkConditionSql("user_name <> 'test'",condition);
    }

    @Test
    @DisplayName("测试 greater than 操作符")
    void testGtOperator() {
        Condition condition = DbOperateEnum.gt.mk("age", 18,null);
        checkConditionSql("age > 18",condition);
    }

    @Test
    @DisplayName("测试 less than 操作符")
    void testLtOperator() {
        Condition condition = DbOperateEnum.lt.mk("age", 18,null);
        checkConditionSql("age < 18",condition);
    }

    @Test
    @DisplayName("测试 greater than OR equal 操作符")
    void testGeOperator() {
        Condition condition = DbOperateEnum.ge.mk("age", 18,null);
        checkConditionSql("age >= 18",condition);
    }

    @Test
    @DisplayName("测试 less than OR equal 操作符")
    void testLeOperator() {
        Condition condition = DbOperateEnum.le.mk("age", 18,null);
        checkConditionSql("age <= 18",condition);
    }

    @Test
    @DisplayName("测试 in 操作符")
    void testInOperator() {
        Condition condition = DbOperateEnum.in.mk("id", new Object[]{1, 2, 3},null);
        checkConditionSql("id IN (1,2,3)",condition);
    }

    @Test
    @DisplayName("测试 NOT IN 操作符")
    void testNotInOperator() {
        Condition condition = DbOperateEnum.notIn.mk("id", new Object[]{1, 2, 3},null);
        checkConditionSql("id NOT IN (1,2,3)",condition);
    }

    @Test
    @DisplayName("测试 LIKE 操作符")
    void testLikeOperator() {
        Condition condition = DbOperateEnum.like.mk("name", "test",null);
        checkConditionSql("name LIKE '%test%'",condition);
    }

    @Test
    @DisplayName("测试 LIKE left 操作符")
    void testLikeLeftOperator() {
        Condition condition = DbOperateEnum.likeLeft.mk("name", "test",null);
        checkConditionSql("name LIKE '%test'",condition);
    }

    @Test
    @DisplayName("测试 LIKE right 操作符")
    void testLikeRightOperator() {
        Condition condition = DbOperateEnum.likeRight.mk("name", "test",null);
        checkConditionSql("name LIKE 'test%'",condition);
    }

    @Test
    @DisplayName("测试 NOT LIKE 操作符")
    void testNotLikeOperator() {
        Condition condition = DbOperateEnum.notLike.mk("name", "test",null);
        checkConditionSql("name NOT LIKE '%test%'",condition);
    }

    @Test
    @DisplayName("测试 BETWEEN 操作符")
    void testBetweenOperator() {
        Condition condition = DbOperateEnum.between.mk("age", new Object[]{18, 60},null);
        checkConditionSql("age BETWEEN 18 AND 60",condition);
        assertThrows(SystemException.class,()->DbOperateEnum.between.mk("age", new Object[]{18},null));
    }

    @Test
    @DisplayName("测试 NOT BETWEEN 操作符")
    void testNotBetweenOperator() {
        Condition condition = DbOperateEnum.notBetween.mk("age", new Object[]{18, 60},null);
        checkConditionSql("age NOT BETWEEN 18 AND 60",condition);
    }

    @Test
    @DisplayName("测试 IS NULL 操作符")
    void testIsNullOperator() {
        Condition condition = DbOperateEnum.isNull.mk("name",null,null);
        checkConditionSql("name IS NULL",condition);
    }

    @Test
    @DisplayName("测试 IS NOT NULL 操作符")
    void testIsNotNullOperator() {
        Condition condition = DbOperateEnum.isNotNull.mk("name",null,null);
        checkConditionSql("name IS NOT NULL",condition);
    }

    @Test
    @DisplayName("测试所有枚举值数量")
    void testAllEnumValues() {
        assertEquals(16, DbOperateEnum.values().length);
    }
}
