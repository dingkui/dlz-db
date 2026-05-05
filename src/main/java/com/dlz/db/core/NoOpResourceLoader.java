package com.dlz.db.core;

import java.io.InputStream;

/**
 * 空操作资源加载器。
 * <p>核心模块默认实现，不支持资源加载。</p>
 */
public class NoOpResourceLoader implements ResourceLoader {

    @Override
    public InputStream[] getResources(String location) throws Exception {
        return new InputStream[0];
    }

    @Override
    public InputStream getResource(String location) throws Exception {
        return null;
    }
}
