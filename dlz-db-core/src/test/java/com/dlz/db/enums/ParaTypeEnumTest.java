package com.dlz.db.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ParaTypeEnum 测试类
 * 
 * @author test
 */
@DisplayName("参数类型枚举测试")
class ParaTypeEnumTest {

    @Test
    @DisplayName("测试枚举值 - Date")
    void testDateEnum() {
        assertEquals("Date", ParaTypeEnum.Date.name());
    }

    @Test
    @DisplayName("测试枚举值 - Blob")
    void testBlobEnum() {
        assertEquals("Blob", ParaTypeEnum.Blob.name());
    }

    @Test
    @DisplayName("测试所有枚举值数量")
    void testAllEnumValues() {
        assertEquals(2, ParaTypeEnum.values().length);
    }
}
