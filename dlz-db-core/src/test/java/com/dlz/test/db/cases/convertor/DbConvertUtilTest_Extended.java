package com.dlz.test.db.cases.convertor;

import com.dlz.db.mapper.name.NameConvertCamel;
import com.dlz.db.mapper.name.NameConvertToLower;
import com.dlz.db.mapper.name.NameConvertToUper;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.util.DbConvertUtil;
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
        DbConvertUtil.defaultNameConvert = new NameConvertCamel();
    }

    @AfterEach
    void tearDown() {
        // 恢复默认值
        DbConvertUtil.defaultNameConvert = new NameConvertCamel();
    }

    @Test
    @DisplayName("测试 toFieldName - 使用 ColumnNameToLower")
    void testToFieldName_WithToLowerConverter() {
        NameConvertToLower converter = new NameConvertToLower();
        DbConvertUtil.defaultNameConvert = converter;
        
        assertEquals("username", DbConvertUtil.toFieldName("USERNAME"));
        assertEquals("userName", DbConvertUtil.toFieldName("USER_NAME"));
    }

    @Test
    @DisplayName("测试 toFieldName - 使用 ColumnNameToUper")
    void testToFieldName_WithToUperConverter() {
        NameConvertToUper converter = new NameConvertToUper();
        DbConvertUtil.defaultNameConvert = converter;
        
        assertEquals("username", DbConvertUtil.toFieldName("username"));
        assertEquals("userName", DbConvertUtil.toFieldName("user_name"));
    }

    @Test
    @DisplayName("测试 toDbColumnName - 使用 ColumnNameToLower")
    void testToDbColumnName_WithToLowerConverter() {
        NameConvertToLower converter = new NameConvertToLower();
        DbConvertUtil.defaultNameConvert = converter;
        
        assertEquals("user_name", DbConvertUtil.toDbName("userName"));
        assertEquals("user_name", DbConvertUtil.toDbName("USER_NAME"));
    }

    @Test
    @DisplayName("测试 toDbColumnName - 使用 ColumnNameToUper")
    void testToDbColumnName_WithToUperConverter() {
        NameConvertToUper converter = new NameConvertToUper();
        DbConvertUtil.defaultNameConvert = converter;
        
        assertEquals("user_name", DbConvertUtil.toDbName("userName"));
        assertEquals("user_name", DbConvertUtil.toDbName("USER_NAME"));
    }

    @Test
    @DisplayName("测试 getFirstColumn - 使用自定义转换器")
    void testGetFirstColumn_WithCustomConverter() {
        NameConvertToLower converter = new NameConvertToLower();
        DbConvertUtil.defaultNameConvert = converter;
        
        ResultMap map = new ResultMap();
        map.put("id", 1);
        map.put("name", "test");
        
        Object result = DbConvertUtil.getFirstColumn(map);
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试 getColumnList - 使用自定义转换器")
    void testGetColumnList_WithCustomConverter() {
        NameConvertToLower converter = new NameConvertToLower();
        DbConvertUtil.defaultNameConvert = converter;
        
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
    void testToDbNames_Multiple() {
        String result = DbConvertUtil.toDbColumnNames("userName userId");
        assertEquals("user_name user_id", result);
    }

    @Test
    @DisplayName("测试 toDbColumnNames - 空字符串")
    void testToDbNames_Empty() {
        String result = DbConvertUtil.toDbColumnNames("");
        assertEquals("", result);
    }


    @Test
    @DisplayName("测试 getVal4Db - null 映射器")
    void testGetVal4Db_NullMapper() {
        // 当映射器为 null 时，返回原值
        Object result = DbConvertUtil.getVal4Db("user", "name", "test");
        assertEquals("test", result);
    }

    @Test
    @DisplayName("测试 getFirstColumnWithType - String 类型")
    void testGetFirstColumnWithType_String() {
        ResultMap map = new ResultMap();
        map.put("name", "test");
        
        String result = DbConvertUtil.getFirstColumn(map, String.class);
        assertEquals("test", result);
    }

    @Test
    @DisplayName("测试 getFirstColumnWithType - Integer 类型")
    void testGetFirstColumnWithType_Integer() {
        ResultMap map = new ResultMap();
        map.put("id", "123");
        
        Integer result = DbConvertUtil.getFirstColumn(map, Integer.class);
        assertEquals(123, result);
    }

    @Test
    @DisplayName("测试 getFirstColumnWithType - Long 类型")
    void testGetFirstColumnWithType_Long() {
        ResultMap map = new ResultMap();
        map.put("id", "123456789");
        
        Long result = DbConvertUtil.getFirstColumn(map, Long.class);
        assertEquals(123456789L, result);
    }

    @Test
    @DisplayName("测试 getFirstColumnWithType - Double 类型")
    void testGetFirstColumnWithType_Double() {
        ResultMap map = new ResultMap();
        map.put("price", "12.34");
        
        Double result = DbConvertUtil.getFirstColumn(map, Double.class);
        assertEquals(12.34, result, 0.001);
    }

    @Test
    @DisplayName("测试 getFirstColumnWithType - Boolean 类型")
    void testGetFirstColumnWithType_Boolean() {
        ResultMap map = new ResultMap();
        map.put("active", "true");
        
        Boolean result = DbConvertUtil.getFirstColumn(map, Boolean.class);
        assertTrue(result);
    }

    @Test
    @DisplayName("测试 getFirstColumnWithType - null 类型")
    void testGetFirstColumnWithType_NullType() {
        ResultMap map = new ResultMap();
        map.put("name", "test");
        
        String result = DbConvertUtil.getFirstColumn(map, null);
        assertEquals("test", result);
    }

    @Test
    @DisplayName("测试 getFirstColumnWithType - Map 为空")
    void testGetFirstColumnWithType_NullMap() {
        String result = DbConvertUtil.getFirstColumn(null, String.class);
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
    void testRoundTrip_ToFieldNameThenToDbName() {
        String original = "user_name";
        String fieldName = DbConvertUtil.toFieldName(original);
        String dbColumn = DbConvertUtil.toDbName(fieldName);
        
        // 驼峰转下划线再回驼峰再回下划线
        assertEquals("user_name", dbColumn);
    }

    @Test
    @DisplayName("测试往返转换 - toDbColumnName 然后 toFieldName")
    void testRoundTrip_ToDbColumnNameThenToFieldName() {
        String original = "userName";
        String dbColumn = DbConvertUtil.toDbName(original);
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
        
        result = DbConvertUtil.toDbName("userNameInfo");
        assertEquals("user_name_info", result);
    }

    @Test
    @DisplayName("测试数字处理")
    void testNumbers() {
        // 测试包含数字的字段名
        String result = DbConvertUtil.toFieldName("user1_name2");
        assertEquals("user1Name2", result);
        
        result = DbConvertUtil.toDbName("user1Name2");
        assertEquals("user1_name2", result);
    }
}
