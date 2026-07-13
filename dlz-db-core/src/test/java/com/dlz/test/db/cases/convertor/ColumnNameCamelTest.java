package com.dlz.test.db.cases.convertor;

import com.dlz.db.mapper.name.NameConvertCamel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnNameCamel 测试类
 * 
 * @author test
 */
@DisplayName("驼峰命名转换器测试")
class ColumnNameCamelTest {

    private NameConvertCamel converter;

    @BeforeEach
    void setUp() {
        converter = new NameConvertCamel();
    }

    // ========== toFieldName 测试 ==========

    @Test
    @DisplayName("toFieldName - 基本下划线转驼峰")
    void testToFieldName_Basic() {
        assertEquals("userName", converter.toFieldName("user_name"));
        assertEquals("userId", converter.toFieldName("user_id"));
        assertEquals("createTime", converter.toFieldName("create_time"));
        assertEquals("userNameInfo", converter.toFieldName("user_name_info"));
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
        assertEquals("UserName", converter.toFieldName("UserName"));
        assertEquals("userName", converter.toFieldName("userName"));
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

    // ========== toDbName 测试 ==========


    @Test
    @DisplayName("toDbColumnName - 全小写")
    void testToDbName_AllLowerCase() {
        assertEquals("name", converter.toDbName("name"));
    }
    @Test
    @DisplayName("toDbColumnName - 基本驼峰转下划线")
    void testToDbName_Basic() {
        assertEquals("user_name", converter.toDbName("userName"));
        assertEquals("user_id", converter.toDbName("userId"));
        assertEquals("create_time", converter.toDbName("createTime"));
        assertEquals("deleted", converter.toDbName("deleted"));
    }
    @Test
    @DisplayName("toDbColumnName - 已包含下划线不转换")
    void testToDbName_AlreadyHasUnderscore() {
        assertEquals("user_name", converter.toDbName("user_name"));
        assertEquals("user_id", converter.toDbName("USER_ID"));
        assertEquals("create_time", converter.toDbName("create_time"));
    }

    @Test
    @DisplayName("toDbColumnName - 连续大写字母")
    void testToDbName_ConsecutiveUpperCase() {
        assertEquals("user_i_d", converter.toDbName("userID"));
        assertEquals("h_t_t_p_u_r_l", converter.toDbName("hTTPURL"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含数字")
    void testToDbName_WithNumbers() {
        assertEquals("user1_name", converter.toDbName("user1Name"));
        assertEquals("user123", converter.toDbName("user123"));
        assertEquals("user1_name2", converter.toDbName("user1Name2"));
    }

    @Test
    @DisplayName("toDbColumnName - 首字母大写")
    void testToDbName_FirstLetterUpperCase() {
        assertEquals("_user_name", converter.toDbName("UserName"));
        assertEquals("_user", converter.toDbName("User"));
        assertEquals("user_name", converter.toDbName("userName"));
    }

    @Test
    @DisplayName("toDbColumnName - 空字符串")
    void testToDbName_EmptyString() {
        assertEquals("", converter.toDbName(""));
    }

    @Test
    @DisplayName("toDbColumnName - null值")
    void testToDbName_Null() {
        assertNull(converter.toDbName(null));
    }

    // ========== 边界条件测试 ==========
    @Test
    @DisplayName("边界条件 - 单个字符")
    void testEdgeCase_SingleCharacter() {
        assertEquals("a", converter.toFieldName("a"));
        assertEquals("_a", converter.toDbName("A"));
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
        assertEquals("user-name", converter.toDbName("user-name"));
        assertEquals("user.name", converter.toDbName("user.name"));
    }

    @Test
    @DisplayName("边界条件 - 超长字符串")
    void testEdgeCase_VeryLongString() {
        String longCamel = "thisIsAVeryLongFieldNameWithManyWordsInIt";
        String result = converter.toDbName(longCamel);
        
        assertNotNull(result);
        assertTrue(result.contains("_"));
        assertTrue(result.equals(result.toLowerCase(Locale.ROOT)));
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
    void testPerformance_ToDbName() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10000; i++) {
            converter.toDbName("userNameInfo" + i);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "toDbColumnName 性能测试失败，耗时: " + duration + "ms");
    }
}
