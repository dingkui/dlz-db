package com.dlz.test.db.config;

import com.dlz.db.core.ITxExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Supplier;

/**
 * 测试用事务执行器 - 空实现
 * 测试环境不支持真实事务
 */
public class MockTxExecutor implements ITxExecutor {

    @Override
    public <T> T execute(Supplier<T> task) throws Exception {
        // 测试环境直接执行，不开启事务
        return task.get();
    }

    @Override
    public DataSource getDataSource() {
        return null;
    }

    @Override
    public boolean hasBinding(DataSource dataSource) {
        return false;
    }

    @Override
    public void bind(DataSource dataSource, Connection connection) {

    }

    @Override
    public void unBind(DataSource dataSource) {

    }
}
