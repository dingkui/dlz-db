package com.dlz.db.spring;

import com.dlz.db.core.IClassScanner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Spring 类扫描器实现。
 * <p>基于 Spring {@link ClassPathScanningCandidateComponentProvider}，仅支持注解扫描。</p>
 */
public class SpringClassScannerAdapter implements IClassScanner {

    @Override
    public Set<Class<?>> scan(String basePackage, Class<? extends Annotation> annotationClass) throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));

        for (BeanDefinition beanDefinition : scanner.findCandidateComponents(basePackage)) {
            try {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                classes.add(clazz);
            } catch (ClassNotFoundException e) {
                throw new Exception("扫描类失败: " + beanDefinition.getBeanClassName(), e);
            }
        }
        return classes;
    }
}
