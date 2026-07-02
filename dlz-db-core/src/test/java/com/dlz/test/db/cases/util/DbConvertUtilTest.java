package com.dlz.test.db.cases.util;

import com.dlz.db.convertor.columnname.ColumnNameCamel;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.util.DbConvertUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbConvertUtil 全面覆盖测试
 */
@DisplayName("数据库转换工具测试")
class DbConvertUtilTest {

    @BeforeEach
    void setUp() {
        DbConvertUtil.defaultColumnMapper = new ColumnNameCamel();
    }

    // ==================== toFieldName ====================

    @ParameterizedTest
    @CsvSource({
        "user_name, userName",
        "user_id, userId",
        "create_time, createTime",
        "update_time, updateTime",
        "DELETED , deleted",
        "order_no, orderNo"
    })
    @DisplayName("toFieldName - 下划线转驼峰")
    void testToFieldName_Basic(String dbKey, String expected) {
        assertEquals(expected, DbConvertUtil.toFieldName(dbKey));
    }

    @Test
    @DisplayName("toFieldName - 特殊情况")
    void testToFieldName_Special() {
        assertEquals("name", DbConvertUtil.toFieldName("name"));
        assertEquals("userNameInfo", DbConvertUtil.toFieldName("user_name_info"));
        assertEquals("user1Name", DbConvertUtil.toFieldName("user1_name"));
    }

    @Test
    @DisplayName("toFieldName - null 返回空字符串")
    void testToFieldName_Null() {
        assertEquals("", DbConvertUtil.toFieldName(null));
    }

    // ==================== toDbColumnName ====================

    @ParameterizedTest
    @CsvSource({
        "userName, USER_NAME",
        "userId, USER_ID",
        "createTime, CREATE_TIME",
        "updateTime, UPDATE_TIME",
        "deleted, DELETED ",
        "orderNo, ORDER_NO"
    })
    @DisplayName("toDbColumnName - 驼峰转下划线大写")
    void testToDbColumnName_Basic(String beanKey, String expected) {
        assertEquals(expected.trim(), DbConvertUtil.toDbColumnName(beanKey));
    }

    @Test
    @DisplayName("toDbColumnName - 特殊情况")
    void testToDbColumnName_Special() {
        assertEquals("NAME", DbConvertUtil.toDbColumnName("name"));
        assertEquals("USER_NAME", DbConvertUtil.toDbColumnName("user_name"));
        assertEquals("ID", DbConvertUtil.toDbColumnName("ID"));
        assertEquals("USER_I_D", DbConvertUtil.toDbColumnName("userID"));
    }

    @Test
    @DisplayName("toDbColumnName - null 返回 null")
    void testToDbColumnName_Null() {
        assertNull(DbConvertUtil.toDbColumnName(null));
    }

    @Test
    @DisplayName("toDbColumnName - 空字符串返回空字符串（覆盖 isEmpty 分支）")
    void testToDbColumnName_Empty() {
        assertEquals("", DbConvertUtil.toDbColumnName(""));
    }

    // ==================== toDbColumnNames ====================

    @Test
    @DisplayName("toDbColumnNames - 带空格的多字段名转换")
    void testToDbColumnNames_WithSpaces() {
        String result = DbConvertUtil.toDbColumnNames("userName   userId");
        assertEquals("USER_NAME USER_ID", result);
    }

    // ==================== getVal4Db ====================

    @Test
    @DisplayName("getVal4Db - null 值直接返回（覆盖快速路径）")
    void testGetVal4Db_NullValue() {
        assertNull(DbConvertUtil.getVal4Db("user", "NAME", null));
    }

    @Test
    @DisplayName("getVal4Db - 非 null 值正常转换")
    void testGetVal4Db_NonNullValue() {
        Object result = DbConvertUtil.getVal4Db("user", "ID", 123);
        assertNotNull(result);
    }

    // ==================== getFirstColumn ====================

    @Test
    @DisplayName("getFirstColumn - 正常返回第一个非特殊列")
    void testGetFistColumn_Normal() {
        ResultMap map = new ResultMap();
        map.put("id", 1);
        Object result = DbConvertUtil.getFirstColumn(map);
        assertEquals(1, result);
    }

    @Test
    @DisplayName("getFirstColumn - 跳过 ROWNUM_ 和 rownum 列")
    void testGetFistColumn_SkipRownum() {
        ResultMap map = new ResultMap();
        map.put("ROWNUM_", 1);
        map.put("name", "test");
        assertEquals("test", DbConvertUtil.getFirstColumn(map));
    }

    @Test
    @DisplayName("getFirstColumn - null 返回 null")
    void testGetFistColumn_Null() {
        assertNull(DbConvertUtil.getFirstColumn((ResultMap) null));
    }

    @Test
    @DisplayName("getFirstColumn - 只有 ROWNUM_ 列应返回 null（覆盖仅 rownum 分支）")
    void testGetFistColumn_OnlyRownum() {
        ResultMap map = new ResultMap();
        map.put("ROWNUM_", 1);
        assertNull(DbConvertUtil.getFirstColumn(map));
    }

    // ==================== getFirstColumn with type ====================

    @Test
    @DisplayName("getFirstColumn(Class) - 映射为 String")
    void testGetFistColumnWithType_String() {
        ResultMap map = new ResultMap();
        map.put("name", "test");
        assertEquals("test", DbConvertUtil.getFirstColumn(map, String.class));
    }

    @Test
    @DisplayName("getFirstColumn(Class) - 映射为 Integer")
    void testGetFistColumnWithType_Integer() {
        ResultMap map = new ResultMap();
        map.put("id", "123");
        assertEquals(123, DbConvertUtil.getFirstColumn(map, Integer.class));
    }

    // ==================== getColumnList ====================

    @Test
    @DisplayName("getColumnList - 正常")
    void testGetColumnList_Normal() {
        List<ResultMap> list = new ArrayList<>();
        ResultMap m1 = new ResultMap(); m1.put("id", "1"); list.add(m1);
        ResultMap m2 = new ResultMap(); m2.put("id", "2"); list.add(m2);
        List<Integer> result = DbConvertUtil.getColumnList(list, Integer.class);
        assertEquals(2, result.size());
        assertEquals(1, (int) result.get(0));
        assertEquals(2, (int) result.get(1));
    }

    @Test
    @DisplayName("getColumnList - 空列表")
    void testGetColumnList_Empty() {
        assertTrue(DbConvertUtil.getColumnList(new ArrayList<>(), String.class).isEmpty());
    }

    @Test
    @DisplayName("getColumnList - tClass 为 null（覆盖 null class 分支）")
    void testGetColumnList_NullClass() {
        List<ResultMap> list = new ArrayList<>();
        ResultMap m = new ResultMap(); m.put("x", "y"); list.add(m);
        List<Object> result = DbConvertUtil.getColumnList(list, null);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof ResultMap);
    }

    // ==================== 往返转换 ====================

    @Test
    @DisplayName("toDbColumnName → toFieldName 往返可逆")
    void testRoundTrip() {
        String original = "userName";
        String dbColumn = DbConvertUtil.toDbColumnName(original);
        String backToField = DbConvertUtil.toFieldName(dbColumn);
        assertEquals(original, backToField);
    }
}
