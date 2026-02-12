package com.dlz.test.db.config;

import com.dlz.db.config.DlzDbConfig;
import com.dlz.db.config.DlzDbProperties;
import com.dlz.db.convertor.dbtype.TableColumnMapper;
import com.dlz.db.dao.IDlzDao;
import com.dlz.db.holder.DBHolder;
import com.dlz.db.holder.SqlHolder;
import com.dlz.db.modal.DB;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.db.util.DbLogUtil;
import com.dlz.spring.config.DlzProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author dk
 * date 2020-10-15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({DlzDbProperties.class, DlzProperties.class})
public class DlzDbConfigs extends DlzDbConfig {

    @Bean(name = "dlzDao")
    @Lazy
    @DependsOn("JdbcTemplate")
    public IDlzDao dlzDao2(DlzDbProperties properties) {
        SqlHolder.init(properties);
        DbLogUtil.init(properties);
        final IDlzDao dlzDao = new DlzTestDao();
        DbConvertUtil.tableCloumnMapper= new TableColumnMapper(dlzDao);
        DBHolder.dao = dlzDao;
        if(log.isInfoEnabled()){
            log.info("init test dlzDao:"+DlzTestDao.class.getName());
            log.info("init tableCloumnMapper:"+TableColumnMapper.class.getName());
        }
        return dlzDao;
    }

    @Bean(name = "JdbcTemplate")
    public JdbcTemplate JdbcTemplate(DataSource dataSource) {
        DB.Dynamic.setDefaultDataSource(dataSource);
        return new JdbcTemplate(dataSource);
    }

}
