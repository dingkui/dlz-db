package com.dlz.test.db.cases.support;

import com.dlz.db.annotation.IdType;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IdInfo 主键信息测试")
class IdInfoTest extends BaseDBTest {

    static class TestEntity {
        public Long id;
        public String name;
        public Integer intId;
    }

    @Test
    @DisplayName("构造函数 - 正确保存field和name")
    void testConstructor() throws Exception {
        Field field = TestEntity.class.getDeclaredField("id");
        IdInfo info = new IdInfo(field, "id");
        assertEquals("id", info.getDbName());
        assertSame(field, info.getField());
    }

    @Test
    @DisplayName("setId - 设置Long类型ID")
    void testSetIdLong() throws Exception {
        Field field = TestEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        IdInfo info = new IdInfo(field, "id");

        TestEntity entity = new TestEntity();
        info.setId(entity, 123L);
        assertEquals(123L, entity.id);
    }

    @Test
    @DisplayName("setId - 字符串转Long")
    void testSetIdStringToLong() throws Exception {
        Field field = TestEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        IdInfo info = new IdInfo(field, "id");

        TestEntity entity = new TestEntity();
        info.setId(entity, "456");
        assertEquals(456L, entity.id);
    }

    @Test
    @DisplayName("getValue - 获取字段值")
    void testGetValue() throws Exception {
        Field field = TestEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        IdInfo info = new IdInfo(field, "id");

        TestEntity entity = new TestEntity();
        entity.id = 789L;
        assertEquals(789L, info.getValue(entity));
    }

    @Test
    @DisplayName("getValue - 字段为null返回null")
    void testGetValueNull() throws Exception {
        Field field = TestEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        IdInfo info = new IdInfo(field, "id");

        TestEntity entity = new TestEntity();
        assertNull(info.getValue(entity));
    }

    @Test
    @DisplayName("type setter/getter")
    void testTypeSetterGetter() throws Exception {
        Field field = TestEntity.class.getDeclaredField("id");
        IdInfo info = new IdInfo(field, "id");
        assertNull(info.getType());
        info.setType(IdType.AUTO);
        assertEquals(IdType.AUTO, info.getType());
    }

    @Test
    @DisplayName("name setter/getter")
    void testNameSetterGetter() throws Exception {
        Field field = TestEntity.class.getDeclaredField("id");
        IdInfo info = new IdInfo(field, "id");
        info.setDbName("user_id");
        assertEquals("user_id", info.getDbName());
    }
}
