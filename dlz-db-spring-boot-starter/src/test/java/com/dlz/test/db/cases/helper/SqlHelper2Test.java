package com.dlz.test.db.cases.helper;

import com.dlz.db.helper.bean.TableInfo;
import com.dlz.db.modal.DB;
import com.dlz.test.db.config.SpingDbBaseTest;
import org.junit.Test;


public class SqlHelper2Test extends SpingDbBaseTest {

    @Test
    public void lamdaTest2() {
        TableInfo sys_test = DB.Dynamic.getSqlHelper().getTableInfo("user");
        System.out.println(sys_test);
    }
}