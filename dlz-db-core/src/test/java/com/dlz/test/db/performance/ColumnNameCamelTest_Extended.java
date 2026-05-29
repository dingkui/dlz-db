package com.dlz.test.db.cases.convertor.columnname;

import com.dlz.db.convertor.columnname.ColumnNameCamel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ColumnNameCamel 扩展测试类
 *
 * @author test
 */
@DisplayName("驼峰命名转换器扩展测试")
class ColumnNameCamelTest_Extended {

    private ColumnNameCamel converter;

    @BeforeEach
    void setUp() {
        converter = new ColumnNameCamel();
    }

    // ========== toFieldName 扩展测试 ==========

    @Test
    @DisplayName("toFieldName - 包含连字符")
    void testToFieldName_WithHyphen() {
        // 连字符不会被转换
        assertEquals("user-name", converter.toFieldName("user-name"));
        assertEquals("user-name-info", converter.toFieldName("user-name-info"));
    }

    @Test
    @DisplayName("toFieldName - 包含点号")
    void testToFieldName_WithDot() {
        // 点号不会被转换
        assertEquals("user.name", converter.toFieldName("user.name"));
        assertEquals("user.name.info", converter.toFieldName("user.name.info"));
    }

    @Test
    @DisplayName("toFieldName - 包含斜杠")
    void testToFieldName_WithSlash() {
        // 斜杠不会被转换
        assertEquals("user/name", converter.toFieldName("user/name"));
    }

    @Test
    @DisplayName("toFieldName - 包含反斜杠")
    void testToFieldName_WithBackslash() {
        // 反斜杠不会被转换
        assertEquals("user\\name", converter.toFieldName("user\\name"));
    }

    @Test
    @DisplayName("toFieldName - 包含括号")
    void testToFieldName_WithParentheses() {
        // 括号不会被转换
        assertEquals("user(name)", converter.toFieldName("user(name)"));
    }

    @Test
    @DisplayName("toFieldName - 包含方括号")
    void testToFieldName_WithBrackets() {
        // 方括号不会被转换
        assertEquals("user[name]", converter.toFieldName("user[name]"));
    }

    @Test
    @DisplayName("toFieldName - 包含花括号")
    void testToFieldName_WithBraces() {
        // 花括号不会被转换
        assertEquals("user{name}", converter.toFieldName("user{name}"));
    }

    @Test
    @DisplayName("toFieldName - 包含特殊符号")
    void testToFieldName_WithSpecialSymbols() {
        // 特殊符号不会被转换
        assertEquals("user@name", converter.toFieldName("user@name"));
        assertEquals("user#name", converter.toFieldName("user#name"));
        assertEquals("user$name", converter.toFieldName("user$name"));
    }

    @Test
    @DisplayName("toFieldName - 包含管道符")
    void testToFieldName_WithPipe() {
        // 管道符不会被转换
        assertEquals("user|name", converter.toFieldName("user|name"));
    }

    @Test
    @DisplayName("toFieldName - 包含问号")
    void testToFieldName_WithQuestionMark() {
        // 问号不会被转换
        assertEquals("user?name", converter.toFieldName("user?name"));
    }

    @Test
    @DisplayName("toFieldName - 包含感叹号")
    void testToFieldName_WithExclamation() {
        // 感叹号不会被转换
        assertEquals("user!name", converter.toFieldName("user!name"));
    }

    @Test
    @DisplayName("toFieldName - 包含百分号")
    void testToFieldName_WithPercent() {
        // 百分号不会被转换
        assertEquals("user%name", converter.toFieldName("user%name"));
    }

    @Test
    @DisplayName("toFieldName - 包含和号")
    void testToFieldName_WithAmpersand() {
        // 和号不会被转换
        assertEquals("user&name", converter.toFieldName("user&name"));
    }

    @Test
    @DisplayName("toFieldName - 包含星号")
    void testToFieldName_WithAsterisk() {
        // 星号不会被转换
        assertEquals("user*name", converter.toFieldName("user*name"));
    }

    @Test
    @DisplayName("toFieldName - 包含加号")
    void testToFieldName_WithPlus() {
        // 加号不会被转换
        assertEquals("user+name", converter.toFieldName("user+name"));
    }

    @Test
    @DisplayName("toFieldName - 包含等号")
    void testToFieldName_WithEquals() {
        // 等号不会被转换
        assertEquals("user=name", converter.toFieldName("user=name"));
    }

    @Test
    @DisplayName("toFieldName - 包含分号")
    void testToFieldName_WithSemicolon() {
        // 分号不会被转换
        assertEquals("user;name", converter.toFieldName("user;name"));
    }

