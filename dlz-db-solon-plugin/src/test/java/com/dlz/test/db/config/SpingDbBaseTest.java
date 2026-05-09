package com.dlz.test.db.config;

import com.dlz.db.inf.ISqlPara;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.para.AParaPojo;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.util.id.TraceUtil;
import com.dlz.test.db.Starter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.noear.solon.Solon;

/**
 * Solon 版测试基类（与 Spring 模块下同名 FQN，保持 case 文件源码兼容）。
 * <p>{@link BeforeClass} 启动 Solon 应用一次（幂等），等价于 Spring 的
 * {@code @RunWith(SpringRunner.class) + @SpringBootTest}。</p>
 */
@Slf4j
public class SpingDbBaseTest {

    @BeforeAll
    public static void bootstrap() {
        if (Solon.app() == null) {
            Solon.start(Starter.class, new String[0]);
            // 在这里添加其他全局初始化逻辑
            // 例如：初始化测试数据、清理环境等
            log.info("Solon 应用启动完成，全局初始化完毕");
        }
    }

    @Before
    public void before() {
        if (TraceUtil.getTraceid() == null) {
            TraceUtil.setTraceId();
        }
    }

    private String clearSql(String sql) {
        return sql.replaceAll("[\\s]+", " ").trim();
    }

    public void showSql(ISqlPara paraMap, String fn, String re) {
        log.debug("-------------------  {}  -------------------", fn);
        JdbcItem jdbcSql = paraMap.jdbcSql();
        String runSqlByJdbc = SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
        if (re == null) {
            log.info(runSqlByJdbc);
        } else if (clearSql(re).equalsIgnoreCase(clearSql(runSqlByJdbc))) {
            log.info("ok:{}", runSqlByJdbc);
        } else {
            log.error("error:{}", runSqlByJdbc);
            log.error("target:{}", re);
            assert false;
        }
    }

    public String toSql(AParaPojo wrapper) {
        JdbcItem jdbcSql = wrapper.jdbcSql();
        return SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
    }
}
