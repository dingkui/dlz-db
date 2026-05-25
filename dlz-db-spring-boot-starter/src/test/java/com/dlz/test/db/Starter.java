package com.dlz.test.db;

import com.dlz.test.db.config.DlzDbConfigs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.atomic.AtomicInteger;

@ConfigurationPropertiesScan
@SpringBootApplication()
@Import({DlzDbConfigs.class})
public class Starter implements ApplicationListener<ContextRefreshedEvent> {
    private static final AtomicInteger CONTEXT_REFRESH_COUNT = new AtomicInteger(0);

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        int count = CONTEXT_REFRESH_COUNT.incrementAndGet();
        System.out.println("Spring 容器启动完成，第 " + count + " 次");
    }

    public static int getContextRefreshCount() {
        return CONTEXT_REFRESH_COUNT.get();
    }
}