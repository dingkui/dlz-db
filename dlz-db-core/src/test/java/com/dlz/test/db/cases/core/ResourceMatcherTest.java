package com.dlz.test.db.cases.core;

import com.dlz.db.support.resouce.DlzResourceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResourceMatcher 资源匹配器测试
 */
@DisplayName("ResourceMatcher 资源匹配器测试")
class ResourceMatcherTest {

    @Test
    @DisplayName("测试 getResources - null 路径返回空列表")
    void testGetResourcesWithNull() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources(null);
        
        assertNotNull(urls);
        assertTrue(urls.isEmpty());
    }

    @Test
    @DisplayName("测试 getResources - classpath: 前缀")
    void testGetResourcesWithClasspathPrefix() throws Exception {
        // 查找一个可能存在的资源
        List<URL> urls = DlzResourceLoader.getResources("classpath:logback.xml");
        
        assertNotNull(urls);
        // 不验证数量，因为可能不存在
    }

    @Test
    @DisplayName("测试 getResources - classpath*: 前缀")
    void testGetResourcesWithClasspathAllPrefix() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("classpath*:META-INF/MANIFEST.MF");
        
        assertNotNull(urls);
        // MANIFEST.MF 通常在 jar 中存在
    }

    @Test
    @DisplayName("测试 getResources - 带通配符的模式")
    void testGetResourcesWithWildcard() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("classpath*:*.xml");
        
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 getResources - ** 多层通配")
    void testGetResourcesWithDoubleStar() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("classpath*:**/*.properties");
        
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 getResources - ? 单字符通配")
    void testGetResourcesWithQuestionMark() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("classpath*:logback?.xml");
        
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 getResourceStream - 获取单个资源流")
    void testGetResourceStream() throws Exception {
        InputStream stream = DlzResourceLoader.getResourceStream("classpath:logback.xml");
        
        // 可能为 null，取决于是否存在该资源
        if (stream != null) {
            stream.close();
        }
    }

    @Test
    @DisplayName("测试 getResourceStream - 不存在的资源返回 null")
    void testGetResourceStreamNotFound() throws Exception {
        InputStream stream = DlzResourceLoader.getResourceStream("classpath:nonexistent_file_xyz.sql");
        
        assertNull(stream);
    }

    @Test
    @DisplayName("测试 getResourceStreams - 获取多个资源流")
    void testGetResourceStreams() throws Exception {
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
    @DisplayName("测试 hasWildcard - 包含星号")
    void testHasWildcardWithStar() throws Exception {
        // 通过调用 getResources 间接测试
        List<URL> urls = DlzResourceLoader.getResources("classpath*:test*.sql");
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试 hasWildcard - 包含问号")
    void testHasWildcardWithQuestionMark() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("classpath*:test?.sql");
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试无前缀的路径")
    void testGetResourcesWithoutPrefix() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("META-INF/MANIFEST.MF");
        
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试带前导斜杠的路径")
    void testGetResourcesWithLeadingSlash() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("classpath:/META-INF/MANIFEST.MF");
        
        assertNotNull(urls);
    }

    @Test
    @DisplayName("测试多个前导斜杠的路径")
    void testGetResourcesWithMultipleLeadingSlashes() throws Exception {
        List<URL> urls = DlzResourceLoader.getResources("classpath:///META-INF/MANIFEST.MF");
        
        assertNotNull(urls);
    }
}
