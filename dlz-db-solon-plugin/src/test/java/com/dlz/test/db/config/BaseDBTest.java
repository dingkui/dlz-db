package com.dlz.test.db.config;

import com.dlz.db.inf.ISqlPara;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.para.AParaPojo;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.util.id.TraceUtil;
import com.dlz.test.db.Starter;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;  // JUnit 4
import org.junit.jupiter.api.BeforeAll;  // JUnit 5
import org.noear.solon.Solon;

/**
 * Solon 版测试基类（与 Spring 模块下同名 FQN，保持 case 文件源码兼容）。
 * <p>同时支持 JUnit 4 和 JUnit 5：</p>
 * <ul>
 *   <li>JUnit 4: 使用 {@link BeforeClass}</li>
 *   <li>JUnit 5: 使用 {@link BeforeAll}</li>
 * </ul>
 * <p>启动 Solon 应用一次（幂等），等价于 Spring 的
 * {@code @RunWith(SpringRunner.class) + @SpringBootTest}。</p>
 */
@Slf4j
public class BaseDBTest {

    /**
     * JUnit 5 初始化入口
     */
    @BeforeAll
    public static void bootstrapJunit5() {
        bootstrap();
    }

    /**
     * JUnit 4 初始化入口
     */
    @BeforeClass
    public static void bootstrapJunit4() {
        bootstrap();
    }

    /**
     * 统一的初始化逻辑（幂等）
     */
    private static void bootstrap() {
        if (Solon.app() == null) {
            Solon.start(Starter.class, new String[0]);
            // 在这里添加其他全局初始化逻辑
            // 例如：初始化测试数据、清理环境等
            log.info("Solon 应用启动完成，全局初始化完毕");
        }
    }

    @Before
    public void before() {
        TraceUtil.setTraceId(this.getClass().getSimpleName());
    }

    @After
    public void after() {
        TraceUtil.clearTraceId();
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
