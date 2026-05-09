package com.dlz.db.solon;

import com.dlz.db.core.*;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.service.ICommService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;

/**
 * {@link ADbProvider} 的 Solon 实现：从 Solon 容器获取 Bean，配合 ThreadLocal 事务连接。
 *
 * @since 7.0.0
 */
@Slf4j
public class SolonDbProvider extends ADbProvider {

    private final BaseDbProperties sqlConfig;

    public SolonDbProvider(BaseDbProperties sqlConfig) {
        this.sqlConfig = sqlConfig;
    }

    @Override
    public ITxExecutor createTxExecutor(DataSourceConfig dataSourceConfig) {
        return new SolonTxExecutorAdapter(dataSourceConfig);
    }

    @Override
    public ISqlExecutor getSqlExecutor() {
        return Solon.context().getBean(ISqlExecutor.class);
    }

    @Override
    public ICommService getService() {
        return Solon.context().getBean(ICommService.class);
    }

    @Override
    public ICacheExecutor getCacheExecutor() {
        return Solon.context().getBean(ICacheExecutor.class);
    }

    @Override
    public BaseDbProperties getSqlConfig() {
        return sqlConfig;
    }
}
