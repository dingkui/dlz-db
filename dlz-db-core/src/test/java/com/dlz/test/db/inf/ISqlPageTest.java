package com.dlz.test.db.inf;

import com.dlz.db.convertor.columnname.ColumnNameCamel;
import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.wrapper.JdbcQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ISqlPage 接口测试
 * 测试分页和排序相关方法
 *
 * @author dingkui
 */
@DisplayName("ISqlPage 分页排序测试")
class ISqlPageTest {

    private JdbcQuery query;

    @BeforeEach
    void setUp() {
        query = new JdbcQuery("SELECT * FROM user");
    }

    @Test
    @DisplayName("测试 limit() 方法 - 仅设置分页大小")
    void testLimit() {
        JdbcQuery result = query.limit(10);
        
        assertNotNull(result, "返回值不应为 null");
        assertSame(query, result, "应该返回同一个对象（链式调用）");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        assertEquals(10, page.getSize(), "页大小应为 10");
        assertEquals(1, page.getCurrent(), "当前页应为 1");
    }

    @Test
    @DisplayName("测试 page() 方法 - 设置分页参数")
    void testPage_WithParameters() {
        JdbcQuery result = query.page(2, 20);
        
        assertNotNull(result, "返回值不应为 null");
        assertSame(query, result, "应该返回同一个对象");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        assertEquals(20, page.getSize(), "页大小应为 20");
        assertEquals(2, page.getCurrent(), "当前页应为 2");
    }

    @Test
    @DisplayName("测试 page() 方法 - 带排序列表")
    void testPage_WithOrderList() {
        List<Order> orders = Arrays.asList(
                Order.asc("name"),
                Order.desc("create_time")
        );
        
        JdbcQuery result = query.page(1, 10, orders);
        
        assertNotNull(result, "返回值不应为 null");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        assertNotNull(page.getOrders(), "排序列表不应为 null");
        assertEquals(2, page.getOrders().size(), "应该有 2 个排序规则");
    }

    @Test
    @DisplayName("测试 page() 方法 - 可变参数排序")
    void testPage_WithVarargsOrders() {
        JdbcQuery result = query.page(1, 10, Order.asc("id"), Order.desc("name"));
        
        assertNotNull(result, "返回值不应为 null");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        assertEquals(2, page.getOrders().size(), "应该有 2 个排序规则");
    }

    @Test
    @DisplayName("测试 page() 方法 - 空排序列表")
    void testPage_WithEmptyOrders() {
        JdbcQuery result = query.page(1, 10, Collections.emptyList());
        
        assertNotNull(result, "返回值不应为 null");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        // 空列表不应该影响分页设置
        assertEquals(10, page.getSize(), "页大小应为 10");
    }

    @Test
    @DisplayName("测试 page() 方法 - null 排序列表")
    void testPage_WithNullOrders() {
        JdbcQuery result = query.page(1, 10, (List<Order>) null);
        
        assertNotNull(result, "返回值不应为 null");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        // null 列表不应该影响分页设置
        assertEquals(10, page.getSize(), "页大小应为 10");
    }

    @Test
    @DisplayName("测试 page() 方法 - 页号 <= 0 不修改")
    void testPage_CurrentLessThanOrEqualToZero() {
        // 先设置页号为 5
        query.page(5, 10);
        
        // 再调用 page，current=0 应该不修改页号
        query.page(0, 20);
        
        Page page = query.getPage();
        assertEquals(5, page.getCurrent(), "页号应保持为 5");
        assertEquals(20, page.getSize(), "页大小应更新为 20");
    }

    @Test
    @DisplayName("测试 page() 方法 - 页大小 <= 0 不修改")
    void testPage_SizeLessThanOrEqualToZero() {
        // 先设置页大小为 10
        query.page(1, 10);
        
        // 再调用 page，size=0 应该不修改页大小
        query.page(2, 0);
        
        Page page = query.getPage();
        assertEquals(2, page.getCurrent(), "页号应更新为 2");
        assertEquals(10, page.getSize(), "页大小应保持为 10");
    }

    @Test
    @DisplayName("测试 orderByAsc() 方法")
    void testOrderByAsc() {
        JdbcQuery result = query.orderByAsc("name", "age");
        
        assertNotNull(result, "返回值不应为 null");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        assertEquals(2, page.getOrders().size(), "应该有 2 个升序排序");
        
        // 验证都是升序
        assertTrue(page.getOrders().stream()
                .allMatch(order -> ((Order) order).isAsc()),
                "所有排序应该是升序");
    }

    @Test
    @DisplayName("测试 orderByDesc() 方法")
    void testOrderByDesc() {
        JdbcQuery result = query.orderByDesc("create_time", "update_time");
        
        assertNotNull(result, "返回值不应为 null");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        assertEquals(2, page.getOrders().size(), "应该有 2 个降序排序");
        
        // 验证都是降序
        assertTrue(page.getOrders().stream()
                .allMatch(order -> !((Order) order).isAsc()),
                "所有排序应该是降序");
    }

    @Test
    @DisplayName("测试 sort() 方法 - 追加排序")
    void testSort_AppendOrders() {
        // 先添加一个排序
        query.orderByAsc("name");
        
        // 再追加排序
        query.sort(Order.desc("age"));
        
        Page page = query.getPage();
        assertEquals(2, page.getOrders().size(), "应该有 2 个排序规则");
    }

    @Test
    @DisplayName("测试多次调用 page() - 合并到同一 Page")
    void testMultiplePageCalls_MergeToSamePage() {
        // 第一次调用
        query.page(1, 10).orderByAsc("name");
        
        Page firstPage = query.getPage();
        
        // 第二次调用
        query.page(2, 20).orderByDesc("age");
        
        Page secondPage = query.getPage();
        
        // 应该是同一个 Page 对象
        assertSame(firstPage, secondPage, "多次调用应该使用同一个 Page 对象");
        assertEquals(2, secondPage.getCurrent(), "页号应更新为 2");
        assertEquals(20, secondPage.getSize(), "页大小应更新为 20");
        assertEquals(2, secondPage.getOrders().size(), "应该有 2 个排序规则");
    }

    @Test
    @DisplayName("测试 convert() 方法配合分页")
    void testConvert_WithPagination() {
        // 这个方法主要测试 convert 不会影响分页设置
        JdbcQuery result = query
                .page(1, 10)
                .convert(new ColumnNameCamel());
        
        assertNotNull(result, "返回值不应为 null");
        
        Page page = query.getPage();
        assertNotNull(page, "Page 对象不应为 null");
        assertEquals(10, page.getSize(), "页大小应保持为 10");
    }

    @Test
    @DisplayName("测试链式调用 - 完整场景")
    void testChainedCalls_FullScenario() {
        JdbcQuery result = query
                .page(1, 20)
                .orderByAsc("name")
                .orderByDesc("create_time")
                .limit(10);  // limit 会覆盖之前的分页
        
        assertNotNull(result, "返回值不应为 null");
        
        Page page = query.getPage();
        assertEquals(10, page.getSize(), "页大小应为 10（limit 覆盖）");
        assertEquals(1, page.getCurrent(), "当前页应为 1");
        assertEquals(2, page.getOrders().size(), "应该有 2 个排序规则");
    }
}
