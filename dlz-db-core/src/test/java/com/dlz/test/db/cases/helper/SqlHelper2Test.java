package com.dlz.test.db.cases.helper;

import com.dlz.db.modal.DB;
import com.dlz.db.support.bean.TableInfo;
import com.dlz.db.support.helper.HelperScan;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.Test;


public class SqlHelper2Test extends BaseDBTest {

    @Test
    public void lamdaTest2() {
        TableInfo sys_test = DB.Dynamic.getSqlHelper().getTableInfo("user");
        System.out.println(sys_test);
    }
    @Test
    public void lamdaTest12() {
        HelperScan.scan(null);
        HelperScan.scan("xx");
    }
}