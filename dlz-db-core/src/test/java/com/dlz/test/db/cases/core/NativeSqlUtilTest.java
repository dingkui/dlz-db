package com.dlz.test.db.cases.core;

import com.dlz.db.core.NativeSqlUtil;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NativeSqlUtil 原生SQL工具测试")
class NativeSqlUtilTest extends BaseDBTest {

    @Test
    @DisplayName("TABLE_NAME_PATTERN 匹配合法表名")
    void testTableNamePatternValid() {
        assertTrue(NativeSqlUtil.TABLE_NAME_PATTERN.matcher("user").matches());
        assertTrue(NativeSqlUtil.TABLE_NAME_PATTERN.matcher("sys_user").matches());
        assertTrue(NativeSqlUtil.TABLE_NAME_PATTERN.matcher("schema.table").matches());
        assertTrue(NativeSqlUtil.TABLE_NAME_PATTERN.matcher("Table123").matches());
    }

    @Test
    @DisplayName("TABLE_NAME_PATTERN 拒绝非法表名")
    void testTableNamePatternInvalid() {
        assertFalse(NativeSqlUtil.TABLE_NAME_PATTERN.matcher("user; DROP TABLE").matches());
        assertFalse(NativeSqlUtil.TABLE_NAME_PATTERN.matcher("user--comment").matches());
        assertFalse(NativeSqlUtil.TABLE_NAME_PATTERN.matcher("").matches());
        assertFalse(NativeSqlUtil.TABLE_NAME_PATTERN.matcher("table name").matches());
    }

    @Test
    @DisplayName("表名校验拒绝 SQL 注入字符")
    void requireSafeTableNameRejectsInjection() {
        assertDoesNotThrow(() -> NativeSqlUtil.requireSafeTableName("schema.user_1"));
        assertThrows(IllegalArgumentException.class,
                () -> NativeSqlUtil.requireSafeTableName("user` WHERE 1=1 --"));
    }

    @Test
    @DisplayName("连接测试 SQL 仅允许单条只读查询")
    void requireSafeConnectionTestQueryRejectsStatementChaining() {
        assertEquals("SELECT 1", NativeSqlUtil.requireSafeConnectionTestQuery(null));
        assertEquals("SELECT 1", NativeSqlUtil.requireSafeConnectionTestQuery(" SELECT 1 "));
        assertThrows(IllegalArgumentException.class,
                () -> NativeSqlUtil.requireSafeConnectionTestQuery("SELECT 1; DROP TABLE users"));
        assertThrows(IllegalArgumentException.class,
                () -> NativeSqlUtil.requireSafeConnectionTestQuery("UPDATE users SET role = 'admin'"));
    }

    @Test
    @DisplayName("queryLastInsertIdByDialect - SQLite方言")
    void testQueryLastInsertIdSqlite() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE test_auto (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
            st.execute("INSERT INTO test_auto (name) VALUES ('hello')");
        }
        Long id = NativeSqlUtil.queryLastInsertIdByDialect(conn);
        assertNotNull(id);
        assertEquals(1L, id);
        conn.close();
    }

    @Test
    @DisplayName("bindArgs - 绑定参数到PreparedStatement")
    void testBindArgs() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE bind_test (id INTEGER, name TEXT, age INTEGER)");
        }
        PreparedStatement ps = conn.prepareStatement("INSERT INTO bind_test VALUES (?, ?, ?)");
        NativeSqlUtil.bindArgs(ps, 1, "test", 25);
        assertEquals(1, ps.executeUpdate());
        ps.close();
        conn.close();
    }

    @Test
    @DisplayName("bindArgs - null参数不抛异常")
    void testBindArgsNull() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        PreparedStatement ps = conn.prepareStatement("SELECT 1");
        assertDoesNotThrow(() -> NativeSqlUtil.bindArgs(ps, (Object[]) null));
        ps.close();
        conn.close();
    }
}