    @Test
    @DisplayName("toFieldName - 包含冒号")
    void testToFieldName_WithColon() {
        // 冒号不会被转换
        assertEquals("user:name", converter.toFieldName("user:name"));
    }

    @Test
    @DisplayName("toFieldName - 包含引号")
    void testToFieldName_WithQuotes() {
        // 引号不会被转换
        assertEquals("user'name", converter.toFieldName("user'name"));
        assertEquals("user\"name", converter.toFieldName("user\"name"));
    }

    @Test
    @DisplayName("toFieldName - 包含尖括号")
    void testToFieldName_WithAngleBrackets() {
        // 尖括号不会被转换
        assertEquals("user<name>", converter.toFieldName("user<name>"));
    }

    @Test
    @DisplayName("toFieldName - 包含波浪号")
    void testToFieldName_WithTilde() {
        // 波浪号不会被转换
        assertEquals("user~name", converter.toFieldName("user~name"));
    }

    @Test
    @DisplayName("toFieldName - 包含反引号")
    void testToFieldName_WithBacktick() {
        // 反引号不会被转换
        assertEquals("user`name", converter.toFieldName("user`name"));
    }

    // ========== toDbColumnName 扩展测试 ==========

    @Test
    @DisplayName("toDbColumnName - 包含连字符")
    void testToDbColumnName_WithHyphen() {
        // 连字符不会被转换
        assertEquals("USER-NAME", converter.toDbColumnName("user-name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含点号")
    void testToDbColumnName_WithDot() {
        // 点号不会被转换
        assertEquals("USER.NAME", converter.toDbColumnName("user.name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含斜杠")
    void testToDbColumnName_WithSlash() {
        // 斜杠不会被转换
        assertEquals("USER/NAME", converter.toDbColumnName("user/name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含反斜杠")
    void testToDbColumnName_WithBackslash() {
        // 反斜杠不会被转换
        assertEquals("USER\\NAME", converter.toDbColumnName("user\\name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含括号")
    void testToDbColumnName_WithParentheses() {
        // 括号不会被转换
        assertEquals("USER(NAME)", converter.toDbColumnName("user(name)"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含方括号")
    void testToDbColumnName_WithBrackets() {
        // 方括号不会被转换
        assertEquals("USER[NAME]", converter.toDbColumnName("user[name]"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含花括号")
    void testToDbColumnName_WithBraces() {
        // 花括号不会被转换
        assertEquals("USER{NAME}", converter.toDbColumnName("user{name}"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含特殊符号")
    void testToDbColumnName_WithSpecialSymbols() {
        // 特殊符号不会被转换
        assertEquals("USER@NAME", converter.toDbColumnName("user@name"));
        assertEquals("USER#NAME", converter.toDbColumnName("user#name"));
        assertEquals("USER$NAME", converter.toDbColumnName("user$name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含管道符")
    void testToDbColumnName_WithPipe() {
        // 管道符不会被转换
        assertEquals("USER|NAME", converter.toDbColumnName("user|name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含问号")
    void testToDbColumnName_WithQuestionMark() {
        // 问号不会被转换
        assertEquals("USER?NAME", converter.toDbColumnName("user?name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含感叹号")
    void testToDbColumnName_WithExclamation() {
        // 感叹号不会被转换
        assertEquals("USER!NAME", converter.toDbColumnName("user!name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含百分号")
    void testToDbColumnName_WithPercent() {
        // 百分号不会被转换
        assertEquals("USER%NAME", converter.toDbColumnName("user%name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含和号")
    void testToDbColumnName_WithAmpersand() {
        // 和号不会被转换
        assertEquals("USER&NAME", converter.toDbColumnName("user&name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含星号")
    void testToDbColumnName_WithAsterisk() {
        // 星号不会被转换
        assertEquals("USER*NAME", converter.toDbColumnName("user*name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含加号")
    void testToDbColumnName_WithPlus() {
        // 加号不会被转换
        assertEquals("USER+NAME", converter.toDbColumnName("user+name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含等号")
    void testToDbColumnName_WithEquals() {
        // 等号不会被转换
        assertEquals("USER=NAME", converter.toDbColumnName("user=name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含分号")
    void testToDbColumnName_WithSemicolon() {
        // 分号不会被转换
        assertEquals("USER;NAME", converter.toDbColumnName("user;name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含冒号")
    void testToDbColumnName_WithColon() {
        // 冒号不会被转换
        assertEquals("USER:NAME", converter.toDbColumnName("user:name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含引号")
    void testToDbColumnName_WithQuotes() {
        // 引号不会被转换
        assertEquals("USER'NAME", converter.toDbColumnName("user'name"));
        assertEquals("USER\"NAME", converter.toDbColumnName("user\"name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含尖括号")
    void testToDbColumnName_WithAngleBrackets() {
        // 尖括号不会被转换
        assertEquals("USER<NAME>", converter.toDbColumnName("user<name>"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含波浪号")
    void testToDbColumnName_WithTilde() {
        // 波浪号不会被转换
        assertEquals("USER~NAME", converter.toDbColumnName("user~name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含反引号")
    void testToDbColumnName_WithBacktick() {
        // 反引号不会被转换
        assertEquals("USER`NAME", converter.toDbColumnName("user`name"));
    }

    // ========== 边界条件扩展测试 ==========


    @Test
    @DisplayName("边界条件 - 只包含数字")
    void testEdgeCase_OnlyNumbers() {
        assertEquals("123456", converter.toFieldName("123456"));
        assertEquals("123456", converter.toDbColumnName("123456"));
    }

    @Test
    @DisplayName("边界条件 - 只包含空格")
    void testEdgeCase_OnlySpaces() {
        assertEquals("   ", converter.toFieldName("   "));
        assertEquals("   ", converter.toDbColumnName("   "));
    }

    @Test
    @DisplayName("边界条件 - 只包含制表符")
    void testEdgeCase_OnlyTabs() {
        assertEquals("\t\t\t", converter.toFieldName("\t\t\t"));
        assertEquals("\t\t\t", converter.toDbColumnName("\t\t\t"));
    }

    @Test
    @DisplayName("边界条件 - 只包含换行符")
    void testEdgeCase_OnlyNewlines() {
        assertEquals("\n\n\n", converter.toFieldName("\n\n\n"));
        assertEquals("\n\n\n", converter.toDbColumnName("\n\n\n"));
    }

    @Test
    @DisplayName("边界条件 - 只包含回车符")
    void testEdgeCase_OnlyCarriageReturns() {
        assertEquals("\r\r\r", converter.toFieldName("\r\r\r"));
        assertEquals("\r\r\r", converter.toDbColumnName("\r\r\r"));
    }

    @Test
    @DisplayName("边界条件 - 只包含 Unicode 字符")
    void testEdgeCase_OnlyUnicode() {
        assertEquals("测试", converter.toFieldName("测试"));
        assertEquals("テスト", converter.toDbColumnName("テスト"));
    }

    @Test
    @DisplayName("边界条件 - 只包含 emoji")
    void testEdgeCase_OnlyEmoji() {
        assertEquals("😀😀😀", converter.toFieldName("😀😀😀"));
        assertEquals("😀😀😀", converter.toDbColumnName("😀😀😀"));
    }

    @Test
    @DisplayName("边界条件 - 混合 Unicode 和字母")
    void testEdgeCase_MixedUnicodeAndLetters() {
        assertEquals("测试username", converter.toFieldName("测试userName"));
        assertEquals("测试USER_NAME", converter.toDbColumnName("测试userName"));
    }

    @Test
    @DisplayName("边界条件 - 混合 emoji 和字母")
    void testEdgeCase_MixedEmojiAndLetters() {
        assertEquals("😀username", converter.toFieldName("😀userName"));
        assertEquals("😀USER_NAME", converter.toDbColumnName("😀userName"));
    }

    // ========== 性能扩展测试 ==========

    @Test
    @DisplayName("性能测试 - toFieldName - 大量数据")
    void testPerformance_ToFieldName_LargeData() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            converter.toFieldName("user_name_info_" + i);
        }

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 5000, "toFieldName 性能测试失败，耗时: " + duration + "ms");
    }

    @Test
    @DisplayName("性能测试 - toDbColumnName - 大量数据")
    void testPerformance_ToDbColumnName_LargeData() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            converter.toDbColumnName("userNameInfo" + i);
        }

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 5000, "toDbColumnName 性能测试失败，耗时: " + duration + "ms");
    }

    @Test
    @DisplayName("性能测试 - toFieldName - 特殊字符")
    void testPerformance_ToFieldName_SpecialChars() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            converter.toFieldName("user_name_info_" + i + "!@#$%^&*()");
        }

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "toFieldName 性能测试失败，耗时: " + duration + "ms");
    }

    @Test
    @DisplayName("性能测试 - toDbColumnName - 特殊字符")
    void testPerformance_ToDbColumnName_SpecialChars() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            converter.toDbColumnName("userNameInfo" + i + "!@#$%^&*()");
        }

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "toDbColumnName 性能测试失败，耗时: " + duration + "ms");
    }
}
