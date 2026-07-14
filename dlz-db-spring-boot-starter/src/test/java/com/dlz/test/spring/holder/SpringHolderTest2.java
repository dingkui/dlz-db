package com.dlz.test.spring.holder;

import com.dlz.spring.holder.SpringHolder;
import com.dlz.test.config.BaseDBTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * SpringHolder 测试类
 *
 * <p>测试 SpringHolder 的各种功能：</p>
 * <ul>
 *   <li>BeanFactory 初始化</li>
 *   <li>Bean 获取（按名称、按类型）</li>
 *   <li>Bean 注册与注销</li>
 *   <li>Bean 创建与自动装配</li>
 *   <li>事件发布</li>
 *   <li>资源获取</li>
 * </ul>
 */
@Slf4j
public class SpringHolderTest2 {
    /**
     * 测试：init() 方法 - 使用默认配置
     */
    @Test
    void testInit() {
        assertThrows(IllegalStateException.class, ()->SpringHolder.getBean(BaseDBTest.class));
    }
}
