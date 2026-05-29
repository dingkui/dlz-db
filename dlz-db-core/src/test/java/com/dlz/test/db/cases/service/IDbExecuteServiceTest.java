package com.dlz.test.db.cases.service;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.AutoIdEntity;
import com.dlz.test.db.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * 回归测试：验证 IDbExecuteService 精简后核心方法仍可用
 */
@Slf4j
public class IDbExecuteServiceTest extends BaseDBTest {
    @Before
    public void setUp() {
        DB.Jdbc.execute("delete from Orders");
        // DB.Jdbc.execute("CREATE TABLE Orders (id INTEGER PRIMARY KEY, user_id TEXT, amount INTEGER)");
    }

    @After
    public void tearDown() {
        DB.Jdbc.execute("delete from Orders");
    }
    @Test
    public void executeAutoBackfill() {
        AutoIdEntity e = new AutoIdEntity();
        e.setName("svc_auto");
        assertNull(e.getId());
        DB.Pojo.insert(e);
        assertNotNull("execute 对 AUTO 应回填主键", e.getId());
        assertTrue("回填的主键应大于 0", e.getId() > 0);
    }

    @Test
    public void executeAssignIdBackfill() {
        Orders o = new Orders();
        o.setUserId("svc_assign");
        o.setAmount(70);
        assertNull(o.getId());
        DB.Pojo.insert(o);
        assertNotNull("execute 对 ASSIGN_ID 应预生成并回填主键", o.getId());
    }

    @Test
    public void insertWithAutoKeyReturnsKey() {
        AutoIdEntity e = new AutoIdEntity();
        e.setName("svc_key");
        DB.Pojo.insert(e);
        assertNotNull("insertWithAutoKey 应返回生成的主键", e.getId());
    }
}
