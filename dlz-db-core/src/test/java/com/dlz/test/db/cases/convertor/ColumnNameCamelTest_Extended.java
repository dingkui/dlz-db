package com.dlz.test.db.cases.convertor;

import com.dlz.db.mapper.name.NameConvertCamel;
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

    private NameConvertCamel converter;

    @BeforeEach
    void setUp() {
        converter = new NameConvertCamel();
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
    void testToDbName_WithHyphen() {
        // 连字符不会被转换
        assertEquals("user-name", converter.toDbName("user-name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含点号")
    void testToDbName_WithDot() {
        // 点号不会被转换
        assertEquals("user.name", converter.toDbName("user.name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含斜杠")
    void testToDbName_WithSlash() {
        // 斜杠不会被转换
        assertEquals("user/name", converter.toDbName("user/name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含反斜杠")
    void testToDbName_WithBackslash() {
        // 反斜杠不会被转换
        assertEquals("user\\name", converter.toDbName("user\\name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含括号")
    void testToDbName_WithParentheses() {
        // 括号不会被转换
        assertEquals("user(name)", converter.toDbName("user(name)"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含方括号")
    void testToDbName_WithBrackets() {
        // 方括号不会被转换
        assertEquals("user[name]", converter.toDbName("user[name]"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含花括号")
    void testToDbName_WithBraces() {
        // 花括号不会被转换
        assertEquals("user{name}", converter.toDbName("user{name}"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含特殊符号")
    void testToDbName_WithSpecialSymbols() {
        // 特殊符号不会被转换
        assertEquals("user@name", converter.toDbName("user@name"));
        assertEquals("user#name", converter.toDbName("user#name"));
        assertEquals("user$name", converter.toDbName("user$name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含管道符")
    void testToDbName_WithPipe() {
        // 管道符不会被转换
        assertEquals("user|name", converter.toDbName("user|name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含问号")
    void testToDbName_WithQuestionMark() {
        // 问号不会被转换
        assertEquals("user?name", converter.toDbName("user?name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含感叹号")
    void testToDbName_WithExclamation() {
        // 感叹号不会被转换
        assertEquals("user!name", converter.toDbName("user!name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含百分号")
    void testToDbName_WithPercent() {
        // 百分号不会被转换
        assertEquals("user%name", converter.toDbName("user%name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含和号")
    void testToDbName_WithAmpersand() {
        // 和号不会被转换
        assertEquals("user&name", converter.toDbName("user&name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含星号")
    void testToDbName_WithAsterisk() {
        // 星号不会被转换
        assertEquals("user*name", converter.toDbName("user*name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含加号")
    void testToDbName_WithPlus() {
        // 加号不会被转换
        assertEquals("user+name", converter.toDbName("user+name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含等号")
    void testToDbName_WithEquals() {
        // 等号不会被转换
        assertEquals("user=name", converter.toDbName("user=name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含分号")
    void testToDbName_WithSemicolon() {
        // 分号不会被转换
        assertEquals("user;name", converter.toDbName("user;name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含冒号")
    void testToDbName_WithColon() {
        // 冒号不会被转换
        assertEquals("user:name", converter.toDbName("user:name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含引号")
    void testToDbName_WithQuotes() {
        // 引号不会被转换
        assertEquals("user'name", converter.toDbName("user'name"));
        assertEquals("user\"name", converter.toDbName("user\"name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含尖括号")
    void testToDbName_WithAngleBrackets() {
        // 尖括号不会被转换
        assertEquals("user<name>", converter.toDbName("user<name>"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含波浪号")
    void testToDbName_WithTilde() {
        // 波浪号不会被转换
        assertEquals("user~name", converter.toDbName("user~name"));
    }

    @Test
    @DisplayName("toDbColumnName - 包含反引号")
    void testToDbName_WithBacktick() {
        // 反引号不会被转换
        assertEquals("user`name", converter.toDbName("user`name"));
    }

    // ========== 边界条件扩展测试 ==========


    @Test
    @DisplayName("边界条件 - 只包含数字")
    void testEdgeCase_OnlyNumbers() {
        assertEquals("123456", converter.toFieldName("123456"));
        assertEquals("123456", converter.toDbName("123456"));
    }

    @Test
    @DisplayName("边界条件 - 只包含空格")
    void testEdgeCase_OnlySpaces() {
        assertEquals("   ", converter.toFieldName("   "));
        assertEquals("   ", converter.toDbName("   "));
    }

    @Test
    @DisplayName("边界条件 - 只包含制表符")
    void testEdgeCase_OnlyTabs() {
        assertEquals("\t\t\t", converter.toFieldName("\t\t\t"));
        assertEquals("\t\t\t", converter.toDbName("\t\t\t"));
    }

    @Test
    @DisplayName("边界条件 - 只包含换行符")
    void testEdgeCase_OnlyNewlines() {
        assertEquals("\n\n\n", converter.toFieldName("\n\n\n"));
        assertEquals("\n\n\n", converter.toDbName("\n\n\n"));
    }

    @Test
    @DisplayName("边界条件 - 只包含回车符")
    void testEdgeCase_OnlyCarriageReturns() {
        assertEquals("\r\r\r", converter.toFieldName("\r\r\r"));
        assertEquals("\r\r\r", converter.toDbName("\r\r\r"));
    }

    @Test
    @DisplayName("边界条件 - 只包含 Unicode 字符")
    void testEdgeCase_OnlyUnicode() {
        assertEquals("测试", converter.toFieldName("测试"));
        assertEquals("テスト", converter.toDbName("テスト"));
    }

    @Test
    @DisplayName("边界条件 - 只包含 emoji")
    void testEdgeCase_OnlyEmoji() {
        assertEquals("😀😀😀", converter.toFieldName("😀😀😀"));
        assertEquals("😀😀😀", converter.toDbName("😀😀😀"));
    }

    @Test
    @DisplayName("边界条件 - 混合 Unicode 和字母")
    void testEdgeCase_MixedUnicodeAndLetters() {
        assertEquals("测试userName", converter.toFieldName("测试userName"));
        assertEquals("测试user_name", converter.toDbName("测试userName"));
    }

    @Test
    @DisplayName("边界条件 - 混合 emoji 和字母")
    void testEdgeCase_MixedEmojiAndLetters() {
        assertEquals("😀userName", converter.toFieldName("😀userName"));
        assertEquals("😀user_name", converter.toDbName("😀userName"));
    }

}
