package com.dlz.test.db.cases.util;

import com.dlz.db.util.DbEntityUtil;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Menu;
import com.dlz.test.db.entity.Orders;
import com.dlz.test.db.entity.SysSql;
import com.dlz.test.db.entity.User;
import org.junit.Test;

import static org.junit.Assert.*;

public class DbEntityUtilTest extends BaseDBTest {

    @Test
    public void getIdName_withTableId() {
        String idName = DbEntityUtil.getIdName(SysSql.class);
        assertEquals("ID", idName);
    }

    @Test
    public void getIdName_noAnnotationButNamedId() {
        String idName = DbEntityUtil.getIdName(User.class);
        assertEquals("ID", idName);
    }

    @Test(expected = SystemException.class)
    public void getIdName_noIdFieldThrows() {
        DbEntityUtil.getIdName(Object.class);
    }

    @Test
    public void getIdInfo_returnsFieldAndName() {
        DbEntityUtil.IdInfo idInfo = DbEntityUtil.getIdInfo(Orders.class);
        assertNotNull(idInfo);
        assertNotNull(idInfo.getField());
        assertEquals("id", idInfo.getField().getName());
        assertEquals("ID", idInfo.getName());
    }

    @Test
    public void getIdInfo_cacheConsistency() {
        DbEntityUtil.IdInfo idInfo1 = DbEntityUtil.getIdInfo(Menu.class);
        DbEntityUtil.IdInfo idInfo2 = DbEntityUtil.getIdInfo(Menu.class);
        assertSame("Field 实例应来自缓存", idInfo1.getField(), idInfo2.getField());
        assertEquals(idInfo1.getName(), idInfo2.getName());
    }
}
