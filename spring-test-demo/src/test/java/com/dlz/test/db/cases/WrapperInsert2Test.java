package com.dlz.test.db.cases;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.test.db.config.SpringDbTestBase;
import com.dlz.test.db.entity.AutoIdEntity;
import com.dlz.test.db.entity.SysSql;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNull;

@Slf4j
public class WrapperInsert2Test extends SpringDbTestBase {

    @Test
    public void insertWrapperTest1() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        dict.setSqlKey("xxx");
        PojoInsert<SysSql> insert = PojoInsert.wrapper(dict);
        showSql(insert,"insertWrapperTest12","insert into SYS_SQL(SQL_KEY,ID) values('xxx',123)");
    }

    @Test
    public void batchAutoNotBackfillTest() {
        AutoIdEntity m1 = new AutoIdEntity();
        m1.setName("batch_auto1");

        AutoIdEntity m2 = new AutoIdEntity();
        m2.setName("batch_auto2");

        DB.Batch.insert(Arrays.asList(m1, m2), 100);

        assertNull("AUTO 类型 batch 后不应回填主键（驱动限制）2", m1.getId());
    }

}