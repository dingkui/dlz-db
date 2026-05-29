package com.dlz.test.db.performance;

import com.dlz.db.support.PojoCache;
import com.dlz.kit.util.system.FieldReflections;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Dict;
import org.junit.jupiter.api.Test;

public class SqlHelperTest extends BaseDBTest {
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
            PojoCache.fnName(Dict::getDictStatus);
            PojoCache.fnName(Dict::getA2);
            PojoCache.fnName(Dict::getA6);
            PojoCache.fnName(Dict::getA4);
            PojoCache.fnName(Dict::getA5);
        }
        System.out.println(System.currentTimeMillis()-t);
    }

}