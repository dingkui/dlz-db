package com.dlz.db.spring.config;

import com.dlz.db.spring.SpringSqlExecutorAdapter;
import com.dlz.db.spring.SpringTxExecutorAdapter;
import com.dlz.db.support.DBHolder;
import com.dlz.spring.holder.SpringHolder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author dk
 * date 2020-10-15
 */
@Slf4j
@EnableConfigurationProperties({SpringDlzDbProperties.class})
public class SpringDlzDbConfig{
    /**
     * spring 容器启动开始执行
     */
    @Bean
    public static BeanFactoryPostProcessor myBeanFactory() {
        return beanFactory -> {
            SpringHolder.init(beanFactory);
        };
    }

    /**
     * 容器刷新完成后初始化 DBHolder（此时 ConfigurationPropertiesBindingPostProcessor 已注册，
     * SpringDlzDbProperties 已绑定配置文件中的值）
     */
    @Bean
    public ApplicationListener<ContextRefreshedEvent> dbInitListener(SpringDlzDbProperties properties) {
        return event -> {
//            DataSource dataSource = SpringHolder.getBean(DataSource.class);
//            JdbcTemplate jdbcTemplate = SpringHolder.getBean(JdbcTemplate.class);
//            DBHolder.init(properties,
//                    () -> dataSource,
//                    () -> new SpringSqlExecutorAdapter(jdbcTemplate),
//                    SpringTxExecutorAdapter::new);
            DBHolder.init(properties,
                    () -> SpringHolder.getBean(DataSource.class),
                    () -> new SpringSqlExecutorAdapter(SpringHolder.getBean(JdbcTemplate.class)),
                    SpringTxExecutorAdapter::new);
        };
    }

    @Bean(name = "JdbcTemplate")
    @Lazy
    @ConditionalOnMissingBean(name = "JdbcTemplate")
    @SuppressFBWarnings(value = "NM_METHOD_NAMING_CONVENTION", justification = "Bean名称与类名一致，方法名故意匹配类名")
    public JdbcTemplate JdbcTemplate(DataSource dataSource) {
        if (log.isInfoEnabled()) {
            log.info("init JdbcTemplate:" + DynamicJdbcTemplate.class.getName());
        }
        return new DynamicJdbcTemplate(dataSource);
    }
}
