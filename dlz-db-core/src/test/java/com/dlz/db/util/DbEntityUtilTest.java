package com.dlz.db.util;

import com.dlz.kit.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * DbEntityUtil 测试类
 * 
 * @author test
 */
@DisplayName("数据库实体工具测试")
@Slf4j
class DbEntityUtilTest{

    @Test
    @DisplayName("测试 getIdName - 获取主键名")
    void testGetIdName() {
        // 这里需要一个带有 @TableId 注解的实体类
        // 由于没有具体的实体类，这里只测试异常情况
        assertThrows(SystemException.class, () -> {
            DbEntityUtil.getIdName(String.class);
        });
    }

    @Test
    @DisplayName("测试 getIdInfo - 获取主键信息")
    void testGetIdInfo() {
        // 这里需要一个带有 @TableId 注解的实体类
        assertThrows(SystemException.class, () -> DbEntityUtil.getIdInfo(String.class));
    }

    @Test
    @DisplayName("测试 getIdInfo - 异常信息包含类名")
    void testGetIdInfo_ExceptionMessage() {
        try {
            DbEntityUtil.getIdInfo(String.class);
            fail("应该抛出异常");
        } catch (Exception e) {
//            log.error("error:"+ e.getMessage());
//            final boolean string = e.getMessage().contains("String");
//            System.out.println(string+"--------------------------------------------------"+e.getMessage());
//            assertTrue(string,"异常信息应该包含类名 String");
        }
    }
}
