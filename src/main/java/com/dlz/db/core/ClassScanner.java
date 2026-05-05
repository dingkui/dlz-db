package com.dlz.db.core;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * 类扫描器接口。
 * <p>抽象类扫描逻辑，支持不同框架的类扫描实现（Spring、Solon 等）。</p>
 *
 * <p>仅支持注解扫描，不扫描 BeanDefinition。</p>
 */
public interface ClassScanner {

    /**
     * 扫描指定包下带有指定注解的类。
     *
     * @param basePackage     基础包名
     * @param annotationClass 注解类型
     * @return 匹配的类集合
     * @throws Exception 扫描失败
     */
    Set<Class<?>> scan(String basePackage, Class<? extends Annotation> annotationClass) throws Exception;
}
