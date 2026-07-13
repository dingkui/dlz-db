package com.dlz.test.db.cases.util;

import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.para.ParaJdbc;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlUtil 全面覆盖测试
 */
@DisplayName("SQL 工具测试")
class SqlUtilTest extends BaseDBTest {

    // ==================== getRunSqlByJdbc ====================

    @Test
    @DisplayName("getRunSqlByJdbc - 数字参数")
    void testGetRunSqlByJdbc_Number() {
        String sql = SqlUtil.getRunSqlByJdbc("SELECT * FROM user WHERE id = ?", new Object[]{1});
        assertEquals("SELECT * FROM user WHERE id = 1", sql);
    }

    @Test
    @DisplayName("getRunSqlByJdbc - 字符串参数")
    void testGetRunSqlByJdbc_String() {
        String sql = SqlUtil.getRunSqlByJdbc("SELECT * FROM user WHERE name = ?", new Object[]{"test"});
        assertTrue(sql.contains("'test'"));
    }

    @Test
    @DisplayName("getRunSqlByJdbc - null 参数")
    void testGetRunSqlByJdbc_Null() {
        String sql = SqlUtil.getRunSqlByJdbc("SELECT * FROM user WHERE name = ?", new Object[]{null});
        // null 会走 ValUtil.toStr 路径
        assertNotNull(sql);
    }

    @Test
    @DisplayName("getRunSqlByJdbc - 多参数混合")
    void testGetRunSqlByJdbc_Mixed() {
        String sql = SqlUtil.getRunSqlByJdbc(
                "SELECT * FROM user WHERE id = ? AND name = ?",
                new Object[]{1, "hello"});
        assertTrue(sql.contains("id = 1"));
        assertTrue(sql.contains("name = 'hello'"));
    }

    @Test
    @DisplayName("getRunSqlByJdbc - Date 参数（覆盖 Date 分支）")
    void testGetRunSqlByJdbc_Date() {
        Date now = new Date();
        String sql = SqlUtil.getRunSqlByJdbc("SELECT * FROM user WHERE t = ?", new Object[]{now});
        assertNotNull(sql);
        assertTrue(sql.startsWith("SELECT * FROM user WHERE t = '"));
    }

