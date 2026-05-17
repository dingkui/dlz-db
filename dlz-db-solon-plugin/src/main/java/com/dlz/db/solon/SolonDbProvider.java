package com.dlz.db.solon;

import com.dlz.db.core.ADbProvider;
import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.core.ITxExecutor;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.service.ICommService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;

/**
 * {@link ADbProvider} 的 Solon 实现：从 Solon 容器获取 Bean，配合 ThreadLocal 事务连接。
 *
 * @since 7.0.0
 */
@Slf4j
public class SolonDbProvider extends ADbProvider {

    private final DlzDbProperties sqlConfig;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "配置属性对象由容器注入，视为不可变")
    public SolonDbProvider(DlzDbProperties sqlConfig) {
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
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "配置属性对象视为不可变")
    public DlzDbProperties getSqlConfig() {
        return sqlConfig;
    }
}
