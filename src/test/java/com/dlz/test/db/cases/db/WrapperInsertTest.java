package com.dlz.test.db.cases.db;

import com.dlz.db.convertor.clumnname.ColumnNameToLower;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.SysSql;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

@Slf4j
public class WrapperInsertTest extends SpingDbBaseTest {

    @Test
    public void insertWrapperTest1() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        dict.setSqlKey("xxx");
        PojoInsert<SysSql> insert = DB.Pojo.insert(dict);
        showSql(insert,"insertWrapperTest1","insert into SYS_SQL(SQL_KEY,ID) values('xxx',123)");
    }

    @Test
    public void insertWrapperTest2_1() {
        SysSql dict = new SysSql();
        dict.setId(666L);
        dict.setSqlKey("xxx");
        dict.setIsDeleted(0);
        DB.Pojo.delete(SysSql.class).eq(SysSql::getId, 666L).execute();
        DB.Pojo.delete(SysSql.class).eq(SysSql::getId, 666L).setLogicDelete(false).execute();
        PojoInsert<SysSql> insert = DB.Pojo.insert(dict);
        showSql(insert,"insertWrapperTest1","insert into Sys_Sql(IS_DELETED,SQL_KEY,ID) values(0,'xxx',666) ");
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
        PojoInsert<SysSql> insert = DB.Pojo.insert(dict);
        showSql(insert,"insertWrapperTest2","insert into SYS_SQL(SQL_KEY) values('xxx')");
        Long aLong = insert.insertWithAutoKey();
        System.out.println(aLong);
    }

}