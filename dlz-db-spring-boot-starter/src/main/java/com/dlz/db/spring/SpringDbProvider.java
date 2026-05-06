package com.dlz.db.spring;

import com.dlz.db.core.*;
import com.dlz.db.core.abstractor.AResourceAdapter;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.service.ICommService;
import com.dlz.db.service.impl.CommServiceImpl;
import com.dlz.spring.holder.SpringHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring 数据库组件提供者实现。
 * <p>整合 Spring 框架下的所有数据库组件获取方式。</p>
 *
 * @since 7.0.0
 */
@Slf4j
public class SpringDbProvider extends ADbProvider {

    private final BaseDbProperties sqlConfig;
    private final IResourceLoader resourceLoader = new AResourceAdapter();
    public SpringDbProvider(BaseDbProperties sqlConfig) {
        this.sqlConfig = sqlConfig;
    }

    @Override
    public ITxExecutor createTxExecutor(DataSourceConfig dataSourceConfig) {
        return new SpringTxExecutorAdapter(dataSourceConfig);
    }

    @Override
    public ISqlExecutor getSqlExecutor() {
        return SpringHolder.getBean(ISqlExecutor.class);
    }

    @Override
    public ICommService getService() {
        return SpringHolder.getBean(ICommService.class);
    }

    @Override
    public ICacheExecutor getCacheExecutor() {
        return SpringHolder.getBean(ICacheExecutor.class);
    }

    @Override
    public IResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public BaseDbProperties getSqlConfig() {
        return sqlConfig;
    }
}
