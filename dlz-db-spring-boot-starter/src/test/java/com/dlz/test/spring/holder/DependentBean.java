package com.dlz.test.spring.holder;

/**
 * 测试用的依赖 Bean 类
 */
public class DependentBean {
    // 可以添加需要注入的字段
    private String value = "dependent";

    public String getValue() {
        return value;
    }
}

