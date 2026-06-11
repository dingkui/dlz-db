package com.dlz.test.db.cases.modal.dto;

import com.dlz.db.modal.dto.Order;
import com.dlz.db.modal.dto.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Page 分页对象测试")
class PageTest {

    @Test
    @DisplayName("默认构造 - 默认页码和大小")
    void testDefaultConstructor() {
        Page<String> page = new Page<>();
        assertEquals(0, page.getCurrent());
        assertEquals(20, page.getSize());
        assertEquals(0, page.getTotal());
        assertEquals(0, page.getPages());
    }

    @Test
    @DisplayName("指定页码和大小")
    void testConstructorWithCurrentAndSize() {
        Page<String> page = new Page<>(2, 10);
        assertEquals(2, page.getCurrent());
        assertEquals(10, page.getSize());
    }

    @Test
    @DisplayName("setSize 超过5000限制为5000")
    void testSetSizeMax() {
        Page<String> page = new Page<>();
        page.setSize(10000);
        assertEquals(5000, page.getSize());
    }

    @Test
    @DisplayName("setTotal 更新总页数")
    void testSetTotalUpdatesPages() {
        Page<String> page = new Page<>(1, 10);
        page.setTotal(25);
        assertEquals(25, page.getTotal());
        assertEquals(3, page.getPages());
    }

    @Test
    @DisplayName("总页数 - 整除")
    void testPagesExactDivision() {
        Page<String> page = new Page<>(1, 10);
        page.setTotal(30);
        assertEquals(3, page.getPages());
    }

    @Test
    @DisplayName("总页数 - 非整除")
    void testPagesRemainder() {
        Page<String> page = new Page<>(1, 10);
        page.setTotal(31);
        assertEquals(4, page.getPages());
    }

    @Test
    @DisplayName("current 超过 pages 时调整到最后一页")
    void testCurrentExceedsPages() {
        Page<String> page = new Page<>(100, 10);
        page.setTotal(25);
        assertEquals(3, page.getCurrent());
    }

    @Test
    @DisplayName("build 静态工厂方法")
    void testBuildFactory() {
        Page<String> page = Page.build(1, 15);
        assertEquals(1, page.getCurrent());
        assertEquals(15, page.getSize());
    }

    @Test
    @DisplayName("build 带排序的工厂方法")
    void testBuildFactoryWithOrder() {
        Page<String> page = Page.build(1, 10, Order.asc("name"));
        assertEquals(1, page.getCurrent());
        assertEquals(1, page.getOrders().size());
    }

    @Test
    @DisplayName("doPage - total=0时返回空列表")
    void testDoPageZeroTotal() {
        Page<String> page = new Page<>(1, 10);
        page.doPage(() -> 0L, () -> Arrays.asList("a", "b"));
        assertEquals(0, page.getTotal());
        assertNotNull(page.getRecords());
        assertTrue(page.getRecords().isEmpty());
    }

    @Test
    @DisplayName("doPage - total>0时查询数据")
    void testDoPageWithData() {
        Page<String> page = new Page<>(1, 10);
        page.doPage(() -> 5L, () -> Arrays.asList("a", "b", "c", "d", "e"));
        assertEquals(5, page.getTotal());
        assertEquals(5, page.getRecords().size());
    }

    @Test
    @DisplayName("doPage - current<=0时设为1")
    void testDoPageCurrentZero() {
        Page<String> page = new Page<>(0, 10);
        page.doPage(() -> 5L, () -> Arrays.asList("a"));
        assertEquals(1, page.getCurrent());
    }

    @Test
    @DisplayName("cover - 转换记录类型")
    void testCover() {
        Page<Integer> page = new Page<>(1, 10);
        page.setTotal(3);
        page.setRecords(Arrays.asList(1, 2, 3));

        Page<String> converted = page.cover(String::valueOf);
        assertEquals(3, converted.getRecords().size());
        assertEquals("1", converted.getRecords().get(0));
        assertEquals(3, converted.getTotal());
    }

    @Test
    @DisplayName("setSize - 正常大小")
    void testSetSizeNormal() {
        Page<String> page = new Page<>(1, 50);
        assertEquals(50, page.getSize());
    }

    @Test
    @DisplayName("build(Order...) - 无参数创建")
    void testBuildNoArgs() {
        Page<String> page = Page.build();
        assertNotNull(page);
        assertEquals(0, page.getCurrent());
    }
}
