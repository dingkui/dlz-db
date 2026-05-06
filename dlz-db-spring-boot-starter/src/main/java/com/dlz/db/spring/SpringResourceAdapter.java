package com.dlz.db.spring;

import com.dlz.db.core.abstractor.AResourceAdapter;

/**
 * Spring 资源加载器实现。
 * <p>直接继承 {@link AResourceAdapter}，复用 core 模块的 {@code ResourceMatcher}（自研，
 * 支持 classpath* 和通配符），不再依赖 {@code PathMatchingResourcePatternResolver}。</p>
 */
public class SpringResourceAdapter extends AResourceAdapter {
}
