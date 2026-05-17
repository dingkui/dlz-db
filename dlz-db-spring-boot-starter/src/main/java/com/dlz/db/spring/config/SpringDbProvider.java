package com.dlz.db.spring.config;

import com.dlz.db.core.ADbProvider;
import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.core.ITxExecutor;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.spring.SpringTxExecutorAdapter;
import com.dlz.spring.holder.SpringHolder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring 数据库组件提供者实现。
 * <p>整合 Spring 框架下的所有数据库组件获取方式。</p>
 *
 * @since 7.0.0
 */
@Slf4j
public class SpringDbProvider extends ADbProvider {

    private final DlzDbProperties sqlConfig;
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "配置属性对象由Spring容器注入，视为不可变")
    public SpringDbProvider(DlzDbProperties sqlConfig) {
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
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "配置属性对象视为不可变")
    public DlzDbProperties getSqlConfig() {
        return sqlConfig;
    }
}
