package com.dlz.test.config;

import com.dlz.db.support.DBHolder;
import com.dlz.kit.util.id.TraceUtil;
import com.dlz.test.db.Starter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Starter.class)
@Slf4j
public abstract class BaseDBTest {
    @BeforeEach
    public void before() {
        DBHolder.getSqlExecutor();
        TraceUtil.setTraceId(this.getClass().getSimpleName());
    }
    @AfterEach
    public void after() {
        TraceUtil.clearTraceId();
    }
}
