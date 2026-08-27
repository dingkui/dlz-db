package com.dlz.test.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author dk
 * date 2020-10-15
 * <p>DLZ-DB 已通过 starter 自动装配，本类仅保留测试所需的目录初始化。</p>
 */
@Slf4j
@Configuration
public class DlzDbConfigs {
    /**
     * spring 容器启动开始执行
     */
    @Bean
    public static BeanFactoryPostProcessor myBeanFactory1() {
        log.info("init DlzDbConfigs111111111");
        return beanFactory -> {
            File dir = new File("./test");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        };
    }
}
