package com.dlz.test.db.cases.modal;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.wrapper.SqlQuery;
import com.dlz.test.db.config.BaseDBTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DBSqlSelectTest extends BaseDBTest {
    @Test
    public void sqlSelectTest1() {
        String sql = "key.sqlTest.sqlUtil";
        SqlQuery ump2 = DB.sql.select(sql);
        ump2.addPara("a", "a1");
        ump2.addPara("b", "b1");
        ump2.addPara("d", "d1");
        ump2.addPara("c", "c1");
        ump2.addPara("_sql", "_sql${a}");
        ump2.setPage(Page.build(1, 2, Order.asc("id")));

        showSql(ump2, "sqlSelectTest1", "SELECT * FROM bb WHERE 1=1 AND a='a1' AND b='b1' AND c=2 AND d=d1 AND d=ddd ^d1 AND d='d1' AND d1='null' AND d2='null' AND c='c1' order by id asc LIMIT 0,2");
    }

    @Test
    public void sqlSelectTest2() {
        String sql = "key.test";
        SqlQuery ump2 = DB.sql.select(sql);
        ump2.addPara("a", "a1");
        ump2.addPara("b", "b1");
        ump2.addPara("d", "d1");
        ump2.addPara("c", "c1");
        ump2.addPara("_sql", "_sql${a}");
        ump2.setPage(Page.build(1, 2, Order.asc("id")));
        showSql(ump2, "sqlSelectTest2", "SELECT * FROM dual xxx ORDER BY id ASC LIMIT 0,2");
    }

    @Test
    public void sqlSelectTest3(){
        final SqlQuery sqlQuery = DB.sql.select("SELECT t.* FROM PTN t WHERE t.id=${key.comm.pageSql} AND t.cc=${a} AND c=${b} AND ccc")
                .addPara("a", "a${b}")
                .addPara("b", "b${c}")
                .addPara("_sql", "_sql${a}");
        showSql(sqlQuery, "sqlSelectTest3", "SELECT t.* FROM PTN t WHERE t.id= _sqlab AND t.cc=ab AND c=b AND ccc");
    }
}