package com.dlz.db.spring.config;

import com.dlz.db.core.DlzDbAdapter;
import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.internal.holder.DBHolder;
import com.dlz.db.spring.SpringSqlExecutorAdapter;
import com.dlz.db.spring.SpringTxExecutorAdapter;
import com.dlz.spring.holder.SpringHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * DLZ-DB Spring Boot 2.x 自动装配入口。
 * <p>通过 {@code META-INF/spring.factories} 注册，导入依赖即生效，无需手动继承或配置类。</p>
 * <p>自动完成：{@code dlz.db.*} 配置绑定、SpringHolder 注入、JdbcTemplate 注册、DBHolder 初始化。</p>
 */
@Slf4j
@Configuration
@ConditionalOnClass({DataSource.class, JdbcTemplate.class})
public class SpringDlzDbAutoConfiguration {

    /**
     * 在容器刷新早期把 BeanFactory 注入 SpringHolder（静态引用），供 DBHolder 的懒加载工厂使用。
     */
    @Bean
    public static BeanFactoryPostProcessor springHolderRegister() {
        return beanFactory -> SpringHolder.init(beanFactory);
    }

    @Bean
    @ConfigurationProperties(prefix = "dlz.db")
    public DlzDbProperties dlzDbProperties() {
        return new DlzDbProperties();
    }

    /**
     * 动态 JdbcTemplate：getDataSource 从 DB.ds 动态获取，支持多数据源切换。
     */
    @Bean
    public JdbcTemplate JdbcTemplate(DataSource dataSource) {
        return new DynamicJdbcTemplate(dataSource);
    }

    @Bean(name = "initDbAdapter")
    @ConditionalOnMissingBean(name = "initDbAdapter")
    public DlzDbAdapter initDbAdapter(DlzDbProperties properties) {
        return DBHolder.init(properties,
                () -> SpringHolder.getBean(DataSource.class),
                () -> new SpringSqlExecutorAdapter(SpringHolder.getBean(JdbcTemplate.class)),
                SpringTxExecutorAdapter::new);
    }
}
