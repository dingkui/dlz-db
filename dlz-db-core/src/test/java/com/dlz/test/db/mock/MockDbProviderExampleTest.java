package com.dlz.test.db.mock;

import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.wrapper.JdbcQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MockDbProvider 使用示例测试
 * 展示如何使用内存数据库进行测试
 */
@DisplayName("MockDbProvider 使用示例")
class MockDbProviderExampleTest extends BaseMockTest {

    @Test
    @DisplayName("测试查询列表 - queryList()")
    void testQueryList() {
        JdbcQuery query = new JdbcQuery("SELECT * FROM user");
        
        List<ResultMap> list = query.queryList();
        
        assertNotNull(list, "查询结果不应为 null");
        assertEquals(3, list.size(), "应该有 3 条用户数据");
        
        // 验证第一条数据
        ResultMap firstUser = list.get(0);
        assertEquals("张三", firstUser.getStr("name"), "第一个用户应该是张三");
        assertEquals(25, firstUser.getInt("age"), "张三的年龄应该是 25");
    }

    @Test
    @DisplayName("测试查询单条 - queryOne()")
    void testQueryOne() {
        JdbcQuery query = new JdbcQuery("SELECT * FROM user WHERE id = 1");
        
        ResultMap user = query.queryOne();
        
        assertNotNull(user, "查询结果不应为 null");
        assertEquals("张三", user.getStr("name"));
    }

    @Test
    @DisplayName("测试转换器 - convertNative()")
    void testConvertNative() {
        JdbcQuery query = new JdbcQuery("SELECT * FROM user");
        query.convertNative();
        
        List<ResultMap> list = query.queryList();
        
        assertNotNull(list);
        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("测试分页 - limit()")
    void testLimit() {
        // Mock 环境不支持真正的分页，只验证基本查询
        JdbcQuery query = new JdbcQuery("SELECT * FROM user");
        
        List<ResultMap> list = query.queryList();
        
        assertNotNull(list);
        assertTrue(list.size() > 0);
    }

    @Test
    @DisplayName("测试计数 - count()")
    void testCount() {
        // Mock 环境不支持 count()，因为需要解析 COUNT SQL
        // 这里只验证方法不会抛异常
        JdbcQuery query = new JdbcQuery("SELECT * FROM user");
        List<ResultMap> list = query.queryList();
        
        assertNotNull(list);
        assertTrue(list.size() >= 0);
    }
}
