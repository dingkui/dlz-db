package com.dlz.db.holder;

import com.dlz.db.core.CacheExecutor;
import com.dlz.db.core.SqlExecutor;
import com.dlz.db.service.ICommService;
import com.dlz.db.service.impl.CommServiceImpl;
import com.dlz.kit.util.StringUtils;
import com.dlz.kit.util.ValUtil;
import com.dlz.spring.holder.SpringHolder;
import com.dlz.spring.redis.excutor.JedisExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * 数据库配置信息
 */
@Slf4j
public class DBHolder {
    public static SqlExecutor sqlExecutor;
    public static ICommService service;
    public static CacheExecutor cacheExecutor = new com.dlz.db.core.NoOpCacheExecutor();

    public static SqlExecutor getSqlExecutor() {
        if (sqlExecutor == null) {
            sqlExecutor = SpringHolder.getBean(SqlExecutor.class);
        }
        return sqlExecutor;
    }

    public static ICommService getService() {
        if (service == null) {
            service = SpringHolder.registerBean(CommServiceImpl.class);
            if (log.isInfoEnabled()) {
                log.debug("commService:" + CommServiceImpl.class.getName());
            }
        }
        return service;
    }

    public static void setCacheExecutor(CacheExecutor executor) {
        DBHolder.cacheExecutor = executor;
    }

    public static CacheExecutor getCacheExecutor() {
        return cacheExecutor;
    }

    public static long sequence(String tableName, long initSeq) {
        String key = "seq:" + tableName;
        Long seq = cacheExecutor.incrBy(key, initSeq);
        if (seq == initSeq) {
            try {
                final String fistColumn = getSqlExecutor().getFistColumn("select max(id) from " + tableName, String.class);
                if (fistColumn == null || !StringUtils.isNumber(fistColumn)) {
                    return seq;
                }
                seq = ValUtil.toBigDecimalZero(fistColumn).longValue() + initSeq;
                if (seq > initSeq) {
                    cacheExecutor.set(key, seq.toString());
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return seq;
    }

    public static long sequence(Class<?> beanClass, long initSeq) {
        return sequence(BeanInfoHolder.getTableName(beanClass), initSeq);
    }

    public static <R> R doDb(Function<ICommService, R> s) {
        return s.apply(getService());
    }

    public static <R> R doDb(Function<ICommService, R> s,boolean clearMappers) {
        if(clearMappers){
            try {
                return s.apply(getService());
            }finally {
                SqlRunThreadHolder.removeColumnNameConvertor();
                SqlRunThreadHolder.removeConvertorToFieldName();
                SqlRunThreadHolder.removeTableColumnMapper();
            }
        }
        return s.apply(getService());
    }

    public static <R> R doDao(Function<SqlExecutor, R> s) {
        return s.apply(getSqlExecutor());
    }
}
