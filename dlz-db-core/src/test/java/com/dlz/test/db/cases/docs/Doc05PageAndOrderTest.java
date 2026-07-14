package com.dlz.test.db.cases.docs;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import org.junit.jupiter.api.Test;

public class Doc05PageAndOrderTest extends BaseDBTest {
    @Test
    public void pageAndOrderTest5_1_1() {
        // 基础分页
        Page page = Page.build(1, 10);  // 第1页，每页10条

        // 带排序的分页
        Page page2 = Page.build(1, 10, Order.desc("create_time"));

        // 多字段排序
        Page page3 = Page.build(1, 10, Order.desc("create_time"), Order.asc("id"));

        // 只排序，无分页
        Page page4 = Page.build(Order.desc("create_time"), Order.asc("id"));

        // 分页结果
        page.getRecords();    // List<User> 当前页数据
        page.getTotal();      // long 总条数
        page.getPages();     // int 总页数
        page.getCurrent();    // int 当前页码
        page.getSize();       // int 每页条数
        // 链式设置排序字段
        page.addOrder(Order.desc("createTime"), Order.asc("id"));

        //非常用方法
        page.getSortSql();       // 取得排序SQL
        page.getOrders() ;      // List<Order> 排序字段
    }

    @Test
    public void pageAndOrderTest5_1_2() {
        // 分页构造方式1：page单独构建
        final Page<?> page = Page.build(1, 10, Order.desc("create_time"));
        DB.pojo.selectWrapper(User.class)
                .eq(User::getStatus, 1)
                .page(page)
                .queryBeanPage();

        // 分页构造方式2：分页和排序链式构建
        DB.pojo.selectWrapper(User.class)
                .eq(User::getStatus, 1)
                .page(1,10)
                .orderByAsc(User::getId)
                .queryBeanPage();
    }

    @Test
    public void pageAndOrderTest5_1_3() {
       DB.jdbc.selectWrapper("SELECT * FROM user WHERE status = ?", 1)
               .page(Page.build(1, 10, Order.desc("id")))
               .queryPage();

        // 生成 SQL：
        // select COUNT(*) FROM user WHERE status = 1 （自动生成count语句）
        // SELECT * FROM user WHERE status = 1 LIMIT 0, 10 （COUNT>0 时才执行）
    }

    @Test
    public void pageAndOrderTest5_1_4() {
        //方法1：预设模版
        /**
         <sql sqlId="key.pageAndOrderTest5_1_4"><![CDATA[
         SELECT * FROM user WHERE AND status = #{status}
         ]]></sql>
         */
        DB.sql.select("key.pageAndOrderTest5_1_4")
                .addPara("status", 1)
                .page(Page.build(1, 10, Order.desc("id")))
                .queryPage();

        //方法2：直接写SQL
        DB.sql.select("SELECT * FROM user WHERE status = #{status}")
                .addPara("status", 1)
                .page(Page.build(1, 10, Order.desc("id")))
                .queryPage();

        // 生成 SQL：
        // select COUNT(*) FROM user WHERE status = 1 （自动生成count语句）
        // SELECT * FROM user WHERE status = 1 order by id desc LIMIT 0,10  （COUNT>0 时才执行）
    }

    @Test
    public void pageAndOrderTest5_1_5() {
        // 只需要排序，不需要分页
        //方法1：设置只有排序的分页
        DB.pojo.selectWrapper(User.class)
            .page(Page.build(Order.descs("create_time", "id")))  // 不传页码
            .queryList();
        //方法2：直接链式设置order
        DB.pojo.selectWrapper(User.class)
            .orderByDesc("create_time", "id")  // 不传页码
            .queryList();

        // 生成 SQL：
        // select * FROM USER t WHERE deleted = 0 order by create_time desc,id desc
        // （无 LIMIT）
    }

    @Test
    public void pageAndOrderTest5_2_1() {
        // 单字段升序
        Order.asc("create_time");

        // 单字段降序
        Order.desc("create_time");

        // 多字段升序
        Order.ascs("status", "create_time");

        // 多字段降序
        Order.descs("create_time", "id");

        // page 构造
        Page.build(1, 10, Order.desc("create_time"), Order.asc("id"));
    }

}
