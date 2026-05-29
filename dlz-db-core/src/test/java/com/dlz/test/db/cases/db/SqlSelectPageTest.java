package com.dlz.test.db.cases.db;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.Test;

public class SqlSelectPageTest extends BaseDBTest {
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
