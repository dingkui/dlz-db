package com.dlz.test.db.config;

import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.service.ICommService;

/**
 * 测试用 CommService - 委托给 MockSqlExecutor
 */
public class MockCommService implements ICommService {

    private final ISqlExecutor sqlExecutor;

    public MockCommService(ISqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    // ICommService 继承自多个接口，这里提供基础实现
    // 实际使用时，会通过 DBHolder.doDb() 调用
    
    @Override
    public ISqlExecutor getSqlExecutor() {
        return sqlExecutor;
    }
}
