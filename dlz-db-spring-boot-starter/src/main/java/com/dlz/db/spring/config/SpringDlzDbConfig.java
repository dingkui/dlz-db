package com.dlz.db.spring.config;

import com.dlz.db.convertor.dbtype.TableColumnMapper;
import com.dlz.db.core.ADbProvider;
import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.core.IRedisExecutor;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.support.helper.HelperScan;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.SqlHolder;
import com.dlz.db.service.ICommService;
import com.dlz.db.service.impl.CommServiceImpl;
import com.dlz.db.spring.SpringCacheAdapter;
import com.dlz.db.spring.SpringSqlExecutorAdapter;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.db.util.DbLogUtil;
import com.dlz.spring.config.DlzFwConfig;
import com.dlz.spring.redis.excutor.JedisExecutor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author dk
 * date 2020-10-15
 */
@Slf4j
@EnableConfigurationProperties({SpringDlzDbProperties.class})
public class SpringDlzDbConfig extends DlzFwConfig {
    @Bean(name = "dbProvider")
    @ConditionalOnMissingBean(name = "dbProvider")
    @DependsOn("JdbcTemplate")
    public ADbProvider dbProvider(SpringDlzDbProperties properties) {
        ADbProvider provider = new SpringDbProvider(properties);
        DBHolder.setDbProvider(provider);
        DbLogUtil.init(properties);
        if (log.isInfoEnabled()) {
            log.info("init dbProvider:" + SpringDbProvider.class.getName());
        }
        return provider;
    }

    @Bean(name = "sqlExecutor")
    @Lazy
    @ConditionalOnMissingBean(name = "sqlExecutor")
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Spring单例配置类，实例方法初始化全局静态组件")
    public ISqlExecutor sqlExecutor(JdbcTemplate jdbc, ADbProvider dbProvider) {
        final DlzDbProperties properties = dbProvider.getSqlConfig();
        final SpringSqlExecutorAdapter springSqlExecutorAdapter = new SpringSqlExecutorAdapter(jdbc);
        SqlHolder.init();
        DbConvertUtil.defaultTableColumnMapper = new TableColumnMapper(springSqlExecutorAdapter);
        if (log.isInfoEnabled()) {
            log.info("init sqlExecutor:" + SpringSqlExecutorAdapter.class.getName());
            log.info("init tableCloumnMapper:" + TableColumnMapper.class.getName());
        }
        SqlHolder.loadDbSql();
        DBHolder.sqlExecutor = springSqlExecutorAdapter;
        //自动扫描
        if (properties.getHelper().isAutoUpdate()) {
            log.info("dlzHelper autoUpdate ...");
            HelperScan.scan(properties.getHelper().getPackageName());
        }
        return springSqlExecutorAdapter;
    }
    @Bean(name = "commService")
    @Lazy
    @ConditionalOnMissingBean(name = "commService")
    public ICommService commService(ISqlExecutor sqlExecutor) {
        return new CommServiceImpl(sqlExecutor);
    }

    @Bean(name = "cacheExecutor")
    @Lazy
    @ConditionalOnMissingBean(name = "cacheExecutor")
    @ConditionalOnClass(JedisExecutor.class)
    public IRedisExecutor cacheExecutor(JedisExecutor jedisExecutor) {
        IRedisExecutor cacheExecutor = new SpringCacheAdapter(jedisExecutor);
        if (log.isInfoEnabled()) {
            log.info("init cacheExecutor:" + SpringCacheAdapter.class.getName());
        }
        return cacheExecutor;
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
