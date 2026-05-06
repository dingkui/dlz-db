package com.dlz.db.core.abstractor;

import com.dlz.db.core.IClassScanner;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * 类扫描器抽象适配器。
 * <p>提供类扫描的默认实现，子类可选择性重写方法。</p>
 *
 * @since 7.0.0
 */
public abstract class AClassScannerAdapter implements IClassScanner {

    @Override
    public Set<Class<?>> scan(String basePackage, Class<? extends Annotation> annotationClass) throws Exception {
        // 默认返回空集合
        return Collections.emptySet();
    }
}
