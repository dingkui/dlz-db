package com.dlz.db.util;

import com.dlz.db.convertor.columnname.ColumnNameCamel;
import com.dlz.db.convertor.columnname.ColumnNameToLower;
import com.dlz.db.convertor.columnname.ColumnNameToUper;
import com.dlz.db.modal.dto.ResultMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbConvertUtil 扩展测试类
 * 
 * @author test
 */
@DisplayName("数据库转换工具扩展测试")
class DbConvertUtilTest_Extended {

    @BeforeEach
    void setUp() {
        // 确保使用默认的转换器
        DbConvertUtil.defaultColumnMapper = new ColumnNameCamel();
        DbConvertUtil.defaultTableColumnMapper = null;
    }

    @AfterEach
    void tearDown() {
        // 恢复默认值
        DbConvertUtil.defaultColumnMapper = new ColumnNameCamel();
        DbConvertUtil.defaultTableColumnMapper = null;
    }

    @Test
    @DisplayName("测试 toFieldName - 使用 ColumnNameToLower")
    void testToFieldName_WithToLowerConverter() {
        ColumnNameToLower converter = new ColumnNameToLower();
        DbConvertUtil.defaultColumnMapper = converter;
        
        assertEquals("username", DbConvertUtil.toFieldName("USERNAME"));
        assertEquals("userName", DbConvertUtil.toFieldName("USER_NAME"));
    }

    @Test
    @DisplayName("测试 toFieldName - 使用 ColumnNameToUper")
    void testToFieldName_WithToUperConverter() {
        ColumnNameToUper converter = new ColumnNameToUper();
        DbConvertUtil.defaultColumnMapper = converter;
        
        assertEquals("username", DbConvertUtil.toFieldName("username"));
        assertEquals("userName", DbConvertUtil.toFieldName("user_name"));
    }

    @Test
    @DisplayName("测试 toDbColumnName - 使用 ColumnNameToLower")
    void testToDbColumnName_WithToLowerConverter() {
        ColumnNameToLower converter = new ColumnNameToLower();
        DbConvertUtil.defaultColumnMapper = converter;
        
        assertEquals("USER_NAME", DbConvertUtil.toDbColumnName("userName"));
        assertEquals("USER_NAME", DbConvertUtil.toDbColumnName("USER_NAME"));
    }

    @Test
    @DisplayName("测试 toDbColumnName - 使用 ColumnNameToUper")
    void testToDbColumnName_WithToUperConverter() {
        ColumnNameToUper converter = new ColumnNameToUper();
        DbConvertUtil.defaultColumnMapper = converter;
        
        assertEquals("USER_NAME", DbConvertUtil.toDbColumnName("userName"));
        assertEquals("USER_NAME", DbConvertUtil.toDbColumnName("USER_NAME"));
    }

