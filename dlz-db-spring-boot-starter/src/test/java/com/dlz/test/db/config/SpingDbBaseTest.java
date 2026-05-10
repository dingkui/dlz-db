package com.dlz.test.db.config;

import com.dlz.db.inf.ISqlPara;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.para.AParaPojo;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.util.id.TraceUtil;
import com.dlz.test.db.Starter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class)
@Slf4j
public abstract class SpingDbBaseTest {
    @Before
    public void before(){
        if(TraceUtil.getTraceid()==null){
            TraceUtil.setTraceId();
        }
    }

    private String clearSql(String sql){
        return sql.replaceAll("\\s+"," ").trim();
    }
    public void showSql(ISqlPara paraMap, String fn, String re) {
        JdbcItem jdbcSql = paraMap.jdbcSql();
        String runSqlByJdbc = SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
        if(re==null){
            log.info(runSqlByJdbc);
        }else if(clearSql(re).equalsIgnoreCase(clearSql(runSqlByJdbc))){
            log.info("sucess:"+runSqlByJdbc);
        }else{
            log.error("error:"+runSqlByJdbc);
            log.error("target:"+re);
            assert false;
        }
    }
    public void showSql(ParaMap paraMap, String fn) {
        showSql(paraMap, fn, null);
    }
    public void showSql(AParaPojo wrapper, String fn) {
        showSql(wrapper, fn, null);
    }
}