package com.dlz.test.db.cases.core;

import com.dlz.db.annotation.TableName;
import com.dlz.db.support.resouce.DlzResourceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DlzResourceAdapter 资源适配器测试
 */
@DisplayName("DlzResourceAdapter 资源适配器测试")
class DlzResourceAdapterTest {
    @Test
    @DisplayName("测试 getResources - 获取多个资源")
    void testGetResources() throws Exception {
        InputStream[] streams = DlzResourceLoader.getResourceStreams("classpath*:META-INF/MANIFEST.MF");
        
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
        InputStream stream = DlzResourceLoader.getResourceStream("classpath:logback.xml");
        
        // 可能为 null
        if (stream != null) {
            stream.close();
        }
    }

    @Test
    @DisplayName("测试 getResourceStream - 获取存在的资源（覆盖非空 URL 分支）")
    void testGetResourceStreamWithExistingResource() throws Exception {
        // 不加 classpath: 前缀，用类路径根下的已知文件
        InputStream stream = DlzResourceLoader.getResourceStream("logback-test.xml");
        assertNotNull(stream, "logback-test.xml 应存在并成功打开流");
        stream.close();
    }

    @Test
    @DisplayName("测试 getResources - 通配符路径不使用 classpath: 前缀（覆盖 enumOf）")
    void testGetResourcesWithWildcardNoPrefix() throws Exception {
        // 不加 classpath: 前缀 → all=false → findPatternResources 中使用 enumOf
        List<URL> urls = DlzResourceLoader.getResources("com/dlz/**/*.class");
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 getResources - 同时包含 * 和 ?（覆盖 firstWildcardIndex Math.min）")
    void testGetResourcesWithBothWildcards() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("com/dlz/**/test?.class");
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 getResources - pattern 结尾为 **（覆盖 wildcardToRegex 裸 ** 分支）")
    void testGetResourcesWithDoubleStarEnd() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("classpath:com/dlz/**");
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 getResources - pattern 内嵌 /**/（覆盖 wildcardToRegex /**/ 分支）")
    void testGetResourcesWithEmbeddedDoubleStarSlash() throws Exception {
        // com/dlz/**/x/**/y.class → pattern = "**/x/**/y.class" 触发 /**/ 分支
        List<URL> urls = DlzResourceLoader.getResources("com/dlz/**/x/**/y.class");
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 getResources - pattern 中 /** 结尾（覆盖 wildcardToRegex /** 分支）")
    void testGetResourcesWithDoubleStarAfterSlash() throws Exception {
        // com/dlz/**/x/**y → pattern = "**/x/**y" 触发 /** 无斜杠分支
        List<URL> urls = DlzResourceLoader.getResources("classpath:com/dlz/**/x/**y");
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 scan - 扫描包下的类")
    void testScan() throws Exception {
        // 扫描当前测试类所在的包
        Set<Class<?>> classes = DlzResourceLoader.scan("com.dlz.test.db.entity", null);
        
        assertNotNull(classes);
        assertTrue(classes.size() > 0, "应该扫描到至少一个类");
    }

    @Test
    @DisplayName("测试 scan - 带注解过滤")
    void testScanWithAnnotation() throws Exception {
        // 扫描带有 TableName 注解的类（可能没有）
        Set<Class<?>> classes = DlzResourceLoader.scan("com.dlz.db.modal", TableName.class);
        
        assertNotNull(classes);
        // 验证所有返回的类都有 TableName 注解
        for (Class<?> clazz : classes) {
            assertTrue(clazz.isAnnotationPresent(TableName.class));
        }
    }

    @Test
    @DisplayName("测试 scan - null 包名返回空集合")
    void testScanWithNullPackage() throws Exception {
        Set<Class<?>> classes = DlzResourceLoader.scan(null, null);
        
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    @DisplayName("测试 scan - 空包名返回空集合")
    void testScanWithEmptyPackage() throws Exception {
        Set<Class<?>> classes = DlzResourceLoader.scan("", null);
        
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    @DisplayName("测试 scan - 不存在的包返回空集合")
    void testScanWithNonExistentPackage() throws Exception {
        Set<Class<?>> classes = DlzResourceLoader.scan("com.nonexistent.package.xyz", null);
        
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }
}
