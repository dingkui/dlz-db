package com.dlz.test.db.cases.enums;

import com.dlz.db.enums.DbOperateEnum;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.para.ParaMap;
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

    @Test
    @DisplayName("测试 equals 操作符")
    void testEqOperator() {
        Condition condition = DbOperateEnum.eq.mk("user_name", "test",null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("USER_NAME = "));
    }

    @Test
    @DisplayName("测试 not equals 操作符")
    void testNeOperator() {
        Condition condition = DbOperateEnum.ne.mk("user_name", "test",null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("USER_NAME <> "));
    }

    @Test
    @DisplayName("测试 greater than 操作符")
    void testGtOperator() {
        Condition condition = DbOperateEnum.gt.mk("age", 18,null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("AGE > "));
    }

    @Test
    @DisplayName("测试 less than 操作符")
    void testLtOperator() {
        Condition condition = DbOperateEnum.lt.mk("age", 18,null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("AGE < "));
    }

    @Test
    @DisplayName("测试 greater than or equal 操作符")
    void testGeOperator() {
        Condition condition = DbOperateEnum.ge.mk("age", 18,null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("AGE >="));
    }

    @Test
    @DisplayName("测试 less than or equal 操作符")
    void testLeOperator() {
        Condition condition = DbOperateEnum.le.mk("age", 18,null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("AGE <= "));
    }

    @Test
    @DisplayName("测试 in 操作符")
    void testInOperator() {
        Condition condition = DbOperateEnum.in.mk("id", new Object[]{1, 2, 3},null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("ID in ("));
    }

    @Test
    @DisplayName("测试 not in 操作符")
    void testNotInOperator() {
        Condition condition = DbOperateEnum.notIn.mk("id", new Object[]{1, 2, 3},null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("ID not in ("));
    }

    @Test
    @DisplayName("测试 like 操作符")
    void testLikeOperator() {
        Condition condition = DbOperateEnum.like.mk("name", "test",null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("NAME like "));
        // like 应该自动添加 % 通配符
    }

    @Test
    @DisplayName("测试 like left 操作符")
    void testLikeLeftOperator() {
        Condition condition = DbOperateEnum.likeLeft.mk("name", "test",null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("NAME like "));
    }

    @Test
    @DisplayName("测试 like right 操作符")
    void testLikeRightOperator() {
        Condition condition = DbOperateEnum.likeRight.mk("name", "test",null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("NAME like "));
    }

    @Test
    @DisplayName("测试 not like 操作符")
    void testNotLikeOperator() {
        Condition condition = DbOperateEnum.notLike.mk("name", "test",null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("NAME not like "));
    }

    @Test
    @DisplayName("测试 between 操作符")
    void testBetweenOperator() {
        Condition condition = DbOperateEnum.between.mk("age", new Object[]{18, 60},null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("AGE between "));
        assertThrows(SystemException.class,()->DbOperateEnum.between.mk("age", new Object[]{18},null));
    }

    @Test
    @DisplayName("测试 not between 操作符")
    void testNotBetweenOperator() {
        Condition condition = DbOperateEnum.notBetween.mk("age", new Object[]{18, 60},null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("AGE not between "));
    }

    @Test
    @DisplayName("测试 is null 操作符")
    void testIsNullOperator() {
        Condition condition = DbOperateEnum.isNull.mk("name",null,null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("NAME is null"));
    }

    @Test
    @DisplayName("测试 is not null 操作符")
    void testIsNotNullOperator() {
        Condition condition = DbOperateEnum.isNotNull.mk("name",null,null);
        assertNotNull(condition);
        assertTrue(condition.getRunsql(new ParaMap()).contains("NAME is not null"));
    }

    @Test
    @DisplayName("测试 getDbOperateEnum - 找到操作符")
    void testGetDbOperateEnum_Found() {
        assertNotNull(DbOperateEnum.getDbOperateEnum("eq"));
        assertNotNull(DbOperateEnum.getDbOperateEnum("like"));
        assertNotNull(DbOperateEnum.getDbOperateEnum("in"));
    }

    @Test
    @DisplayName("测试 getDbOperateEnum - 未找到操作符")
    void testGetDbOperateEnum_NotFound() {
        assertNull(DbOperateEnum.getDbOperateEnum("invalid"));
        assertNull(DbOperateEnum.getDbOperateEnum(null));
    }

    @Test
    @DisplayName("测试所有枚举值数量")
    void testAllEnumValues() {
        assertEquals(16, DbOperateEnum.values().length);
    }
}
