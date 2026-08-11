package com.dlz.test.db.config;

import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.core.jdbc.JdbcSqlExecutor;
import com.dlz.db.core.jdbc.JdbcTxExecutor;
import com.dlz.db.inf.ISqlPara;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.para.AParaPojo;
import com.dlz.db.support.DBHolder;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.mdc.DlzTrace;
import com.dlz.kit.mdc.MdcContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试基类 - 自动初始化 MockDbProvider
 * 所有需要数据库功能的测试类继承此类即可
 */
@Slf4j
@ExtendWith(TraceExtension.class)
public abstract class BaseDBTest {

    /**
     * 初始化入口（幂等）
     */
    @BeforeAll
    public static void bootstrap() {
        initMockDb();
    }

    static boolean init = false;



    static void initMockDb() {
        if (!init) {
            synchronized (BaseDBTest.class) {
                if (!init) {
                    try (final MdcContext ignore = DlzTrace.trace("initMockDb")){
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

                        DBHolder.getSqlExecutor();

                        log.info("---------------------------------------------------------");
                        init = true;
                    }
                }
            }
        }
    }


    private String clearSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    public void showSql(ISqlPara paraMap, String fn, String re) {
        JdbcItem jdbcSql = paraMap.jdbcSql();
        String runSqlByJdbc = SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
        if (re == null) {
            log.info(runSqlByJdbc);
        } else if (clearSql(re).equalsIgnoreCase(clearSql(runSqlByJdbc))) {
            log.info("{} success:{}", fn, runSqlByJdbc);
        } else {
            log.error("{} error:{}", fn, runSqlByJdbc);
            log.error("{} target:{}", fn, re);
            assert false;
        }
    }

    public String toSql(AParaPojo wrapper) {
        JdbcItem jdbcSql = wrapper.jdbcSql();
        return SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
    }
}
