package com.dlz.test.db.cases.docs;

import com.dlz.db.DB;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.SysSql;
import com.dlz.test.db.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class Doc00AllTest extends BaseDBTest {
    @BeforeEach
    public void addSql() {
        SysSql SysSql = new SysSql();
        SysSql.setSqlKey("test");
        SysSql.setSqlValue("SELECT * FROM user WHERE AND status = #{status}");
        try {
            DB.pojo.insert(SysSql);
            fail("应该抛出 SystemException");
        } catch (SystemException e) {
            assertTrue(e.getMessage().contains("SysSql.id为手动输入"));
        }
    }

    @Test
    public void allTest_0_1() {
        DB.pojo.deleteWrapper(User.class).execute();
    }

}
