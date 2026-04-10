package com.dlz.test.db.config;

import com.dlz.db.config.DlzDbConfig;
import com.dlz.db.config.DlzDbProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author dk
 * date 2020-10-15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({DlzDbProperties.class})
public class DlzDbConfigs extends DlzDbConfig {

//    @Bean(name = "dlzDao")
//    @Lazy
//    @DependsOn("JdbcTemplate")
//    public IDlzDao dlzDao2(DlzDbProperties properties) {
//        SqlHolder.init(properties);
//        DbLogUtil.init(properties);
//        final IDlzDao dlzDao = new DlzTestDao();
//        DbConvertUtil.tableCloumnMapper= new TableColumnMapper(dlzDao);
//        DBHolder.dao = dlzDao;
//        if(log.isInfoEnabled()){
//            log.info("init test dlzDao:"+DlzTestDao.class.getName());
//            log.info("init tableCloumnMapper:"+TableColumnMapper.class.getName());
//        }
//        return dlzDao;
//    }
//
//    @Bean(name = "JdbcTemplate")
//    public JdbcTemplate JdbcTemplate(DataSource dataSource) {
//        DB.Dynamic.setDefaultDataSource(dataSource);
//        return new JdbcTemplate(dataSource);
//    }

}
