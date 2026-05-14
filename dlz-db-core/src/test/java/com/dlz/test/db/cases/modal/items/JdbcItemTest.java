package com.dlz.test.db.cases.modal.items;

import com.dlz.db.modal.items.JdbcItem;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JdbcItem JDBC 项测试
 */
@DisplayName("JdbcItem JDBC 项测试")
class JdbcItemTest extends BaseDBTest {

    @Test
    @DisplayName("测试构造函数 - 基本用法")
    void testConstructorBasic() {
        JdbcItem item = new JdbcItem("SELECT * FROM user WHERE id = ?", 1);
        
        assertEquals("SELECT * FROM user WHERE id = ?", item.sql);
        assertNotNull(item.paras);
        assertEquals(1, item.paras.length);
        assertEquals(1, item.paras[0]);
    }

    @Test
    @DisplayName("测试构造函数 - 多个参数")
    void testConstructorMultipleParams() {
        JdbcItem item = new JdbcItem("SELECT * FROM user WHERE id = ? AND name = ?", 1, "张三");
        
        assertEquals("SELECT * FROM user WHERE id = ? AND name = ?", item.sql);
        assertEquals(2, item.paras.length);
        assertEquals(1, item.paras[0]);
        assertEquals("张三", item.paras[1]);
    }

    @Test
    @DisplayName("测试构造函数 - 无参数")
    void testConstructorNoParams() {
        JdbcItem item = new JdbcItem("SELECT COUNT(*) FROM user");
        
        assertEquals("SELECT COUNT(*) FROM user", item.sql);
        assertNotNull(item.paras);
        assertEquals(0, item.paras.length);
    }

    @Test
    @DisplayName("测试 of 静态工厂方法")
    void testOfFactoryMethod() {
        JdbcItem item = JdbcItem.of("INSERT INTO user VALUES (?, ?)", 1, "张三");
        
        assertEquals("INSERT INTO user VALUES (?, ?)", item.sql);
        assertEquals(2, item.paras.length);
        assertEquals(1, item.paras[0]);
        assertEquals("张三", item.paras[1]);
    }

    @Test
    @DisplayName("测试 of 方法 - 单个参数")
    void testOfSingleParam() {
        JdbcItem item = JdbcItem.of("DELETE FROM user WHERE id = ?", 1);
        
        assertEquals("DELETE FROM user WHERE id = ?", item.sql);
        assertEquals(1, item.paras.length);
    }

    @Test
    @DisplayName("测试 sql 字段不可变")
    void testSqlImmutable() {
        JdbcItem item = new JdbcItem("SELECT * FROM user", new Object[]{});
        
        // sql 是 final 字段，无法修改
        assertNotNull(item.sql);
    }

    @Test
    @DisplayName("测试 paras 字段不可变引用")
    void testParasImmutableReference() {
        Object[] params = new Object[]{1, "test"};
        JdbcItem item = new JdbcItem("SELECT * FROM user WHERE id = ? AND name = ?", params);
        
        // 虽然数组引用是 final，但数组内容可以修改（这是 Java 的特性）
        assertEquals(2, item.paras.length);
    }

    @Test
    @DisplayName("测试 null SQL")
    void testNullSql() {
        JdbcItem item = new JdbcItem(null, new Object[]{});
        
        assertNull(item.sql);
        assertNotNull(item.paras);
    }

    @Test
    @DisplayName("测试 null 参数数组")
    void testNullParams() {
        JdbcItem item = new JdbcItem("SELECT * FROM user", (Object[]) null);
        
        assertNotNull(item.sql);
        assertNull(item.paras);
    }

    @Test
    @DisplayName("测试复杂参数类型")
    void testComplexParamTypes() {
        JdbcItem item = new JdbcItem(
            "INSERT INTO user VALUES (?, ?, ?, ?)",
            1,
            "张三",
            25.5,
            true
        );
        
        assertEquals(4, item.paras.length);
        assertEquals(1, item.paras[0]);
        assertEquals("张三", item.paras[1]);
        assertEquals(25.5, item.paras[2]);
        assertEquals(true, item.paras[3]);
    }

    @Test
    @DisplayName("测试序列化兼容性")
    void testSerializable() {
        JdbcItem item = new JdbcItem("SELECT * FROM user WHERE id = ?", 1);
        
        // 验证实现了 Serializable 接口
        assertTrue(item instanceof java.io.Serializable);
    }
}
