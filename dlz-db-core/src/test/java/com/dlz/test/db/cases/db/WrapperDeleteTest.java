package com.dlz.test.db.cases.db;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.SysSql;
import org.junit.jupiter.api.Test;

public class WrapperDeleteTest extends BaseDBTest {

    @Test
    public void deleteWrapperTest1() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        PojoDelete<SysSql> delete = DB.Pojo.delete(SysSql.class).eq(SysSql::getId, 123);
        showSql(delete,"deleteWrapperTest1","delete from SYS_SQL where ID = 123 and DELETED = 0");
    }
    //未输入条件删除条件为false
    @Test
    public void deleteWrapperTest2() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        PojoDelete<SysSql> delete = DB.Pojo.delete(SysSql.class);
        showSql(delete,"deleteWrapperTest2","delete from SYS_SQL where DELETED = 0");
    }

}