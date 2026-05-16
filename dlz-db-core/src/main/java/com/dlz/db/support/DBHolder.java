package com.dlz.db.support;

import com.dlz.db.core.*;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.service.ICommService;
import com.dlz.db.service.impl.CommServiceImpl;
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
    private static IRedisExecutor cacheExecutor;
    private static DlzDbProperties properties;
    public static ADbProvider dbProvider;
    private static SegmentIdGenerator segmentIdGenerator = new SegmentIdGenerator(1000);

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
                service = new CommServiceImpl(getSqlExecutor());
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

    public static IRedisExecutor getCacheExecutor() {
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

    public static DlzDbProperties getSqlConfig() {
        if (properties == null && dbProvider != null) {
            properties = dbProvider.getSqlConfig();
        }
        return properties;
    }

    public static long sequence(String tableName, long initSeq) {
        return segmentIdGenerator.nextId(tableName, initSeq);
    }
    public static long sequence(String tableName) {
        return sequence(tableName, 1);
    }

    public static <R> R doDb(Function<ICommService, R> s) {
        try {
            return s.apply(getService());
        } finally {
            SqlRunThreadHolder.removeColumnNameConvertor();
            SqlRunThreadHolder.removeConvertorToFieldName();
            SqlRunThreadHolder.removeTableColumnMapper();
        }
    }

    public static <R> R doDao(Function<ISqlExecutor, R> s) {
        return s.apply(getSqlExecutor());
    }
}
