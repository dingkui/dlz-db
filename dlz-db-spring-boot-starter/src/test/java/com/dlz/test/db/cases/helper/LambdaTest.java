package com.dlz.test.db.cases.helper;

import com.dlz.db.support.PojoCache;
import com.dlz.kit.fn.DlzFn;
import com.dlz.test.db.entity.Dict;
import org.junit.Test;

public class LambdaTest {


    @Test
    public void LambdaTest1() {
        System.out.println("方法名：" + doSFunction(Dict::getA2));
    }
    private <T, R> String doSFunction(DlzFn<T, R> func) {
        return PojoCache.fnName(func);
    }
}
