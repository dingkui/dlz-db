package com.dlz.test.db.config;

import com.dlz.db.spring.config.SpringDlzDbConfig;
import com.dlz.db.spring.config.SpringDlzDbProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author dk
 * date 2020-10-15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({SpringDlzDbProperties.class})
public class DlzDbConfigs extends SpringDlzDbConfig {

}
