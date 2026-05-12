package com.dlz.db.util;

import com.dlz.db.convertor.columnname.ColumnNameCamel;
import com.dlz.db.modal.dto.ResultMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbConvertUtil 测试类
 * 
 * @author test
 */
@DisplayName("数据库转换工具测试")
class DbConvertUtilTest {

    @BeforeEach
    void setUp() {
        // 确保使用默认的转换器
        DbConvertUtil.defaultColumnMapper = new ColumnNameCamel();
    }

    @Test
    @DisplayName("测试数据库字段名转Java字段名 - 基本转换")
    void testToFieldName_Basic() {
        // 下划线转驼峰
        assertEquals("userName", DbConvertUtil.toFieldName("user_name"));
        assertEquals("userId", DbConvertUtil.toFieldName("user_id"));
        assertEquals("createTime", DbConvertUtil.toFieldName("create_time"));
    }

    @ParameterizedTest
    @DisplayName("测试数据库字段名转Java字段名 - 参数化测试")
    @CsvSource({
        "user_name, userName",
        "user_id, userId",
        "create_time, createTime",
        "update_time, updateTime",
        "is_deleted, isDeleted",
        "order_no, orderNo"
    })
    void testToFieldName_Parameterized(String dbKey, String expected) {
        assertEquals(expected, DbConvertUtil.toFieldName(dbKey));
    }

    @Test
    @DisplayName("测试数据库字段名转Java字段名 - 特殊情况")
    void testToFieldName_SpecialCases() {
        // 全小写无下划线
        assertEquals("name", DbConvertUtil.toFieldName("name"));
        
        // 多个下划线
        assertEquals("userNameInfo", DbConvertUtil.toFieldName("user_name_info"));
        
        // 数字
        assertEquals("user1Name", DbConvertUtil.toFieldName("user1_name"));
    }

    @Test
    @DisplayName("测试数据库字段名转Java字段名 - 空值")
    void testToFieldName_Null() {
        assertEquals("", DbConvertUtil.toFieldName(null));
    }

    @Test
    @DisplayName("测试Java字段名转数据库字段名 - 基本转换")
    void testToDbColumnName_Basic() {
        // 驼峰转下划线
        assertEquals("USER_NAME", DbConvertUtil.toDbColumnName("userName"));
        assertEquals("USER_ID", DbConvertUtil.toDbColumnName("userId"));
        assertEquals("CREATE_TIME", DbConvertUtil.toDbColumnName("createTime"));
    }

    @ParameterizedTest
    @DisplayName("测试Java字段名转数据库字段名 - 参数化测试")
    @CsvSource({
        "userName, USER_NAME",
        "userId, USER_ID",
        "createTime, CREATE_TIME",
        "updateTime, UPDATE_TIME",
        "isDeleted, IS_DELETED",
        "orderNo, ORDER_NO"
    })
    void testToDbColumnName_Parameterized(String beanKey, String expected) {
        assertEquals(expected, DbConvertUtil.toDbColumnName(beanKey));
    }

    @Test
    @DisplayName("测试Java字段名转数据库字段名 - 特殊情况")
    void testToDbColumnName_SpecialCases() {
        // 全小写
        assertEquals("NAME", DbConvertUtil.toDbColumnName("name"));
        
        // 已经包含下划线（不转换）
        assertEquals("user_name", DbConvertUtil.toDbColumnName("user_name"));
        
        // 全大写（不转换）
        assertEquals("ID", DbConvertUtil.toDbColumnName("ID"));
        
        // 连续大写
        assertEquals("USER_I_D", DbConvertUtil.toDbColumnName("userID"));
    }

    @Test
    @DisplayName("测试Java字段名转数据库字段名 - 空值")
    void testToDbColumnName_Null() {
        assertNull(DbConvertUtil.toDbColumnName(null));
    }

    @Test
    @DisplayName("测试获取第一列 - 正常情况")
    void testGetFistColumn_Normal() {
        ResultMap map = new ResultMap();
        map.put("id", 1);
        map.put("name", "test");
        
        Object result = DbConvertUtil.getFistColumn(map);
        assertNotNull(result);
        assertTrue(result.equals(1) || result.equals("test"));
    }

    @Test
    @DisplayName("测试获取第一列 - 跳过ROWNUM")
    void testGetFistColumn_SkipRownum() {
        ResultMap map = new ResultMap();
        map.put("ROWNUM_", 1);
        map.put("name", "test");
        
        Object result = DbConvertUtil.getFistColumn(map);
        assertEquals("test", result);
    }

    @Test
    @DisplayName("测试获取第一列 - 空Map")
    void testGetFistColumn_Null() {
        assertNull(DbConvertUtil.getFistColumn(null));
    }

    @Test
    @DisplayName("测试获取第一列并转换类型 - String")
    void testGetFistColumnWithType_String() {
        ResultMap map = new ResultMap();
        map.put("name", "test");
        
        String result = DbConvertUtil.getFistColumn(map, String.class);
        assertEquals("test", result);
    }

    @Test
    @DisplayName("测试获取第一列并转换类型 - Integer")
    void testGetFistColumnWithType_Integer() {
        ResultMap map = new ResultMap();
        map.put("id", "123");
        
        Integer result = DbConvertUtil.getFistColumn(map, Integer.class);
        assertEquals(123, result);
    }

    @Test
    @DisplayName("测试获取列表 - 正常情况")
    void testGetColumnList_Normal() {
        List<ResultMap> list = new ArrayList<>();
        
        ResultMap map1 = new ResultMap();
        map1.put("id", "1");
        list.add(map1);
        
        ResultMap map2 = new ResultMap();
        map2.put("id", "2");
        list.add(map2);
        
        List<Integer> result = DbConvertUtil.getColumnList(list, Integer.class);
        
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }

    @Test
    @DisplayName("测试获取列表 - 空列表")
    void testGetColumnList_Empty() {
        List<ResultMap> list = new ArrayList<>();
        List<String> result = DbConvertUtil.getColumnList(list, String.class);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试字段名批量转换 - 带空格")
    void testToDbColumnNames_WithSpaces() {
        String result = DbConvertUtil.toDbColumnNames("userName   userId");
        assertEquals("USER_NAME USER_ID", result);
    }

    @Test
    @DisplayName("测试往返转换 - 确保可逆性")
    void testRoundTrip() {
        String original = "userName";
        String dbColumn = DbConvertUtil.toDbColumnName(original);
        String backToField = DbConvertUtil.toFieldName(dbColumn);
        
        assertEquals(original, backToField);
    }
}
