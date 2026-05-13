package com.dlz.test.db.core;

import com.dlz.db.annotation.TableName;
import com.dlz.db.core.DlzResourceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DlzResourceAdapter 资源适配器测试
 */
@DisplayName("DlzResourceAdapter 资源适配器测试")
class DlzResourceAdapterTest {

    private DlzResourceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DlzResourceAdapter();
    }

    @Test
    @DisplayName("测试 getResources - 获取多个资源")
    void testGetResources() throws Exception {
        InputStream[] streams = adapter.getResources("classpath*:META-INF/MANIFEST.MF");
        
        assertNotNull(streams);
        // 关闭所有流
        for (InputStream stream : streams) {
            if (stream != null) {
                stream.close();
            }
        }
    }

    @Test
    @DisplayName("测试 getResource - 获取单个资源")
    void testGetResource() throws Exception {
        InputStream stream = adapter.getResource("classpath:logback.xml");
        
        // 可能为 null
        if (stream != null) {
            stream.close();
        }
    }

    @Test
    @DisplayName("测试 scan - 扫描包下的类")
    void testScan() throws Exception {
        // 扫描当前测试类所在的包
        Set<Class<?>> classes = adapter.scan("com.dlz.test.db.core", null);
        
        assertNotNull(classes);
        assertTrue(classes.size() > 0, "应该扫描到至少一个类");
    }

    @Test
    @DisplayName("测试 scan - 带注解过滤")
    void testScanWithAnnotation() throws Exception {
        // 扫描带有 TableName 注解的类（可能没有）
        Set<Class<?>> classes = adapter.scan("com.dlz.db.modal", TableName.class);
        
        assertNotNull(classes);
        // 验证所有返回的类都有 TableName 注解
        for (Class<?> clazz : classes) {
            assertTrue(clazz.isAnnotationPresent(TableName.class));
        }
    }

    @Test
    @DisplayName("测试 scan - null 包名返回空集合")
    void testScanWithNullPackage() throws Exception {
        Set<Class<?>> classes = adapter.scan(null, null);
        
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    @DisplayName("测试 scan - 空包名返回空集合")
    void testScanWithEmptyPackage() throws Exception {
        Set<Class<?>> classes = adapter.scan("", null);
        
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    @DisplayName("测试 scan - 不存在的包返回空集合")
    void testScanWithNonExistentPackage() throws Exception {
        Set<Class<?>> classes = adapter.scan("com.nonexistent.package.xyz", null);
        
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }
}
