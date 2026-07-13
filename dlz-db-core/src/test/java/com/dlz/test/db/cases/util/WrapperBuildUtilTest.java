package com.dlz.test.db.cases.util;

import com.dlz.db.modal.wrapper.WrapperBuildUtil;
import com.dlz.db.support.PojoCache;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.SysSql;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

public class WrapperBuildUtilTest extends BaseDBTest {

    @Test
    public void buildUpdateSql_withCustomIdName() {
        List<Field> fields = PojoCache.getBeanFields(SysSql.class);
        String sql = WrapperBuildUtil.buildUpdateSql("sys_sql", fields, "id");
        assertTrue("动态 idName 应出现在 WHERE 条件", sql.contains("WHERE id = ?"));
        assertFalse("SET 中不应包含主键", sql.contains("SET id="));
    }

    @Test
    public void buildUpdateParams_withCustomIdName() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        dict.setName("test_update");
        List<Field> fields = PojoCache.getBeanFields(SysSql.class);
        Object[] params = WrapperBuildUtil.buildUpdateParams(dict, fields, PojoCache.getIdField(SysSql.class));
        assertTrue("参数数组长度应大于 0", params.length > 0);
        assertEquals("最后一个参数应是主键值", 123L, params[params.length - 1]);
    }

    @Test
    public void buildInsertParams_noAutoIdFill() {
        SysSql dict = new SysSql();
        dict.setId(456L);
        dict.setSqlKey("test_key");
        List<Field> fields = PojoCache.getBeanFields(SysSql.class);
        Object[] params = WrapperBuildUtil.buildInsertParams(dict, fields);
        assertNotNull(params);
        assertTrue("参数数组应包含主键值", java.util.Arrays.asList(params).contains(456L));
    }
}
