package com.dlz.test.db.cases.modal.wrapper;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.SysSql;
import org.junit.jupiter.api.Test;

public class PojoDeleteWTest extends BaseDBTest {

    @Test
    public void deleterapperTest1() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        PojoDelete<SysSql> delete = DB.Pojo.delete(SysSql.class).eq(SysSql::getId, 123);
        showSql(delete,"deleterapperTest1","delete from SYS_SQL where ID = 123 and DELETED = 0");
    }
    //未输入条件删除条件为false
    @Test
    public void deleterapperTest2() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        PojoDelete<SysSql> delete = DB.Pojo.delete(SysSql.class);
        showSql(delete,"deleterapperTest2","delete from SYS_SQL where DELETED = 0");
    }

}