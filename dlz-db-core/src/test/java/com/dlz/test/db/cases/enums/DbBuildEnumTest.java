package com.dlz.test.db.cases.enums;

import com.dlz.db.enums.DbBuildEnum;
import com.dlz.db.modal.condition.Condition;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DbBuildEnum SQL构建枚举测试")
class DbBuildEnumTest extends BaseDBTest {

    @Test
    @DisplayName("buildSql - 替换#s占位符")
    void testBuildSql() {
        assertEquals("and name='test'", DbBuildEnum.ands.buildSql("name='test'"));
        assertEquals("or age>10", DbBuildEnum.ors.buildSql("age>10"));
        assertEquals("(select 1)", DbBuildEnum.sql.buildSql("select 1"));
    }

    @Test
    @DisplayName("buildSql - 空sql返回空字符串")
    void testBuildSqlEmpty() {
        assertEquals("", DbBuildEnum.ands.buildSql(""));
        assertEquals("", DbBuildEnum.sql.buildSql(""));
    }

    @Test
    @DisplayName("build(tableName) - ands")
    void testBuildAnds() {
        Condition cond = DbBuildEnum.ands.build("test_table");
        assertNotNull(cond);
    }

    @Test
    @DisplayName("build(tableName) - ors")
    void testBuildOrs() {
        Condition cond = DbBuildEnum.ors.build("test_table");
        assertNotNull(cond);
    }

    @Test
    @DisplayName("build(tableName) - muOr")
    void testBuildMuOr() {
        Condition cond = DbBuildEnum.muOr.build("test_table");
        assertNotNull(cond);
    }

    @Test
    @DisplayName("build(tableName) - muAnd")
    void testBuildMuAnd() {
        Condition cond = DbBuildEnum.muAnd.build("test_table");
        assertNotNull(cond);
    }

    @Test
    @DisplayName("build(tableName) - where")
    void testBuildWhere() {
        Condition cond = DbBuildEnum.where.build("test_table");
        assertNotNull(cond);
    }

    @Test
    @DisplayName("build(tableName) - sql类型抛异常")
    void testBuildSqlTypeThrows() {
        assertThrows(Exception.class, () -> DbBuildEnum.sql.build("test_table"));
    }

    @Test
    @DisplayName("build(tableName) - apply类型抛异常")
    void testBuildApplyTypeThrows() {
        assertThrows(Exception.class, () -> DbBuildEnum.apply.build("test_table"));
    }

    @Test
    @DisplayName("枚举值数量为7")
    void testEnumCount() {
        assertEquals(7, DbBuildEnum.values().length);
    }

    @Test
    @DisplayName("valueOf 正确解析")
    void testValueOf() {
        assertEquals(DbBuildEnum.ands, DbBuildEnum.valueOf("ands"));
        assertEquals(DbBuildEnum.sql, DbBuildEnum.valueOf("sql"));
        assertEquals(DbBuildEnum.apply, DbBuildEnum.valueOf("apply"));
    }
}