    @Test
    @DisplayName("测试 getFistColumn - 使用自定义转换器")
    void testGetFistColumn_WithCustomConverter() {
        ColumnNameToLower converter = new ColumnNameToLower();
        DbConvertUtil.defaultColumnMapper = converter;
        
        ResultMap map = new ResultMap();
        map.put("id", 1);
        map.put("name", "test");
        
        Object result = DbConvertUtil.getFistColumn(map);
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 getColumnList - 使用自定义转换器")
    void testGetColumnList_WithCustomConverter() {
        ColumnNameToLower converter = new ColumnNameToLower();
        DbConvertUtil.defaultColumnMapper = converter;
        
        List<ResultMap> list = new ArrayList<>();
        ResultMap map1 = new ResultMap();
        map1.put("id", "1");
        list.add(map1);
        
        List<Integer> result = DbConvertUtil.getColumnList(list, Integer.class);
        
        assertEquals(1, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    @DisplayName("测试 toDbColumnNames - 多个字段名")
    void testToDbColumnNames_Multiple() {
        String result = DbConvertUtil.toDbColumnNames("userName userId");
        assertEquals("USER_NAME USER_ID", result);
    }

    @Test
    @DisplayName("测试 toDbColumnNames - 空字符串")
    void testToDbColumnNames_Empty() {
        String result = DbConvertUtil.toDbColumnNames("");
        assertEquals("", result);
    }


    @Test
    @DisplayName("测试 getVal4Db - null 映射器")
    void testGetVal4Db_NullMapper() {
        DbConvertUtil.defaultTableColumnMapper = null;
        
        // 当映射器为 null 时，返回原值
        Object result = DbConvertUtil.getVal4Db("user", "name", "test");
        assertEquals("test", result);
    }

    @Test
    @DisplayName("测试 getFistColumnWithType - String 类型")
    void testGetFistColumnWithType_String() {
        ResultMap map = new ResultMap();
        map.put("name", "test");
        
        String result = DbConvertUtil.getFistColumn(map, String.class);
        assertEquals("test", result);
    }

    @Test
    @DisplayName("测试 getFistColumnWithType - Integer 类型")
    void testGetFistColumnWithType_Integer() {
        ResultMap map = new ResultMap();
        map.put("id", "123");
        
        Integer result = DbConvertUtil.getFistColumn(map, Integer.class);
        assertEquals(123, result);
    }

    @Test
    @DisplayName("测试 getFistColumnWithType - Long 类型")
    void testGetFistColumnWithType_Long() {
        ResultMap map = new ResultMap();
        map.put("id", "123456789");
        
        Long result = DbConvertUtil.getFistColumn(map, Long.class);
        assertEquals(123456789L, result);
    }

    @Test
    @DisplayName("测试 getFistColumnWithType - Double 类型")
    void testGetFistColumnWithType_Double() {
        ResultMap map = new ResultMap();
        map.put("price", "12.34");
        
        Double result = DbConvertUtil.getFistColumn(map, Double.class);
        assertEquals(12.34, result, 0.001);
    }

    @Test
    @DisplayName("测试 getFistColumnWithType - Boolean 类型")
    void testGetFistColumnWithType_Boolean() {
        ResultMap map = new ResultMap();
        map.put("active", "true");
        
        Boolean result = DbConvertUtil.getFistColumn(map, Boolean.class);
        assertTrue(result);
    }

    @Test
    @DisplayName("测试 getFistColumnWithType - null 类型")
    void testGetFistColumnWithType_NullType() {
        ResultMap map = new ResultMap();
        map.put("name", "test");
        
        String result = DbConvertUtil.getFistColumn(map, null);
        assertEquals("test", result);
    }

    @Test
    @DisplayName("测试 getFistColumnWithType - Map 为空")
    void testGetFistColumnWithType_NullMap() {
        String result = DbConvertUtil.getFistColumn(null, String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("测试 getColumnList - null 类型")
    void testGetColumnList_NullType() {
        List<ResultMap> list = new ArrayList<>();
        ResultMap map1 = new ResultMap();
        map1.put("id", "1");
        list.add(map1);
        
        List<String> result = DbConvertUtil.getColumnList(list, null);
        
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("测试 getColumnList - 空列表")
    void testGetColumnList_EmptyList() {
        List<ResultMap> list = new ArrayList<>();
        
        List<String> result = DbConvertUtil.getColumnList(list, String.class);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试往返转换 - toFieldName 然后 toDbColumnName")
    void testRoundTrip_ToFieldNameThenToDbColumnName() {
        String original = "user_name";
        String fieldName = DbConvertUtil.toFieldName(original);
        String dbColumn = DbConvertUtil.toDbColumnName(fieldName);
        
        // 驼峰转下划线再回驼峰再回下划线
        assertEquals("USER_NAME", dbColumn);
    }

    @Test
    @DisplayName("测试往返转换 - toDbColumnName 然后 toFieldName")
    void testRoundTrip_ToDbColumnNameThenToFieldName() {
        String original = "userName";
        String dbColumn = DbConvertUtil.toDbColumnName(original);
        String fieldName = DbConvertUtil.toFieldName(dbColumn);
        
        // 驼峰转下划线再回驼峰
        assertEquals(original, fieldName);
    }

    @Test
    @DisplayName("测试特殊字符处理")
    void testSpecialCharacters() {
        // 测试包含特殊字符的字段名
        String result = DbConvertUtil.toFieldName("user_name_info");
        assertEquals("userNameInfo", result);
        
        result = DbConvertUtil.toDbColumnName("userNameInfo");
        assertEquals("USER_NAME_INFO", result);
    }

    @Test
    @DisplayName("测试数字处理")
    void testNumbers() {
        // 测试包含数字的字段名
        String result = DbConvertUtil.toFieldName("user1_name2");
        assertEquals("user1Name2", result);
        
        result = DbConvertUtil.toDbColumnName("user1Name2");
        assertEquals("USER1_NAME2", result);
    }

    @Test
    @DisplayName("测试性能 - toFieldName")
    void testPerformance_ToFieldName() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10000; i++) {
            DbConvertUtil.toFieldName("user_name_info_" + i);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "toFieldName 性能测试失败，耗时: " + duration + "ms");
    }

    @Test
    @DisplayName("测试性能 - toDbColumnName")
    void testPerformance_ToDbColumnName() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10000; i++) {
            DbConvertUtil.toDbColumnName("userNameInfo" + i);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "toDbColumnName 性能测试失败，耗时: " + duration + "ms");
    }
}
