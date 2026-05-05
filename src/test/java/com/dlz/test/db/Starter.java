package com.dlz.test.db;

//import com.dlz.db.DbInfo;

import com.dlz.db.holder.DBHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {"com.dlz.spring", "com.dlz.test.db.config"})
public class Starter implements InitializingBean {
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
    public void afterPropertiesSet() throws Exception{
        DBHolder.getSqlExecutor();
//        String sql = DbInfo.getSql("key.setting.getSettings");
//        System.out.println(sql);
    }
}