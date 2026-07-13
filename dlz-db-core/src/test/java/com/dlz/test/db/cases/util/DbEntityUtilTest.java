package com.dlz.test.db.cases.util;

import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * DbEntityUtil 测试类
 *
 * @author test
 */
@DisplayName("数据库实体工具测试")
@Slf4j
class DbEntityUtilTest extends BaseDBTest {

    @Test
    @DisplayName("测试 getIdName - 获取主键名")
    void testGetIdName() {
        // 这里需要一个带有 @TableId 注解的实体类
        // 由于没有具体的实体类，这里只测试异常情况
        assertThrows(SystemException.class, () -> {
            PojoCache.getIdDbName(String.class);
        });
    }

    @Test
    @DisplayName("测试 getIdInfo - 获取主键信息")
    void testGetIdInfo() {
        // 这里需要一个带有 @TableId 注解的实体类
        assertNull(PojoCache.getIdInfo(String.class));
    }

    @Test
    public void getIdName_withTableId() {
        String idName = PojoCache.getIdDbName(SysSql.class);
        assertEquals("id", idName);
    }

    @Test
    public void getIdName_noAnnotationButNamedId() {
        String idDbName = PojoCache.getIdDbName(User.class);
        assertEquals("id", idDbName);
    }
    @Test
    public void getIdName_noAnnotationButNamedId2() {
        String idName = PojoCache.getIdDbName(TestUser.class);
        assertEquals("id", idName);
    }

    @Test
    public void getIdInfo_returnsFieldAndName() {
        IdInfo idInfo = PojoCache.getIdInfo(Orders.class);
        assertNotNull(idInfo);
        assertNotNull(idInfo.getField());
        assertEquals("id", idInfo.getField().getName());
        assertEquals("id", idInfo.getDbName());
    }

    @Test
    public void getIdInfo_cacheConsistency() {
        IdInfo idInfo1 = PojoCache.getIdInfo(Menu.class);
        IdInfo idInfo2 = PojoCache.getIdInfo(Menu.class);
        assertSame("Field 实例应来自缓存", idInfo1.getField(), idInfo2.getField());
        assertEquals(idInfo1.getDbName(), idInfo2.getDbName());
    }
}
