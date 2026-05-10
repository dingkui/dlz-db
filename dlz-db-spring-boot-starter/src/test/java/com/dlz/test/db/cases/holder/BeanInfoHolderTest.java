package com.dlz.test.db.cases.holder;

import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.test.db.entity.*;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class BeanInfoHolderTest {

    @Test
    public void getIdField_withExplicitTableId() {
        Field idField = BeanInfoHolder.getIdField(SysSql.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
        assertTrue(BeanInfoHolder.isColumnPk(idField));
    }

    @Test
    public void getIdField_withAutoType() {
        Field idField = BeanInfoHolder.getIdField(AutoIdEntity.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
        assertTrue(BeanInfoHolder.isColumnPk(idField));
    }

    @Test
    public void getIdField_withAssignIdType() {
        Field idField = BeanInfoHolder.getIdField(Orders.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
        assertTrue(BeanInfoHolder.isColumnPk(idField));
    }

    @Test
    public void getIdField_noAnnotationButNamedId() {
        Field idField = BeanInfoHolder.getIdField(User.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
    }

    @Test
    public void getIdField_cacheReturnsSameInstance() {
        Field f1 = BeanInfoHolder.getIdField(Goods.class);
        Field f2 = BeanInfoHolder.getIdField(Goods.class);
        assertSame("getIdField 应缓存并返回同一 Field 实例", f1, f2);
    }
}
