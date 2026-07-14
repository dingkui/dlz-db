package com.dlz.test.db.cases.enums;

import com.dlz.db.enums.DbBuildEnum;
import com.dlz.db.modal.condition.Condition;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DbBuildEnum SQL构建枚举测试")
class DbBuildEnumTest extends BaseDBTest {

    @Test
    @DisplayName("buildSql - 替换#s占位符")
    void testBuildSql() {
        assertEquals("AND name='test'", DbBuildEnum.ands.buildSql("name='test'"));
        assertEquals("OR age>10", DbBuildEnum.ors.buildSql("age>10"));
        assertEquals("(SELECT 1)", DbBuildEnum.sql.buildSql("SELECT 1"));
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
        assertThrows(SystemException.class, () -> DbBuildEnum.where.build("",null, null));
    }

    @Test
    @DisplayName("build(tableName) - sql类型抛异常")
    void testBuildTypeThrows() {
        assertThrows(Exception.class, () -> DbBuildEnum.sql.build("test_table"));
        assertThrows(SystemException.class, () -> DbBuildEnum.where.build("",null, null));
    }

    @Test
    @DisplayName("valueOf 正确解析")
    void testValueOf() {
        assertEquals(DbBuildEnum.ands, DbBuildEnum.valueOf("ands"));
//        assertEquals(DbBuildEnum.sql, DbBuildEnum.valueOf("sql"));
//        assertEquals(DbBuildEnum.apply, DbBuildEnum.valueOf("apply"));
    }
}
