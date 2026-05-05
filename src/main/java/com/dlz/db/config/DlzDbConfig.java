package com.dlz.db.config;

import com.dlz.db.convertor.dbtype.TableColumnMapper;
import com.dlz.db.core.CacheExecutor;
import com.dlz.db.core.ClassScanner;
import com.dlz.db.core.ResourceLoader;
import com.dlz.db.core.SqlExecutor;
import com.dlz.db.spring.SpringCacheExecutor;
import com.dlz.db.spring.SpringClassScanner;
import com.dlz.db.spring.SpringResourceLoader;
import com.dlz.db.spring.SpringSqlExecutor;
import com.dlz.db.ds.DynamicJdbcTemplate;
import com.dlz.db.helper.support.HelperScan;
import com.dlz.db.holder.DBHolder;
import com.dlz.db.holder.SqlHolder;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.db.util.DbLogUtil;
import com.dlz.spring.config.DlzFwConfig;
import com.dlz.spring.redis.excutor.JedisExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author dk
 * date 2020-10-15
 */
@Slf4j
@EnableConfigurationProperties({DlzDbProperties.class})
public class DlzDbConfig extends DlzFwConfig {
    @Bean(name = "sqlExecutor")
    @Lazy
    @ConditionalOnMissingBean(name = "sqlExecutor")
    public SqlExecutor sqlExecutor(JdbcTemplate jdbc, DlzDbProperties properties) {
        SqlHolder.init(properties);
        DbLogUtil.init(properties);
        final SqlExecutor sqlExecutor = new SpringSqlExecutor(jdbc);
        DbConvertUtil.defaultTableCloumnMapper = new TableColumnMapper(sqlExecutor);
        if (log.isInfoEnabled()) {
            log.info("init sqlExecutor:" + SpringSqlExecutor.class.getName());
            log.info("init tableCloumnMapper:" + TableColumnMapper.class.getName());
        }
        DBHolder.sqlExecutor = sqlExecutor;
        SqlHolder.loadDbSql();
        //自动扫描
        if (properties.getHelper().autoUpdate) {
            log.info("dlzHelper autoUpdate ...");
            HelperScan.scan(properties.getHelper().packageName);
        }
        return sqlExecutor;
    }

    @Bean(name = "resourceLoader")
    @ConditionalOnMissingBean(name = "resourceLoader")
    public ResourceLoader resourceLoader() {
        ResourceLoader resourceLoader = new SpringResourceLoader();
        SqlHolder.setResourceLoader(resourceLoader);
        if (log.isInfoEnabled()) {
            log.info("init resourceLoader:" + SpringResourceLoader.class.getName());
        }
        return resourceLoader;
    }

    @Bean(name = "classScanner")
    @Lazy
    @ConditionalOnMissingBean(name = "classScanner")
    public ClassScanner classScanner() {
        ClassScanner classScanner = new SpringClassScanner();
        HelperScan.setClassScanner(classScanner);
        if (log.isInfoEnabled()) {
            log.info("init classScanner:" + SpringClassScanner.class.getName());
        }
        return classScanner;
    }

    @Bean(name = "cacheExecutor")
    @Lazy
    @ConditionalOnMissingBean(name = "cacheExecutor")
    @ConditionalOnClass(JedisExecutor.class)
    public CacheExecutor cacheExecutor(JedisExecutor jedisExecutor) {
        CacheExecutor cacheExecutor = new SpringCacheExecutor(jedisExecutor);
        DBHolder.setCacheExecutor(cacheExecutor);
        if (log.isInfoEnabled()) {
            log.info("init cacheExecutor:" + SpringCacheExecutor.class.getName());
        }
        return cacheExecutor;
    }

    @Bean(name = "JdbcTemplate")
    @Lazy
    @ConditionalOnMissingBean(name = "JdbcTemplate")
    public JdbcTemplate JdbcTemplate(DataSource dataSource) {
        if (log.isInfoEnabled()) {
            log.info("init JdbcTemplate:" + DynamicJdbcTemplate.class.getName());
        }
        return new DynamicJdbcTemplate(dataSource);
    }
}
