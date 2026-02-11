package com.dlz.test.db.cases.docs;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.SysSql;
import com.dlz.test.db.entity.User;
import org.junit.Before;
import org.junit.Test;

public class Doc00AllTest extends SpingDbBaseTest {
    @Before
    public void addSql() {
        SysSql SysSql = new SysSql();
        SysSql.setSqlKey("test");
        SysSql.setSqlValue("SELECT * FROM user WHERE and status = #{status}");
        DB.Pojo.insert(SysSql).execute();
    }

    @Test
    public void allTest_0_1() {
        DB.Pojo.delete(User.class).execute();
    }

}
