package com.dlz.test.db.cases.util;

import com.dlz.db.annotation.IdType;
import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.db.modal.wrapper.WrapperBuildUtil;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.AutoIdEntity;
import com.dlz.test.db.entity.Orders;
import com.dlz.test.db.entity.SysSql;
import com.dlz.test.db.entity.User;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

public class WrapperBuildUtilTest extends SpingDbBaseTest {

    @Test
    public void getIdType_withExplicitInput() {
        Field idField = BeanInfoHolder.getIdField(SysSql.class);
        IdType idType = WrapperBuildUtil.getIdType(idField);
        assertEquals(IdType.INPUT, idType);
    }

    @Test
    public void getIdType_withAssignId() {
        Field idField = BeanInfoHolder.getIdField(Orders.class);
        IdType idType = WrapperBuildUtil.getIdType(idField);
        assertEquals(IdType.ASSIGN_ID, idType);
    }

    @Test
    public void getIdType_autoType() {
        Field idField = BeanInfoHolder.getIdField(AutoIdEntity.class);
        IdType idType = WrapperBuildUtil.getIdType(idField);
        assertEquals(IdType.AUTO, idType);
    }

    @Test
    public void getIdType_noAnnotationReturnsNull() {
        Field idField = BeanInfoHolder.getIdField(User.class);
        IdType idType = WrapperBuildUtil.getIdType(idField);
        assertNull(idType);
    }

    @Test
    public void buildUpdateSql_withCustomIdName() {
        List<Field> fields = BeanInfoHolder.getBeanFields(SysSql.class);
        String sql = WrapperBuildUtil.buildUpdateSql("SYS_SQL", fields, "ID");
        assertTrue("动态 idName 应出现在 WHERE 条件", sql.contains("WHERE ID = ?"));
        assertFalse("SET 中不应包含主键", sql.contains("SET ID="));
    }

    @Test
    public void buildUpdateParams_withCustomIdName() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        dict.setName("test_update");
        List<Field> fields = BeanInfoHolder.getBeanFields(SysSql.class);
        Object[] params = WrapperBuildUtil.buildUpdateParams(dict, fields, "ID");
        assertTrue("参数数组长度应大于 0", params.length > 0);
        assertEquals("最后一个参数应是主键值", 123L, params[params.length - 1]);
    }

    @Test
    public void buildInsertParams_noAutoIdFill() {
        SysSql dict = new SysSql();
        dict.setId(456L);
        dict.setSqlKey("test_key");
        List<Field> fields = BeanInfoHolder.getBeanFields(SysSql.class);
        Object[] params = WrapperBuildUtil.buildInsertParams(dict, fields);
        assertNotNull(params);
        assertTrue("参数数组应包含主键值", java.util.Arrays.asList(params).contains(456L));
    }
}
