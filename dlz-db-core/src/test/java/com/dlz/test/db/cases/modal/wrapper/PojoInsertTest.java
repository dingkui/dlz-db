package com.dlz.test.db.cases.modal.wrapper;

import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.SysSql;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/**
 * PojoInsert SQL 生成测试（仅验证 SQL，不执行）
 */
@Slf4j
public class PojoInsertTest extends BaseDBTest {

    @Test
    public void insertWrapperTest1() {
        SysSql dict = new SysSql();
        dict.setId(123L);
        dict.setSqlKey("xxx");
        PojoInsert<SysSql> insert = new PojoInsert(SysSql.class).value(dict);
        assertEquals("INSERT INTO sys_sql(sql_key,deleted,id) VALUES('xxx',0,123)", toSql(insert));
    }

    @Test
    public void insertWrapperTest2_1() {
        SysSql dict = new SysSql();
        dict.setId(666L);
        dict.setSqlKey("xxx");
        dict.setDeleted(0);
        PojoInsert<SysSql> insert = new PojoInsert(SysSql.class).value(dict);
        assertEquals("INSERT INTO sys_sql(sql_key,deleted,id) VALUES('xxx',0,666)", toSql(insert));
    }

    @Test
    public void insertWrapperTest2() {
        SysSql dict = new SysSql();
        dict.setSqlKey("xxx");
        PojoInsert<SysSql> insert = new PojoInsert(SysSql.class).value(dict);
        showSql(insert, "insertWrapperTest2", "INSERT INTO sys_sql(SQL_KEY,deleted) VALUES('xxx',0)");
    }
}
