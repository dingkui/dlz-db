package com.dlz.db.core.abstractor;

import com.dlz.db.core.IResourceLoader;
import com.dlz.db.core.ResourceMatcher;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 资源加载器统一默认实现：基于自研 {@link ResourceMatcher}，
 * 支持 {@code classpath*:} 跨 JAR 资源加载与类扫描。
 *
 * <p>所有框架（Spring/Solon 等）统一复用本类，无需各自实现。</p>
 *
 * @since 7.0.0
 */
@Slf4j
public class AResourceAdapter implements IResourceLoader {

    @Override
    public InputStream[] getResources(String location) throws Exception {
        return ResourceMatcher.getResourceStreams(location);
    }

    @Override
    public InputStream getResource(String location) throws Exception {
        return ResourceMatcher.getResourceStream(location);
    }

    @Override
    public Set<Class<?>> scan(String basePackage, Class<? extends Annotation> annotationClass) throws Exception {
        Set<Class<?>> result = new HashSet<>();
        if (basePackage == null || basePackage.isEmpty()) {
            return result;
        }
        String basePath = basePackage.replace('.', '/');
        String pattern = "classpath*:" + basePath + "/**/*.class";
        List<URL> urls = ResourceMatcher.getResources(pattern);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        for (URL url : urls) {
            String className = extractClassName(url, basePath);
            if (className == null) {
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className, false, cl);
                if (annotationClass == null || clazz.isAnnotationPresent(annotationClass)) {
                    result.add(clazz);
                }
            } catch (Throwable e) {
                log.debug("加载类失败 {}: {}", className, e.getMessage());
            }
        }
        return result;
    }

    /** 从资源 URL 提取类全限定名。 */
    private static String extractClassName(URL url, String basePath) {
        String path = url.toString();
        int idx = path.indexOf(basePath);
        if (idx < 0) {
            return null;
        }
        String classPath = path.substring(idx);
        if (!classPath.endsWith(".class")) {
            return null;
        }
        classPath = classPath.substring(0, classPath.length() - ".class".length());
        return classPath.replace('/', '.');
    }
}
