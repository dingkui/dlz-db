package com.dlz.test.db.cases.holder;

import com.dlz.db.support.PojoCache;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.*;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class BeanInfoHolderTest extends BaseDBTest {

    @Test
    public void getIdField_withExplicitTableId() {
        Field idField = PojoCache.getIdField(SysSql.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
        assertTrue(PojoCache.isColumnPk(idField));
    }

    @Test
    public void getIdField_withAutoType() {
        Field idField = PojoCache.getIdField(AutoIdEntity.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
        assertTrue(PojoCache.isColumnPk(idField));
    }

    @Test
    public void getIdField_withAssignIdType() {
        Field idField = PojoCache.getIdField(Orders.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
        assertTrue(PojoCache.isColumnPk(idField));
    }

    @Test
    public void getIdField_noAnnotationButNamedId() {
        Field idField = PojoCache.getIdField(User.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
    }

    @Test
    public void getIdField_cacheReturnsSameInstance() {
        Field f1 = PojoCache.getIdField(Goods.class);
        Field f2 = PojoCache.getIdField(Goods.class);
        assertSame("getIdField 应缓存并返回同一 Field 实例", f1, f2);
    }
}
