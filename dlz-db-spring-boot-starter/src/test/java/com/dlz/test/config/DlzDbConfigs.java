package com.dlz.test.config;

import com.dlz.db.spring.config.SpringDlzDbConfig;
import com.dlz.db.spring.config.SpringDlzDbProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author dk
 * date 2020-10-15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({SpringDlzDbProperties.class})
public class DlzDbConfigs extends SpringDlzDbConfig {
    /**
     * spring 容器启动开始执行
     */
    @Bean
    public static BeanFactoryPostProcessor myBeanFactory1() {
        log.info("init DlzDbConfigs");
        return beanFactory -> {
            File dir = new File("./test");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        };
    }
}
