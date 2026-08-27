package com.dlz.db.internal.service;

import com.dlz.db.internal.inf.IExecutorQuery;
import com.dlz.db.model.ResultMap;
import com.dlz.db.wrapper.PojoQuery;
import com.dlz.kit.util.system.ConvertUtil;

/**
 * 从数据库中取得单条map类型数据：{adEnddate=2015-04-08 13:47:12.0}
 * sql语句，可以带参数如：select AD_ENDDATE FROM JOB_AD t WHERE ad_id=#{ad_id}
 * paraMap ：Map<String,Object> m=new HashMap<String,Object>();m.put("ad_id", "47");
 *
  * @throws Exception
 */
public interface IDbOneService extends IDbBaseService {
    default ResultMap getMap(IExecutorQuery paraMap, boolean throwEx) {
        return doDb(paraMap, jdbcSql -> getSqlExecutor().getOne(jdbcSql.sql, throwEx, jdbcSql.paras));
    }

    default <T> T getBean(PojoQuery<T> wrapper, boolean throwEx) {
        return doDb(wrapper, jdbcSql -> ConvertUtil.convert(getSqlExecutor().getOne(jdbcSql.sql, throwEx, jdbcSql.paras),wrapper.getBeanClass()));
    }
    default <T> T getBean(IExecutorQuery paraMap, Class<T> t, boolean throwEx) {
        return doDb(paraMap, jdbcSql -> ConvertUtil.convert(getSqlExecutor().getOne(jdbcSql.sql, throwEx, jdbcSql.paras), t));
    }
}
