package com.dlz.db.spring.config;

import com.dlz.db.core.DlzDbProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 验证 {@link SpringDlzDbAutoConfiguration} 自动装配生效：
 * spring.factories 注册、条件注解、配置属性绑定、核心 bean 注册。
 */
class SpringDlzDbAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SpringDlzDbAutoConfiguration.class,
                    ConfigurationPropertiesAutoConfiguration.class));

    @Test
    void autoConfigRegistersCoreBeans() {
        runner.withPropertyValues("dlz.db.dbSupport=sqlite")
                .withBean(HikariDataSource.class, () -> {
                    HikariDataSource ds = new HikariDataSource();
                    ds.setJdbcUrl("jdbc:sqlite::memory:");
                    return ds;
                })
                .run(ctx -> {
                    assertNotNull(ctx.getBean(DlzDbProperties.class));
                    assertNotNull(ctx.getBean("initDbAdapter"));
                    assertNotNull(ctx.getBean("JdbcTemplate"));
                    DlzDbProperties props = ctx.getBean(DlzDbProperties.class);
                    assertEquals("sqlite", props.getDbSupport());
                });
    }

    @Test
    void autoConfigSkipsWhenUserDefinesAdapter() {
        runner.withPropertyValues("dlz.db.dbSupport=sqlite")
                .withBean(HikariDataSource.class, () -> {
                    HikariDataSource ds = new HikariDataSource();
                    ds.setJdbcUrl("jdbc:sqlite::memory:");
                    return ds;
                })
                .withUserConfiguration(UserAdapterConfig.class)
                .run(ctx -> {
                    // 用户自定义的 initDbAdapter 生效，自动配置不再重复注册
                    assertThat(ctx).hasSingleBean(com.dlz.db.core.DlzDbAdapter.class);
                });
    }

    @org.springframework.context.annotation.Configuration
    static class UserAdapterConfig {
        @org.springframework.context.annotation.Bean(name = "initDbAdapter")
        public com.dlz.db.core.DlzDbAdapter userAdapter(DlzDbProperties properties) {
            return new com.dlz.db.core.DlzDbAdapter(properties,
                    () -> null,
                    () -> null,
                    ds -> null);
        }
    }
}
