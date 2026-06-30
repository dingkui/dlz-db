package com.dlz.test.db.cases.docs;

import com.dlz.db.modal.DB;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.SysSql;
import com.dlz.test.db.entity.User;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Doc00AllTest extends BaseDBTest {
    @Before
    public void addSql() {
        SysSql SysSql = new SysSql();
        SysSql.setSqlKey("test");
        SysSql.setSqlValue("SELECT * FROM user WHERE and status = #{status}");
        try {
            DB.Pojo.insert(SysSql);
            fail("应该抛出 SystemException");
        } catch (SystemException e) {
            assertTrue(e.getMessage().contains("SysSql.id为手动输入"));
        }
    }

    @Test
    public void allTest_0_1() {
        DB.Pojo.deleteW(User.class).execute();
    }

}
