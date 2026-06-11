package com.dlz.test.db.cases.ds;

import com.dlz.db.ds.DataSourceProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataSourceProperty 数据源属性测试")
class DataSourcePropertyTest {

    @Test
    @DisplayName("默认连接池配置")
    void testDefaultPoolConfig() {
        DataSourceProperty prop = new DataSourceProperty();
        assertEquals(10, prop.getMaxPoolSize());
        assertEquals(2, prop.getMinIdle());
        assertEquals(30000, prop.getConnectionTimeout());
        assertEquals(3000, prop.getValidationTimeout());
        assertEquals(600000, prop.getIdleTimeout());
        assertEquals(1800000, prop.getMaxLifetime());
        assertEquals(60000, prop.getLeakDetectionThreshold());
    }

    @Test
    @DisplayName("额外配置默认为空Map")
    void testDefaultAdditionalProperties() {
        DataSourceProperty prop = new DataSourceProperty();
        assertNotNull(prop.getAdditionalProperties());
        assertTrue(prop.getAdditionalProperties().isEmpty());
    }

    @Test
    @DisplayName("健康检查配置默认为空Map")
    void testDefaultHealthCheckRegistry() {
        DataSourceProperty prop = new DataSourceProperty();
        assertNotNull(prop.getHealthCheckRegistry());
        assertTrue(prop.getHealthCheckRegistry().isEmpty());
    }

    @Test
    @DisplayName("setter/getter - 基本属性")
    void testBasicProperties() {
        DataSourceProperty prop = new DataSourceProperty();
        prop.setName("primary");
        prop.setDriverClassName("org.sqlite.JDBC");
        prop.setUrl("jdbc:sqlite::memory:");
        prop.setUsername("user");
        prop.setPassword("pass");
        prop.setTestQuery("SELECT 1");
        prop.setSchema("public");

        assertEquals("primary", prop.getName());
        assertEquals("org.sqlite.JDBC", prop.getDriverClassName());
        assertEquals("jdbc:sqlite::memory:", prop.getUrl());
        assertEquals("user", prop.getUsername());
        assertEquals("pass", prop.getPassword());
        assertEquals("SELECT 1", prop.getTestQuery());
        assertEquals("public", prop.getSchema());
    }

    @Test
    @DisplayName("setter/getter - 连接池属性")
    void testPoolProperties() {
        DataSourceProperty prop = new DataSourceProperty();
        prop.setMaxPoolSize(20);
        prop.setMinIdle(5);
        prop.setConnectionTimeout(60000);
        prop.setIdleTimeout(300000);
        prop.setMaxLifetime(900000);
        prop.setLeakDetectionThreshold(120000);
        prop.setValidationTimeout(5000);

        assertEquals(20, prop.getMaxPoolSize());
        assertEquals(5, prop.getMinIdle());
        assertEquals(60000, prop.getConnectionTimeout());
        assertEquals(300000, prop.getIdleTimeout());
        assertEquals(900000, prop.getMaxLifetime());
        assertEquals(120000, prop.getLeakDetectionThreshold());
        assertEquals(5000, prop.getValidationTimeout());
    }

    @Test
    @DisplayName("setter/getter - 额外配置和健康检查")
    void testExtraProperties() {
        DataSourceProperty prop = new DataSourceProperty();
        Map<String, Object> additional = new HashMap<>();
        additional.put("key", "value");
        prop.setAdditionalProperties(additional);
        assertEquals("value", prop.getAdditionalProperties().get("key"));

        Map<String, String> health = new HashMap<>();
        health.put("check", "true");
        prop.setHealthCheckRegistry(health);
        assertEquals("true", prop.getHealthCheckRegistry().get("check"));
    }

    @Test
    @DisplayName("creatorClassName 默认为null")
    void testCreatorClassNameDefault() {
        DataSourceProperty prop = new DataSourceProperty();
        assertNull(prop.getCreatorClassName());
    }

    @Test
    @DisplayName("dbProductName setter/getter")
    void testDbProductName() {
        DataSourceProperty prop = new DataSourceProperty();
        prop.setDbProductName("SQLite");
        assertEquals("SQLite", prop.getDbProductName());
    }
}
