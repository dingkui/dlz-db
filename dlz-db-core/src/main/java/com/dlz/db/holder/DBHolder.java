package com.dlz.db.holder;

import com.dlz.db.core.*;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.service.ICommService;
import com.dlz.kit.exception.SystemException;
import com.dlz.kit.util.StringUtils;
import com.dlz.kit.util.ValUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * 数据库配置信息
 */
@Slf4j
public class DBHolder {
    public static ISqlExecutor sqlExecutor;
    private static ICommService service;
    private static ICacheExecutor cacheExecutor;
    private static BaseDbProperties properties;
    public static ADbProvider dbProvider;

    public static ISqlExecutor getSqlExecutor() {
        if (sqlExecutor == null && dbProvider != null) {
            sqlExecutor = dbProvider.getSqlExecutor();
        }
        if (sqlExecutor == null) {
            throw new SystemException("sqlExecutor is null");
        }
        return sqlExecutor;
    }

    public static ICommService getService() {
        if (service == null) {
            if (dbProvider != null) {
                service = dbProvider.getService();
            }
            if (service == null) {
                throw new SystemException("service is null");
            }
            if (log.isInfoEnabled()) {
                log.debug("init commService:" + service.getClass().getName());
            }
        }

        return service;
    }

    public static ICacheExecutor getCacheExecutor() {
        if (cacheExecutor == null && dbProvider != null) {
            cacheExecutor = dbProvider.getCacheExecutor();
        }
        return cacheExecutor;
    }

    public static ITxExecutor getTxExecutor(DataSourceConfig dataSourceConfig) {
        return dbProvider.createTxExecutor(dataSourceConfig);
    }


    public static void setDbProvider(ADbProvider provider) {
        DBHolder.dbProvider = provider;
    }

    public static BaseDbProperties getSqlConfig() {
        if (properties == null && dbProvider != null) {
            properties = dbProvider.getSqlConfig();
        }
        return properties;
    }

    public static long sequence(String tableName, long initSeq) {
        String key = "seq:" + tableName;
        final ICacheExecutor cacheExecutor = getCacheExecutor();
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

    public static <R> R doDb(Function<ICommService, R> s) {
        return s.apply(getService());
    }

    public static <R> R doDb(Function<ICommService, R> s, boolean clearMappers) {
        if (clearMappers) {
            try {
                return s.apply(getService());
            } finally {
                SqlRunThreadHolder.removeColumnNameConvertor();
                SqlRunThreadHolder.removeConvertorToFieldName();
                SqlRunThreadHolder.removeTableColumnMapper();
            }
        }
        return s.apply(getService());
    }

    public static <R> R doDao(Function<ISqlExecutor, R> s) {
        return s.apply(getSqlExecutor());
    }
}
