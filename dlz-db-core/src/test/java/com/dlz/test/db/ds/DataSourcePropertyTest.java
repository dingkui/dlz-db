package com.dlz.test.db.ds;

import com.dlz.db.ds.DataSourceProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSourceProperty 数据源属性测试
 */
@DisplayName("DataSourceProperty 数据源属性测试")
class DataSourcePropertyTest {

    private DataSourceProperty property;

    @BeforeEach
    void setUp() {
        property = new DataSourceProperty();
    }

    @Test
    @DisplayName("测试基本属性设置")
    void testBasicProperties() {
        property.setName("test_db");
        property.setUrl("jdbc:mysql://localhost:3306/test");
        property.setUsername("root");
        property.setPassword("123456");
        property.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        assertEquals("test_db", property.getName());
        assertEquals("jdbc:mysql://localhost:3306/test", property.getUrl());
        assertEquals("root", property.getUsername());
        assertEquals("123456", property.getPassword());
        assertEquals("com.mysql.cj.jdbc.Driver", property.getDriverClassName());
    }

    @Test
    @DisplayName("测试连接池配置默认值")
    void testDefaultPoolConfig() {
        assertEquals(10, property.getMaxPoolSize());
        assertEquals(2, property.getMinIdle());
        assertEquals(30000, property.getConnectionTimeout());
        assertEquals(3000, property.getValidationTimeout());
        assertEquals(600000, property.getIdleTimeout());
        assertEquals(1800000, property.getMaxLifetime());
        assertEquals(60000, property.getLeakDetectionThreshold());
    }

    @Test
    @DisplayName("测试连接池配置自定义值")
    void testCustomPoolConfig() {
        property.setMaxPoolSize(20);
        property.setMinIdle(5);
        property.setConnectionTimeout(60000);
        property.setValidationTimeout(5000);
        property.setIdleTimeout(1200000);
        property.setMaxLifetime(3600000);
        property.setLeakDetectionThreshold(120000);
        
        assertEquals(20, property.getMaxPoolSize());
        assertEquals(5, property.getMinIdle());
        assertEquals(60000, property.getConnectionTimeout());
        assertEquals(5000, property.getValidationTimeout());
        assertEquals(1200000, property.getIdleTimeout());
        assertEquals(3600000, property.getMaxLifetime());
        assertEquals(120000, property.getLeakDetectionThreshold());
    }

    @Test
    @DisplayName("测试额外配置属性")
    void testAdditionalProperties() {
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("cachePrepStmts", true);
        additionalProps.put("prepStmtCacheSize", 250);
        
        property.setAdditionalProperties(additionalProps);
        
        assertNotNull(property.getAdditionalProperties());
        assertEquals(2, property.getAdditionalProperties().size());
        assertTrue((Boolean) property.getAdditionalProperties().get("cachePrepStmts"));
        assertEquals(250, property.getAdditionalProperties().get("prepStmtCacheSize"));
    }

    @Test
    @DisplayName("测试健康检查配置")
    void testHealthCheckRegistry() {
        Map<String, String> healthCheck = new HashMap<>();
        healthCheck.put("healthCheckMetricRegistry", "my-registry");
        
        property.setHealthCheckRegistry(healthCheck);
        
        assertNotNull(property.getHealthCheckRegistry());
        assertEquals(1, property.getHealthCheckRegistry().size());
        assertEquals("my-registry", property.getHealthCheckRegistry().get("healthCheckMetricRegistry"));
    }

    @Test
    @DisplayName("测试可选属性")
    void testOptionalProperties() {
        property.setDbProductName("MySQL");
        property.setTestQuery("SELECT 1");
        property.setSchema("public");
        property.setCreatorClassName("com.example.CustomCreator");
        
        assertEquals("MySQL", property.getDbProductName());
        assertEquals("SELECT 1", property.getTestQuery());
        assertEquals("public", property.getSchema());
        assertEquals("com.example.CustomCreator", property.getCreatorClassName());
    }

    @Test
    @DisplayName("测试空字符串和 null 值")
    void testNullAndEmptyValues() {
        property.setName(null);
        property.setUrl("");
        property.setUsername(null);
        
        assertNull(property.getName());
        assertEquals("", property.getUrl());
        assertNull(property.getUsername());
    }
}
