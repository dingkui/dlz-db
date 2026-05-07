package com.dlz.test.db.cases.db;

import com.dlz.db.convertor.clumnname.ColumnNameToLower;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.AutoIdEntity;
import com.dlz.test.db.entity.Orders;
import com.dlz.test.db.entity.SysSql;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class WrapperInsertTest extends SpingDbBaseTest {

    @Test
    public void insertWrapperTest1() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        dict.setSqlKey("xxx");
        PojoInsert<SysSql> insert = PojoInsert.wrapper(dict);
        assertEquals("insert into SYS_SQL(SQL_KEY,ID) values('xxx',123)", toSql(insert));
    }

    @Test
    public void insertWrapperTest2_1() {
        SysSql dict = new SysSql();
        dict.setId(666L);
        dict.setSqlKey("xxx");
        dict.setIsDeleted(0);
        DB.Pojo.delete(SysSql.class).eq(SysSql::getId, 666L).execute();
        DB.Pojo.delete(SysSql.class).eq(SysSql::getId, 666L).setLogicDelete(false).execute();
        PojoInsert<SysSql> insert = PojoInsert.wrapper(dict);
        assertEquals("insert into SYS_SQL(IS_DELETED,SQL_KEY,ID) values(0,'xxx',666)", toSql(insert));
        insert.execute();
        final List<ResultMap> resultMaps = DB.Table.select("Sys_Sql").setAllowFullQuery(true).queryList();
        log.info("resultMaps:"+resultMaps);
        final List<ResultMap> resultMaps2 = DB.Table.select("Sys_Sql").setAllowFullQuery(true).queryList(new ColumnNameToLower());
        log.info("resultMaps:"+resultMaps2);
    }

    @Test
    public void insertWrapperTest2() {
        SysSql dict = new SysSql();
        dict.setSqlKey("xxx");
        PojoInsert<SysSql> insert = PojoInsert.wrapper(dict);
        showSql(insert,"insertWrapperTest2","insert into SYS_SQL(SQL_KEY) values('xxx')");
        try {
            insert.execute();
            fail("应该抛出 SystemException");
        } catch (SystemException e) {
            assertTrue(e.getMessage().contains("为手动输入,不能为空"));
        }
    }

    @Test
    public void insertExecuteAutoBackfillTest() {
        AutoIdEntity entity = new AutoIdEntity();
        entity.setName("auto_backfill");
        assertNull(entity.getId());

        DB.Pojo.insert(entity);
        assertNotNull("AUTO 类型 execute 后应回填生成的主键", entity.getId());
        assertTrue("回填的主键应大于 0", entity.getId() > 0);
    }

    @Test
    public void insertExecuteAssignIdBackfillTest() {
        Orders orders = new Orders();
        orders.setUserId("u001");
        orders.setAmount(100);
        assertNull(orders.getId());

        DB.Pojo.insert(orders);

        assertNotNull("ASSIGN_ID 类型 execute 后应预生成并回填主键", orders.getId());
    }

    @Test
    public void batchAssignIdBackfillTest() {
        Orders o1 = new Orders();
        o1.setUserId("batch_u1");
        o1.setAmount(10);

        Orders o2 = new Orders();
        o2.setUserId("batch_u2");
        o2.setAmount(20);

        assertNull(o1.getId());
        assertNull(o2.getId());

        DB.Batch.insert(Arrays.asList(o1, o2), 100);

        assertNotNull("batch 后 bean 应被回填 ASSIGN_ID", o1.getId());
        assertNotNull("batch 后 bean 应被回填 ASSIGN_ID", o2.getId());
        assertNotEquals("两个 bean 的 ASSIGN_ID 应不同", o1.getId(), o2.getId());
    }

    @Test
    public void batchAutoNotBackfillTest() {
        AutoIdEntity m1 = new AutoIdEntity();
        m1.setName("batch_auto1");

        AutoIdEntity m2 = new AutoIdEntity();
        m2.setName("batch_auto2");

        DB.Batch.insert(Arrays.asList(m1, m2), 100);

        assertNull("AUTO 类型 batch 后不应回填主键（驱动限制）", m1.getId());
    }

}