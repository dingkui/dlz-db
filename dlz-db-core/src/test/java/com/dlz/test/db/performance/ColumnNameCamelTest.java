package com.dlz.test.db.performance;

import com.dlz.db.convertor.columnname.ColumnNameCamel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnNameCamel 测试类
 * 
 * @author test
 */
@DisplayName("驼峰命名转换器测试")
class ColumnNameCamelTest {

    private ColumnNameCamel converter;

    @BeforeEach
    void setUp() {
        converter = new ColumnNameCamel();
    }

    // ========== toFieldName 测试 ==========

    @Test
    @DisplayName("toFieldName - 基本下划线转驼峰")
    void testToFieldName_Basic() {
        assertEquals("userName", converter.toFieldName("user_name"));
        assertEquals("userId", converter.toFieldName("user_id"));
        assertEquals("createTime", converter.toFieldName("create_time"));
    }

    @ParameterizedTest
    @DisplayName("toFieldName - 参数化测试")
    @CsvSource({
        "user_name, userName",
        "user_id, userId",
        "create_time, createTime",
        "update_time, updateTime",
        "DELETED , deleted",
        "order_no, orderNo",
        "user_name_info, userNameInfo"
    })
    void testToFieldName_Parameterized(String input, String expected) {
        assertEquals(expected, converter.toFieldName(input));
    }

    @Test
    @DisplayName("toFieldName - 全小写无下划线")
    void testToFieldName_AllLowerCase() {
        assertEquals("name", converter.toFieldName("name"));
        assertEquals("id", converter.toFieldName("id"));
        assertEquals("status", converter.toFieldName("status"));
    }

    @Test
    @DisplayName("toFieldName - 全大写转小写")
    void testToFieldName_AllUpperCase() {
        assertEquals("username", converter.toFieldName("USERNAME"));
        assertEquals("userid", converter.toFieldName("USERID"));
    }

    @Test
    @DisplayName("toFieldName - 大写带下划线")
    void testToFieldName_UpperCaseWithUnderscore() {
        assertEquals("userName", converter.toFieldName("USER_NAME"));
        assertEquals("userId", converter.toFieldName("USER_ID"));
    }

    @Test
    @DisplayName("toFieldName - 混合大小写无下划线")
    void testToFieldName_MixedCaseNoUnderscore() {
        // 混合大小写会被转换
        assertEquals("username", converter.toFieldName("UserName"));
        assertEquals("userid", converter.toFieldName("UserId"));
    }

    @Test
    @DisplayName("toFieldName - 包含数字")
    void testToFieldName_WithNumbers() {
        assertEquals("user1Name", converter.toFieldName("user1_name"));
        assertEquals("user123", converter.toFieldName("user123"));
        assertEquals("user1Name2", converter.toFieldName("user1_name2"));
    }

    @Test
    @DisplayName("toFieldName - 多个连续下划线")
    void testToFieldName_MultipleUnderscores() {
        assertEquals("userName", converter.toFieldName("user__name"));
        assertEquals("userNameInfo", converter.toFieldName("user_name__info"));
    }

    @Test
    @DisplayName("toFieldName - 开头和结尾的下划线")
    void testToFieldName_LeadingTrailingUnderscore() {
        assertEquals("UserName", converter.toFieldName("_user_name"));
        assertEquals("userName", converter.toFieldName("user_name_"));
        assertEquals("UserName", converter.toFieldName("_user_name_"));
    }

    @Test
    @DisplayName("toFieldName - 空字符串")
    void testToFieldName_EmptyString() {
        assertEquals("", converter.toFieldName(""));
    }

    @Test
    @DisplayName("toFieldName - null值")
    void testToFieldName_Null() {
        assertEquals("", converter.toFieldName(null));
    }

    // ========== toDbColumnName 测试 ==========

    @Test
    @DisplayName("toDbColumnName - 基本驼峰转下划线")
    void testToDbColumnName_Basic() {
        assertEquals("USER_NAME", converter.toDbColumnName("userName"));
        assertEquals("USER_ID", converter.toDbColumnName("userId"));
        assertEquals("CREATE_TIME", converter.toDbColumnName("createTime"));
    }

    @ParameterizedTest
    @DisplayName("toDbColumnName - 参数化测试")
    @CsvSource({
        "userName, USER_NAME",
        "userId, USER_ID",
        "createTime, CREATE_TIME",
        "updateTime, UPDATE_TIME",
        "deleted, DELETED ",
        "orderNo, ORDER_NO"
    })
    void testToDbColumnName_Parameterized(String input, String expected) {
        assertEquals(expected, converter.toDbColumnName(input));
    }

    @Test
    @DisplayName("toDbColumnName - 全小写")
    void testToDbColumnName_AllLowerCase() {
        assertEquals("NAME", converter.toDbColumnName("name"));
        assertEquals("ID", converter.toDbColumnName("id"));
        assertEquals("STATUS", converter.toDbColumnName("status"));
    }

