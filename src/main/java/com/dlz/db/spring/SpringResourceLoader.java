package com.dlz.db.spring;

import com.dlz.db.core.ResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring 资源加载器实现。
 * <p>基于 Spring {@link PathMatchingResourcePatternResolver}，支持 classpath*: 模式。</p>
 */
public class SpringResourceLoader implements ResourceLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public SpringResourceLoader() {
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }

    @Override
    public InputStream[] getResources(String location) throws Exception {
        Resource[] resources = resourcePatternResolver.getResources(location);
        List<InputStream> inputStreams = new ArrayList<>();
        for (Resource resource : resources) {
            inputStreams.add(resource.getInputStream());
        }
        return inputStreams.toArray(new InputStream[0]);
    }

    @Override
    public InputStream getResource(String location) throws Exception {
        Resource resource = resourcePatternResolver.getResource(location);
        if (resource.exists()) {
            return resource.getInputStream();
        }
        return null;
    }
}
