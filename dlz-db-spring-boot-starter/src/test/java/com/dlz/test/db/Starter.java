package com.dlz.test.db;

//import com.dlz.db.DbInfo;

import com.dlz.db.holder.DBHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {"com.dlz.spring", "com.dlz.test.db.config"})
public class Starter implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
    public void afterPropertiesSet() throws Exception{
        // afterPropertiesSet 执行时，依赖的 Bean 已经初始化
        DBHolder.getSqlExecutor();
//        String sql = DbInfo.getSql("key.setting.getSettings");
//        System.out.println(sql);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 容器刷新完成后执行，所有 Bean 都已初始化
        System.out.println("Spring 容器启动完成");
    }
}