package com.dlz.test.db.cases.helper;

import com.dlz.kit.util.system.FieldReflections;
import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.Dict;
import org.junit.Test;

public class SqlHelperTest extends SpingDbBaseTest {
    @Test
    public void landaTest1() {
        long t=System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            FieldReflections.getFields(Dict.class);
        }
        System.out.println(System.currentTimeMillis()-t);
    }
    @Test
    public void landaTest2() {
        long t=System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            FieldReflections.getFn(Dict::getDictStatus);
            FieldReflections.getFn(Dict::getA2);
            FieldReflections.getFn(Dict::getA6);
            FieldReflections.getFn(Dict::getA4);
            FieldReflections.getFn(Dict::getA5);
        }
        System.out.println(System.currentTimeMillis()-t);
    }
    @Test
    public void landaTest3() {
        long t=System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            BeanInfoHolder.fnName(Dict::getDictStatus);
            BeanInfoHolder.fnName(Dict::getA2);
            BeanInfoHolder.fnName(Dict::getA6);
            BeanInfoHolder.fnName(Dict::getA4);
            BeanInfoHolder.fnName(Dict::getA5);
        }
        System.out.println(System.currentTimeMillis()-t);
    }

}