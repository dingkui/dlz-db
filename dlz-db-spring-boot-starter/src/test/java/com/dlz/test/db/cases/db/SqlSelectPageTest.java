package com.dlz.test.db.cases.db;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.test.db.config.SpingDbBaseTest;
import org.junit.Test;

public class SqlSelectPageTest extends SpingDbBaseTest {
    @Test
    public void PageTest() {
        DB.Sql.select("select t.* from Goods t where t.goods_id=310")
                .page(Page.build(1, 2, Order.asc("id")))
                .queryPage();
    }

    @Test
    public void PageTest2() {
        DB.Sql.select("select t.* from GOODS_PRICE t where t.goods_id=#{goodsId}")
                .page(Page.build(1, 2, Order.asc("id"), Order.desc("xx2")))
                .addPara("goodsId", 123).queryOne();
    }
}
