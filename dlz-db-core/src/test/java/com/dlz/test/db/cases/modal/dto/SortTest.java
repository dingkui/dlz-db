package com.dlz.test.db.cases.modal.dto;

import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Sort;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sort 排序对象测试")
class SortTest extends BaseDBTest {

    @Test
    @DisplayName("默认构造 - 空排序列表")
    void testDefaultConstructor() {
        Sort<Sort> sort = new Sort<>();
        assertNotNull(sort.getOrders());
        assertTrue(sort.getOrders().isEmpty());
    }

    @Test
    @DisplayName("varargs 构造 - 传入多个Order")
    void testVarargsConstructor() {
        Sort<Sort> sort = new Sort<>(Order.asc("name"), Order.desc("age"));
        assertEquals(2, sort.getOrders().size());
    }

    @Test
    @DisplayName("List 构造 - 传入Order列表")
    void testListConstructor() {
        List<Order> orders = Arrays.asList(Order.asc("id"), Order.desc("time"));
        Sort<Sort> sort = new Sort<>(orders);
        assertEquals(2, sort.getOrders().size());
    }

    @Test
    @DisplayName("getSortSql - 无排序返回null")
    void testGetSortSqlEmpty() {
        Sort<Sort> sort = new Sort<>();
        assertNull(sort.getSortSql());
    }

    @Test
    @DisplayName("getSortSql - 单字段ASC")
    void testGetSortSqlSingleAsc() {
        Sort<Sort> sort = new Sort<>(Order.asc("name"));
        String sql = sort.getSortSql();
        assertNotNull(sql);
        assertTrue(sql.contains("order by"));
        assertTrue(sql.contains("asc"));
    }

    @Test
    @DisplayName("getSortSql - 单字段DESC")
    void testGetSortSqlSingleDesc() {
        Sort<Sort> sort = new Sort<>(Order.desc("age"));
        String sql = sort.getSortSql();
        assertNotNull(sql);
        assertTrue(sql.contains("desc"));
    }

    @Test
    @DisplayName("getSortSql - 多字段排序")
    void testGetSortSqlMultiple() {
        Sort<Sort> sort = new Sort<>(Order.asc("name"), Order.desc("age"));
        String sql = sort.getSortSql();
        assertNotNull(sql);
        assertTrue(sql.contains(","));
    }

    @Test
    @DisplayName("addOrder - 添加排序项")
    void testAddOrder() {
        Sort<Sort> sort = new Sort<>();
        sort.addOrder(Order.asc("name"));
        assertEquals(1, sort.getOrders().size());
    }

    @Test
    @DisplayName("addOrder - 忽略column为null的Order")
    void testAddOrderNullColumn() {
        Sort<Sort> sort = new Sort<>();
        sort.addOrder(new Order(null, true));
        assertTrue(sort.getOrders().isEmpty());
    }

    @Test
    @DisplayName("addOrder - List形式")
    void testAddOrderList() {
        Sort<Sort> sort = new Sort<>();
        sort.addOrder(Arrays.asList(Order.asc("a"), Order.desc("b")));
        assertEquals(2, sort.getOrders().size());
    }

    @Test
    @DisplayName("removeOrder - 移除匹配的排序")
    void testRemoveOrder() {
        Sort<Sort> sort = new Sort<>(Order.asc("name"), Order.desc("age"), Order.asc("id"));
        sort.removeOrder(o -> "age".equals(o.getColumn()));
        assertEquals(2, sort.getOrders().size());
        assertTrue(sort.getOrders().stream().noneMatch(o -> "age".equals(o.getColumn())));
    }

    @Test
    @DisplayName("me - 返回自身引用（链式调用）")
    void testMe() {
        Sort<Sort> sort = new Sort<>();
        assertSame(sort, sort.me());
    }
}
