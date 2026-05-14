package com.dlz.test.db.config;

import com.dlz.db.core.*;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.service.ICommService;

/**
 * 测试用数据库提供者 - 基于内存数据存储
 * 用于单元测试，无需真实数据库连接
 */
public class MockDbProvider extends ADbProvider {

    private final MockSqlExecutor sqlExecutor;
    private final MockCommService commService;
    private final MockCacheExecutor cacheExecutor;
    private final BaseDbProperties sqlConfig;

    public MockDbProvider() {
        this.sqlExecutor = new MockSqlExecutor();
        this.commService = new MockCommService(sqlExecutor);
        this.cacheExecutor = new MockCacheExecutor();
        this.sqlConfig = createDefaultConfig();
        
        // 初始化测试数据
        sqlExecutor.initTestData();
    }

    private BaseDbProperties createDefaultConfig() {
        BaseDbProperties config = new BaseDbProperties();
        config.setLogicDeleteField("IS_DELETED");
        config.setTableCacheTime(-1);
        
        BaseDbProperties.Log logConfig = new BaseDbProperties.Log();
        logConfig.setShowResult(false);
        logConfig.setShowRunSql(false);
        logConfig.setShowCaller(false);
        logConfig.setSlowSqlThreshold(0L);
        config.setLog(logConfig);
        
        return config;
    }

    @Override
    public ITxExecutor createTxExecutor(DataSourceConfig dataSourceConfig) {
        // 测试环境不支持事务，返回空实现
        return new MockTxExecutor();
    }

    @Override
    public ISqlExecutor getSqlExecutor() {
        return sqlExecutor;
    }

    @Override
    public ICommService getService() {
        return commService;
    }

    @Override
    public ICacheExecutor getCacheExecutor() {
        return cacheExecutor;
    }

    @Override
    public BaseDbProperties getSqlConfig() {
        return sqlConfig;
    }
}
