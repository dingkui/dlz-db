package com.dlz.test.db.cases.core;

import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.core.anno.SqlAction;
import com.dlz.db.exception.DbException;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.support.DBHolder;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ISqlExecutor 默认方法覆盖测试
 */
@DisplayName("ISqlExecutor 默认方法测试")
class ISqlExecutorTest extends BaseDBTest {

    /** 获取真实的 ISqlExecutor 实例用于测试 */
    private ISqlExecutor getRealExecutor() {
        return DBHolder.getSqlExecutor();
    }

    @Test
    @DisplayName("getTableColumnsInfo - 合法表名返回列信息")
    void testGetTableColumnsInfo_Valid() {
        HashMap<String, Integer> info = getRealExecutor().getTableColumnsInfo("user");
        assertNotNull(info);
        assertTrue(info.containsKey("id"));
        assertTrue(info.containsKey("NAME"));
    }

    @Test
    @DisplayName("getTableColumnsInfo - null 表名抛异常（覆盖 null 分支）")
    void testGetTableColumnsInfo_Null() {
        assertThrows(Exception.class, () -> getRealExecutor().getTableColumnsInfo(null));
    }

    @Test
    @DisplayName("getTableColumnsInfo - 非法表名抛异常（覆盖 pattern 不匹配分支）")
    void testGetTableColumnsInfo_InvalidName() {
        assertThrows(Exception.class, () -> getRealExecutor().getTableColumnsInfo("'; DROP TABLE"));
    }

    @Test
    @DisplayName(" UPDATE - 简单更新执行")
    void testUpdate() {
        int rows = getRealExecutor().update("DELETE FROM user");
        assertTrue(rows >= 0);
    }

    @Test
    @DisplayName("getOne - 查不到数据返回 null")
    void testGetOne_Empty() {
        ResultMap result = getRealExecutor().getOne("SELECT * FROM user WHERE 1=2", false);
        assertNull(result);
    }

    @Test
    @DisplayName("getOne - 查到一条数据")
    void testGetOne_Single() {
        DBHolder.getSqlExecutor().update("DELETE FROM user");
        getRealExecutor().update("INSERT INTO user(id,name) VALUES(999,'test')");

        ResultMap result = getRealExecutor().getOne("SELECT * FROM user WHERE id=999", false);
        assertNotNull(result);
        assertEquals("test", result.get("name"));
    }

    @Test
    @DisplayName("getOne - checkOne=true 多条数据抛异常（覆盖检查分支）")
    void testGetOne_CheckOneMultiple() {
        getRealExecutor().update("INSERT INTO user(id,name) VALUES(1,'a')");
        getRealExecutor().update("INSERT INTO user(id,name) VALUES(2,'b')");

        // 查询会返回多条，checkOne=true 应抛异常
        assertThrows(DbException.class,
                () -> getRealExecutor().getOne("SELECT * FROM user WHERE id IN(1,2)", true));
    }

    @Test
    @DisplayName("getFirstColumn - 获取首列值")
    void testGetFirstColumn() {
        getRealExecutor().update("INSERT INTO user(id,name) VALUES(200,'firstCol')");
        String name = getRealExecutor().getFirstColumn(
                "SELECT name FROM user WHERE id=200", String.class);
        assertEquals("firstCol", name);
    }

    @Test
    @DisplayName("updateForId - 插入并返回自增主键")
    void testUpdateForId() {
        Long id = getRealExecutor().updateForId(
                "INSERT INTO user(name) VALUES(?)", "autoKeyTest");
        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    @DisplayName("updateForId - 带其他参数插入")
    void testUpdateForId_WithAge() {
        Long id = getRealExecutor().updateForId(
                "INSERT INTO user(name,age) VALUES(?,?)", "ageTest", 99);
        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    @DisplayName("doDb(SqlAction) - DbException 实例直接抛出（覆盖异常分支）")
    void testDoDbSqlAction_DbException() {
        ISqlExecutor exec = getRealExecutor();
        assertThrows(DbException.class,
                () -> exec.doDb((SqlAction) conn -> {
                    throw new DbException("测试 DbException", 1001);
                }, (t, r) -> "error"));
    }
}
