package com.dlz.db.core.abstractor;

import com.dlz.db.core.IResourceLoader;

import java.io.InputStream;

/**
 * 资源加载器抽象适配器。
 * <p>提供资源加载的默认实现，子类可选择性重写方法。</p>
 *
 * @since 7.0.0
 */
public abstract class AResourceAdapter implements IResourceLoader {

    @Override
    public InputStream[] getResources(String location) throws Exception {
        // 默认返回空数组
        return new InputStream[0];
    }

    @Override
    public InputStream getResource(String location) throws Exception {
        // 默认返回 null
        return null;
    }
}
