package com.dlz.test.spring.holder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 测试事件监听器
 */

@Component
@Slf4j
public class TestEventListener implements ApplicationListener<TestEvent> {
    @Override
    public void onApplicationEvent(TestEvent event) {
        log.info("收到测试事件: {}", event.getMessage());
    }
}