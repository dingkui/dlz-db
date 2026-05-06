package com.dlz.db.core;

import java.io.InputStream;

/**
 * 资源加载器接口。
 * <p>抽象资源加载逻辑，支持不同框架的资源加载实现（Spring、Solon 等）。</p>
 *
 * <p>支持 classpath*: 模式扫描 JAR 包内的资源。</p>
 */
public interface IResourceLoader {

    /**
     * 根据路径加载资源输入流。
     *
     * @param location 资源路径，支持 classpath*: 模式
     * @return 资源输入流数组，支持多个匹配
     * @throws Exception 加载失败
     */
    InputStream[] getResources(String location) throws Exception;

    /**
     * 根据路径加载单个资源输入流。
     *
     * @param location 资源路径
     * @return 资源输入流，不存在返回 null
     * @throws Exception 加载失败
     */
    InputStream getResource(String location) throws Exception;
}
