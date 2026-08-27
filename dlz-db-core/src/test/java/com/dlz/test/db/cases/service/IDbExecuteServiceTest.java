package com.dlz.test.db.cases.service;

import com.dlz.db.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.AutoIdEntity;
import com.dlz.test.db.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 回归测试：验证 IDbExecuteService 精简后核心方法仍可用
 */
@Slf4j
public class IDbExecuteServiceTest extends BaseDBTest {

    @Test
    public void executeAutoBackfill() {
        AutoIdEntity e = new AutoIdEntity();
        e.setName("svc_auto");
        assertNull(e.getId());
        DB.pojo.insert(e);
        assertNotNull(e.getId(), "execute 对 AUTO 应回填主键");
        assertTrue(e.getId() > 0, "回填的主键应大于 0");
    }

    @Test
    public void executeAssignIdBackfill() {
        Orders o = new Orders();
        o.setUserId("svc_assign");
        o.setAmount(70);
        assertNull(o.getId());
        DB.pojo.insert(o);
        assertNotNull(o.getId(), "execute 对 ASSIGN_ID 应预生成并回填主键");
    }

    @Test
    public void insertWithAutoKeyReturnsKey() {
        AutoIdEntity e = new AutoIdEntity();
        e.setName("svc_key");
        DB.pojo.insert(e);
        assertNotNull(e.getId(), "insertWithAutoKey 应返回生成的主键");
    }
}
