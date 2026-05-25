package com.dlz.test.db.config;

import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.core.jdbc.JdbcSqlExecutor;
import com.dlz.db.core.jdbc.JdbcTxExecutor;
import com.dlz.db.support.DBHolder;
import com.dlz.kit.util.id.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试基类 - 自动初始化 MockDbProvider
 * 所有需要数据库功能的测试类继承此类即可
 */
@Slf4j
public abstract class BaseDBTest {

    /**
     * JUnit 5 初始化入口
     */
    @BeforeAll
    public static void bootstrapJunit5() {
        initMockDb();
    }

    /**
     * JUnit 4 初始化入口
     */
    @BeforeClass
    public static void bootstrapJunit4() {
        initMockDb();
    }

    static boolean init = false;


    static void initMockDb() {
        if (!init) {
            synchronized (BaseDBTest.class) {
                if (!init) {
                    TraceUtil.setTraceId("initMockDb");
                    final DlzDbProperties testConfig = SqliteDbProviderUtil.createTestConfig();
                    List<String> sqlList = new ArrayList<>();
                    sqlList.add("app/*");
                    sqlList.add("demo/*");
                    testConfig.setSqllist(sqlList);
                    testConfig.getHelper().setPackageName("com.dlz.test.db.entity");
                    testConfig.getHelper().setAutoUpdate(true);

                    // 设置 MockDbProvider
                    DBHolder.init(testConfig,
                            SqliteDbProviderUtil::createSqliteDataSource,
                            JdbcSqlExecutor::new,
                            JdbcTxExecutor::new);

                    log.info("---------------------------------------------------------");
                    TraceUtil.clearTraceId();
                    init = true;
                }
            }
        }
    }


    @BeforeEach
    public void before() {
        TraceUtil.setTraceId(this.getClass().getSimpleName());
    }

    @AfterEach
    public void after() {
        TraceUtil.clearTraceId();
    }
}
