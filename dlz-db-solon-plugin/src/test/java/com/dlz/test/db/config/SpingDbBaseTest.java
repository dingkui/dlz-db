package com.dlz.test.db.config;

import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.items.SqlItem;
import com.dlz.db.modal.para.AParaPojo;
import com.dlz.db.modal.para.ParaJdbc;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.util.ValUtil;
import com.dlz.kit.util.id.TraceUtil;
import com.dlz.kit.util.system.FieldReflections;
import com.dlz.test.db.Starter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.noear.solon.Solon;

/**
 * Solon 版测试基类（与 Spring 模块下同名 FQN，保持 case 文件源码兼容）。
 * <p>{@link BeforeClass} 启动 Solon 应用一次（幂等），等价于 Spring 的
 * {@code @RunWith(SpringRunner.class) + @SpringBootTest}。</p>
 */
@Slf4j
public class SpingDbBaseTest {

    @BeforeClass
    public static void bootstrap() {
        if (Solon.app() == null) {
            Solon.start(Starter.class, new String[0]);
        }
    }

    @Test
    public void db() {
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

    public void showSql(ParaMap paraMap, String fn, String re) {
        log.debug("-------------------  " + fn + "  -------------------");
        log.debug(ValUtil.toStr(paraMap));
        JdbcItem jdbcSql = paraMap.jdbcSql();
        SqlItem sqlItem = paraMap.getSqlItem();
        log.debug(sqlItem.toString());
        log.debug(paraMap.getPara().toString());
        String runSqlByJdbc = SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
        if (re == null) {
            log.info(runSqlByJdbc);
        } else if (clearSql(re).equalsIgnoreCase(clearSql(runSqlByJdbc))) {
            log.info("sucess:" + runSqlByJdbc);
        } else {
            log.error("error:" + runSqlByJdbc);
            log.error("target:" + re);
            assert false;
        }
    }

    public void showSql(ParaJdbc paraMap, String fn, String re) {
        log.debug("-------------------  " + fn + "  -------------------");
        log.debug(ValUtil.toStr(paraMap));
        JdbcItem jdbcSql = paraMap.jdbcSql();
        String runSqlByJdbc = SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
        if (re == null) {
            log.info(runSqlByJdbc);
        } else if (clearSql(re).equals(clearSql(runSqlByJdbc))) {
            log.info("sucess:" + runSqlByJdbc);
        } else {
            log.error("error:" + runSqlByJdbc);
            log.error("target:" + re);
            assert false;
        }
    }

    public void showSql(ParaMap paraMap, String fn) {
        showSql(paraMap, fn, null);
    }

    public void showSql(AParaPojo wrapper, String fn, String re) {
        wrapper.jdbcSql();
        ParaMap paraMap = FieldReflections.getValue(wrapper, "pm", false);
        showSql(paraMap, fn, re);
    }

    public void showSql(AParaPojo wrapper, String fn) {
        showSql(wrapper, fn, null);
    }
}
