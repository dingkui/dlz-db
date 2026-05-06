package com.dlz.db.core;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * 资源加载器接口（统一资源 + 类扫描）。
 * <p>抽象资源加载与类扫描逻辑，所有框架共用 {@link com.dlz.db.core.abstractor.AResourceAdapter}
 * 默认实现。支持 {@code classpath*:} 模式跨 JAR 扫描。</p>
 *
 * @since 7.0.0
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

    /**
     * 扫描指定包下带有指定注解的类。
     *
     * @param basePackage     基础包名
     * @param annotationClass 注解类型，{@code null} 表示返回所有类
     * @return 匹配的类集合
     * @throws Exception 扫描失败
     */
    Set<Class<?>> scan(String basePackage, Class<? extends Annotation> annotationClass) throws Exception;
}