    @Test
    @DisplayName("getRunSqlByJdbc - TemporalAccessor 参数（覆盖 LocalDateTime 分支）")
    void testGetRunSqlByJdbc_LocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2026, 7, 1, 12, 0, 0);
        String sql = SqlUtil.getRunSqlByJdbc("SELECT * FROM user WHERE t = ?", new Object[]{ldt});
        assertNotNull(sql);
        assertTrue(sql.startsWith("SELECT * FROM user WHERE t = '"));
    }

    @Test
    @DisplayName("getRunSqlByJdbc - 无 ? 占位符的原生 SQL")
    void testGetRunSqlByJdbc_NoPlaceholders() {
        String sql = SqlUtil.getRunSqlByJdbc("SELECT 1", new Object[]{});
        assertEquals("SELECT 1", sql);
    }

    @Test
    @DisplayName("getRunSqlByJdbc - null jdbcSql 抛异常")
    void testGetRunSqlByJdbc_NullJdbcSql() {
        assertThrows(Exception.class, () -> SqlUtil.getRunSqlByJdbc(null, new Object[]{}));
    }

    // ==================== getCntSql ====================

    @Test
    @DisplayName("getCntSql - 基本转换")
    void testGetCntSql_Basic() {
        assertEquals("SELECT COUNT(*) FROM user WHERE id = 1",
                SqlUtil.getCntSql("SELECT * FROM user WHERE id = 1"));
    }

    @Test
    @DisplayName("getCntSql - 大写 SQL")
    void testGetCntSql_UpperCase() {
        assertEquals("SELECT COUNT(*) FROM user",SqlUtil.getCntSql("SELECT * FROM user"));
    }

    @Test
    @DisplayName("getCntSql - 无 FROM 抛异常")
    void testGetCntSql_NoFrom() {
        assertThrows(Exception.class, () -> SqlUtil.getCntSql("SELECT 1"));
    }

    // ==================== getSqlInStr ====================

    @Test
    @DisplayName("getSqlInStr - 字符串数组（非数字）")
    void testGetSqlInStr_ArrayString() {
        String result = SqlUtil.getSqlInStr(new Object[]{"a", "b", "c"});
        assertEquals("'a','b','c'", result);
    }

    @Test
    @DisplayName("getSqlInStr - 数字数组")
    void testGetSqlInStr_NumberArray() {
        String result = SqlUtil.getSqlInStr(new Object[]{1, 2, 3});
        assertEquals("1,2,3", result);
    }

    @Test
    @DisplayName("getSqlInStr - 逗号分隔字符串")
    void testGetSqlInStr_CommaString() {
        String result = SqlUtil.getSqlInStr("a,b,c");
        assertEquals("'a','b','c'", result);
    }

    @Test
    @DisplayName("getSqlInStr - 集合参数")
    void testGetSqlInStr_Collection() {
        List<String> list = Arrays.asList("x", "y");
        String result = SqlUtil.getSqlInStr(list);
        assertEquals("'x','y'", result);
    }

    @Test
    @DisplayName("getSqlInStr - Number 直接输入（覆盖 Number 分支）")
    void testGetSqlInStr_NumberDirect() {
        String result = SqlUtil.getSqlInStr(12345);
        assertEquals("12345", result);
    }

    @Test
    @DisplayName("getSqlInStr - 非法类型抛异常（覆盖无效类型分支）")
    void testGetSqlInStr_InvalidType() {
        assertThrows(SystemException.class, () -> SqlUtil.getSqlInStr(true));
    }

    @Test
    @DisplayName("getSqlInStr - 空值抛异常")
    void testGetSqlInStr_Empty() {
        assertThrows(SystemException.class, () -> SqlUtil.getSqlInStr(""));
        assertThrows(SystemException.class, () -> SqlUtil.getSqlInStr((Object) null));
    }

    @Test
    @DisplayName("getSqlInStr - 值带引号（覆盖 strip 引号分支）")
    void testGetSqlInStr_WithQuotes() {
        // 以 ' 开头和结尾的值应被剥离引号后再处理
        Object[] array = new Object[]{"'hello'", "world"};
        String result = SqlUtil.getSqlInStr(array);
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
    }

    // ==================== replaceSql ====================

    @Test
    @DisplayName("replaceSql - 基本替换")
    void testReplaceSql_Basic() {
        Map<String, Object> para = new HashMap<>();
        para.put("name", "test");
        String result = SqlUtil.replaceSql("SELECT * FROM user WHERE name = ${name}", para, 0);
        assertTrue(result.contains("test"));
    }

    @Test
    @DisplayName("replaceSql - 多参数替换")
    void testReplaceSql_Multi() {
        Map<String, Object> para = new HashMap<>();
        para.put("table", "user");
        para.put("id", 1);
        String result = SqlUtil.replaceSql("SELECT * FROM ${table} WHERE id = ${id}", para, 0);
        assertTrue(result.contains("user"));
        assertTrue(result.contains("1"));
    }

    @Test
    @DisplayName("replaceSql - key. 前缀触发 SqlHolder 加载")
    void testReplaceSql_KeyPrefix() {
        // ${key.xxx} 会触发 getConditionStr(SqlHolder.getSql(key), m)
        Map<String, Object> para = new HashMap<>();
        String result = SqlUtil.replaceSql("SELECT ${key.comm.pageSql}", para, 0);
        // key.comm.pageSql 是内置 SQL，应被替换为分页模板
        assertNotNull(result);
    }

    @Test
    @DisplayName("replaceSql - 数组值替换（覆盖 getSqlInStr 路径）")
    void testReplaceSql_ArrayValue() {
        Map<String, Object> para = new HashMap<>();
        para.put("ids", new Object[]{1, 2, 3});
        String result = SqlUtil.replaceSql("SELECT * FROM user WHERE id IN (${ids})", para, 0);
        assertTrue(result.contains("1,2,3"));
    }

    @Test
    @DisplayName("replaceSql - 不存在的 key 替换为空字符串")
    void testReplaceSql_MissingKey() {
        Map<String, Object> para = new HashMap<>();
        String result = SqlUtil.replaceSql("SELECT ${missing}", para, 0);
        assertFalse(result.contains("${missing}")); // 被替换为空
    }

    // ==================== getConditionStr ====================

    @Test
    @DisplayName("getConditionStr - 条件满足时保留")
    void testGetConditionStr_Matched() {
        Map<String, Object> para = new HashMap<>();
        para.put("id", 1);
        String result = SqlUtil.getConditionStr("SELECT * FROM user [WHERE id = #{id}]", para);
        assertTrue(result.contains("WHERE"));
    }

    @Test
    @DisplayName("getConditionStr - 条件不满足时移除")
    void testGetConditionStr_NotMatched() {
        Map<String, Object> para = new HashMap<>();
        String result = SqlUtil.getConditionStr("SELECT * FROM user [WHERE id = #{id}]", para);
        assertFalse(result.contains("WHERE"));
    }

    @Test
    @DisplayName("getConditionStr - ^#{} 在条件满足时被剥离（覆盖 none 模式）")
    void testGetConditionStr_NonePattern() {
        Map<String, Object> para = new HashMap<>();
        para.put("name", "test");
        // ^#{} 标记的参数，当条件触发时该参数被移除
        String result = SqlUtil.getConditionStr("SELECT * FROM user [AND name = ^#{name}]", para);
        assertTrue(result.contains("AND name = "));
        assertFalse(result.contains("^#{"));  // ^#{} 被剥离
    }

    @Test
    @DisplayName("getConditionStr - ${} 替换条件满足时保留")
    void testGetConditionStr_ReplaceMatched() {
        Map<String, Object> para = new HashMap<>();
        para.put("value", "test");
        // 条件中只有 ${value} 没有 #{value}
        String result = SqlUtil.getConditionStr("SELECT * FROM user [WHERE name = ${value}]", para);
        assertTrue(result.contains("WHERE"));
    }

    @Test
    @DisplayName("getConditionStr - 多条件混合：部分满足")
    void testGetConditionStr_Mixed() {
        Map<String, Object> para = new HashMap<>();
        para.put("id", 1);
        // 第一个条件满足（id 存在），第二个条件不满足（name 不存在）
        String result = SqlUtil.getConditionStr(
                "SELECT * FROM user WHERE 1=1 [AND id = #{id}] [AND name = #{name}]", para);
        assertTrue(result.contains("id = #{id}"));
        assertFalse(result.contains("name = #{name}"));
    }

    @Test
    @DisplayName("getConditionStr - 无条件 SQL 原样返回")
    void testGetConditionStr_NoCondition() {
        Map<String, Object> para = new HashMap<>();
        String result = SqlUtil.getConditionStr("SELECT * FROM user", para);
        assertEquals("SELECT * FROM user", result);
    }

    // ==================== dealJdbc ====================

    @Test
    @DisplayName("dealJdbc - dealType=1 普通 SQL")
    void testDealJdbc_Basic() {
        ParaJdbc pj = new ParaJdbc("SELECT * FROM user", new Object[]{});
        JdbcItem item = SqlUtil.dealJdbc(pj, 1);
        assertNotNull(item);
        assertEquals("SELECT * FROM user", item.sql);
    }

    @Test
    @DisplayName("dealJdbc - dealType=2 计数 SQL")
    void testDealJdbc_Count() {
        ParaJdbc pj = new ParaJdbc("SELECT * FROM user WHERE id = ?", new Object[]{1});
        JdbcItem item = SqlUtil.dealJdbc(pj, 2);
        assertNotNull(item);
        assertEquals("SELECT COUNT(*) FROM user WHERE id = ?",item.sql);
    }

    @Test
    @DisplayName("dealJdbc - dealType=3 分页 SQL")
    void testDealJdbc_Page() {
        ParaJdbc pj = new ParaJdbc("SELECT * FROM user", new Object[]{});
        pj.setPage(new Page());
        JdbcItem item = SqlUtil.dealJdbc(pj, 3);
        assertNotNull(item);
    }

    @Test
    @DisplayName("dealJdbc - 无效 dealType 抛异常（覆盖 default 分支）")
    void testDealJdbc_InvalidDealType() {
        ParaJdbc pj = new ParaJdbc("SELECT 1", new Object[]{});
        assertThrows(Exception.class, () -> SqlUtil.dealJdbc(pj, 99));
    }

    // ==================== dealParm ====================

    @Test
    @DisplayName("dealParm - 无效 dealType 抛异常（覆盖 default 分支）")
    void testDealParm_InvalidDealType() {
        ParaMap pm = new ParaMap();
        SqlUtil.dealParm(pm, 99);

        ParaMap pm2 = new ParaMap("x");
        assertThrows(Exception.class, () -> SqlUtil.dealParm(pm2, 99));
    }

    // ==================== getPageSql ====================

    @Test
    @DisplayName("getPageSql(ParaJdbc) - null sqlDeal 抛异常")
    void testGetPageSqlParaJdbc_NullSqlDeal() {
        ParaJdbc pj = new ParaJdbc(null, new Object[]{});
        assertThrows(Exception.class, () -> SqlUtil.getPageSql(pj));
    }

    @Test
    @DisplayName("getPageSql(ParaJdbc) - null page 返回原样")
    void testGetPageSqlParaJdbc_NullPage() {
        ParaJdbc pj = new ParaJdbc("SELECT 1", new Object[]{});
        JdbcItem item = SqlUtil.getPageSql(pj);
        // page 为 null 时返回原始 SQL + 参数
        assertNotNull(item);
        assertEquals("SELECT 1", item.sql);
    }

    @Test
    @DisplayName("getPageSql(ParaJdbc) - 带 page 参数")
    void testGetPageSqlParaJdbc_WithPage() {
        ParaJdbc pj = new ParaJdbc("SELECT * FROM user", new Object[]{});
        pj.setPage(new Page(1, 10));
        JdbcItem item = SqlUtil.getPageSql(pj);
        assertNotNull(item);
        // 分页 SQL 应包含原有 SQL
        assertTrue(item.sql.contains("user"));
    }
}
