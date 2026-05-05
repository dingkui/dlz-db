package com.dlz.db.core;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * 空操作类扫描器。
 * <p>核心模块默认实现，不支持类扫描。</p>
 */
public class NoOpClassScanner implements ClassScanner {

    @Override
    public Set<Class<?>> scan(String basePackage, Class<? extends Annotation> annotationClass) throws Exception {
        return Collections.emptySet();
    }
}
