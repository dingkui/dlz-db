package com.dlz.test.db.cases.db;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.wrapper.JdbcQuery;
import com.dlz.test.db.config.SpingDbBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class JdbcTest extends SpingDbBaseTest {
    @Test
    public void jdbcPageTest1() {
        final JdbcQuery page = DB.Jdbc.select("select 1 from dual where ?=1", 1)
                .page(Page.build(1, 2));
        showSql(page, "jdbcPageTest1", "select 1 from dual where 1=1 LIMIT 0,2");
    }

    @Test
    public void jdbcPageTest2() {
        final JdbcQuery page = DB.Jdbc.select("select 1 from dual where ?=1", 1)
                .page(Page.build(1, 2, Order.descs("x1", "x2")));
        showSql(page, "jdbcPageTest2", "select 1 from dual where 1=1 order by X1 desc,X2 desc LIMIT 0,2");
    }

    @Test
    public void jdbcPageTest3() {
        final JdbcQuery page = DB.Jdbc.select("select 1 from dual where ?=1", 1)
                .page(1, 20, Order.descs("x1", "x2"))
                .page(Page.build(Order.descs("x1", "x2")));
        showSql(page, "jdbcPageTest3", "select 1 from dual where 1=1 order by X1 desc,X2 desc");
    }
}