    @Test
    @DisplayName("toDbColumnName - 全大写不转换")
    void testToDbColumnName_AllUpperCase() {
        assertEquals("ID", converter.toDbColumnName("ID"));
        assertEquals("NAME", converter.toDbColumnName("NAME"));
        assertEquals("STATUS", converter.toDbColumnName("STATUS"));
    }

    @Test
    @DisplayName("toDbColumnName - 已包含下划线不转换")
    void testToDbColumnName_AlreadyHasUnderscore() {
        assertEquals("user_name", converter.toDbColumnName("user_name"));
        assertEquals("USER_ID", converter.toDbColumnName("USER_ID"));
        assertEquals("create_time", converter.toDbColumnName("create_time"));
    }

    @Test
    @DisplayName("toDbColumnName - 连续大写字母")
    void testToDbColumnName_ConsecutiveUpperCase() {
        assertEquals("USER_I_D", converter.toDbColumnName("userID"));
        assertEquals("H_T_T_P_U_R_L", converter.toDbColumnName("hTTPURL"));
        assertEquals("X_M_L_PARSER", converter.toDbColumnName("xMLParser"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含数字")
    void testToDbColumnName_WithNumbers() {
        assertEquals("USER1_NAME", converter.toDbColumnName("user1Name"));
        assertEquals("USER123", converter.toDbColumnName("user123"));
        assertEquals("USER1_NAME2", converter.toDbColumnName("user1Name2"));
    }

    @Test
    @DisplayName("toDbColumnName - 首字母大写")
    void testToDbColumnName_FirstLetterUpperCase() {
        assertEquals("_USER_NAME", converter.toDbColumnName("UserName"));
        assertEquals("_USER_ID", converter.toDbColumnName("UserId"));
    }

    @Test
    @DisplayName("toDbColumnName - 空字符串")
    void testToDbColumnName_EmptyString() {
        assertEquals("", converter.toDbColumnName(""));
    }

    @Test
    @DisplayName("toDbColumnName - null值")
    void testToDbColumnName_Null() {
        assertNull(converter.toDbColumnName(null));
    }

    // ========== 往返转换测试 ==========

    @Test
    @DisplayName("往返转换 - 驼峰到下划线再回驼峰")
    void testRoundTrip_CamelToUnderscoreAndBack() {
        String original = "userName";
        String dbColumn = converter.toDbColumnName(original);
        String backToField = converter.toFieldName(dbColumn);
        
        assertEquals(original, backToField);
    }

    @Test
    @DisplayName("往返转换 - 下划线到驼峰再回下划线")
    void testRoundTrip_UnderscoreToCamelAndBack() {
        String original = "USER_NAME";
        String fieldName = converter.toFieldName(original);
        String backToDb = converter.toDbColumnName(fieldName);
        
        assertEquals(original, backToDb);
    }

    @ParameterizedTest
    @DisplayName("往返转换 - 多个测试用例")
    @ValueSource(strings = {
        "userName",
        "userId",
        "createTime",
        "updateTime",
        "deleted",
        "orderNo"
    })
    void testRoundTrip_Multiple(String original) {
        String dbColumn = converter.toDbColumnName(original);
        String backToField = converter.toFieldName(dbColumn);
        
        assertEquals(original, backToField);
    }

    // ========== 边界条件测试 ==========

    @Test
    @DisplayName("边界条件 - 单个字符")
    void testEdgeCase_SingleCharacter() {
        assertEquals("a", converter.toFieldName("a"));
        assertEquals("A", converter.toDbColumnName("A"));
    }

    @Test
    @DisplayName("边界条件 - 只有下划线")
    void testEdgeCase_OnlyUnderscore() {
        assertEquals("", converter.toFieldName("_"));
        assertEquals("", converter.toFieldName("__"));
    }

    @Test
    @DisplayName("边界条件 - 特殊字符")
    void testEdgeCase_SpecialCharacters() {
        // 包含下划线的不转换
        assertEquals("USER-NAME", converter.toDbColumnName("user-name"));
        assertEquals("USER.NAME", converter.toDbColumnName("user.name"));
    }

    @Test
    @DisplayName("边界条件 - 超长字符串")
    void testEdgeCase_VeryLongString() {
        String longCamel = "thisIsAVeryLongFieldNameWithManyWordsInIt";
        String result = converter.toDbColumnName(longCamel);
        
        assertNotNull(result);
        assertTrue(result.contains("_"));
        assertTrue(result.equals(result.toUpperCase()));
    }

    // ========== 性能测试 ==========

    @Test
    @DisplayName("性能测试 - toFieldName")
    void testPerformance_ToFieldName() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10000; i++) {
            converter.toFieldName("user_name_info_" + i);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "toFieldName 性能测试失败，耗时: " + duration + "ms");
    }

    @Test
    @DisplayName("性能测试 - toDbColumnName")
    void testPerformance_ToDbColumnName() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10000; i++) {
            converter.toDbColumnName("userNameInfo" + i);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "toDbColumnName 性能测试失败，耗时: " + duration + "ms");
    }
}
