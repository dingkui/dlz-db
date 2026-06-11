package com.dlz.test.db.cases.modal.dto;

import com.dlz.db.modal.dto.Order;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order 排序元素测试")
class OrderTest extends BaseDBTest {

    @Test
    @DisplayName("build(column, boolean) - ASC")
    void testBuildAsc() {
        Order order = Order.build("name", true);
        assertEquals("name", order.getColumn());
        assertTrue(order.isAsc());
    }

    @Test
    @DisplayName("build(column, boolean) - DESC")
    void testBuildDesc() {
        Order order = Order.build("age", false);
        assertEquals("age", order.getColumn());
        assertFalse(order.isAsc());
    }

    @Test
    @DisplayName("build(column, String) - asc字符串")
    void testBuildStringAsc() {
        Order order = Order.build("name", "asc");
        assertTrue(order.isAsc());
    }

    @Test
    @DisplayName("build(column, String) - desc字符串")
    void testBuildStringDesc() {
        Order order = Order.build("name", "desc");
        assertFalse(order.isAsc());
    }

    @Test
    @DisplayName("build(column, String) - 大小写不敏感")
    void testBuildStringCaseInsensitive() {
        assertTrue(Order.build("name", "ASC").isAsc());
        assertTrue(Order.build("name", "Asc").isAsc());
    }

    @Test
    @DisplayName("buildWithSql - 解析SQL排序片段")
    void testBuildWithSql() {
        Order order = Order.buildWithSql("name asc");
        assertEquals("name", order.getColumn());
        assertTrue(order.isAsc());
    }

    @Test
    @DisplayName("buildWithSql - DESC排序片段")
    void testBuildWithSqlDesc() {
        Order order = Order.buildWithSql("age desc");
        assertEquals("age", order.getColumn());
        assertFalse(order.isAsc());
    }

    @Test
    @DisplayName("asc 静态方法")
    void testAscStatic() {
        Order order = Order.asc("id");
        assertEquals("id", order.getColumn());
        assertTrue(order.isAsc());
    }

    @Test
    @DisplayName("desc 静态方法")
    void testDescStatic() {
        Order order = Order.desc("id");
        assertEquals("id", order.getColumn());
        assertFalse(order.isAsc());
    }

    @Test
    @DisplayName("ascs - 多字段ASC排序数组")
    void testAscsMultiple() {
        Order[] orders = Order.ascs("name", "age", "id");
        assertEquals(3, orders.length);
        for (Order o : orders) {
            assertTrue(o.isAsc());
        }
        assertEquals("name", orders[0].getColumn());
        assertEquals("age", orders[1].getColumn());
        assertEquals("id", orders[2].getColumn());
    }

    @Test
    @DisplayName("descs - 多字段DESC排序数组")
    void testDescsMultiple() {
        Order[] orders = Order.descs("name", "age");
        assertEquals(2, orders.length);
        for (Order o : orders) {
            assertFalse(o.isAsc());
        }
    }

    @Test
    @DisplayName("buildWithSqls - 解析多排序字符串")
    void testBuildWithSqls() {
        Order[] orders = Order.buildWithSqls("name asc,age desc");
        assertEquals(2, orders.length);
        assertEquals("name", orders[0].getColumn());
        assertTrue(orders[0].isAsc());
        assertEquals("age", orders[1].getColumn());
        assertFalse(orders[1].isAsc());
    }

    @Test
    @DisplayName("默认构造 - asc默认true")
    void testDefaultConstructor() {
        Order order = new Order();
        assertTrue(order.isAsc());
        assertNull(order.getColumn());
    }

    @Test
    @DisplayName("全参构造")
    void testAllArgsConstructor() {
        Order order = new Order("field", false);
        assertEquals("field", order.getColumn());
        assertFalse(order.isAsc());
    }
}
