package com.dlz.test.spring.holder;

import org.springframework.context.ApplicationEvent;

/**
 * 测试事件类
 */
public class TestEvent extends ApplicationEvent {
    private final String message;

    public TestEvent(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}