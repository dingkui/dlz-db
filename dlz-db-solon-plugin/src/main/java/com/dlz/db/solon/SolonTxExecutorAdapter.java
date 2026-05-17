package com.dlz.db.solon;

import com.dlz.db.core.ITxExecutor;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.exception.DbException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * {@link ITxExecutor} 的 Solon 实现：基于原生 JDBC + {@link SolonConnectionHolder}。
 *
 * <h3>嵌套事务策略</h3>
 * <ul>
 *   <li>同一线程同一 {@link DataSource} 已存在事务时，复用外层连接（加入外层事务），
 *       内层提交/回滚由外层负责。</li>
 *   <li>切换数据源时（嵌套 {@code DB.Dynamic.use(name, ...)}），对新数据源重新开启独立事务。</li>
 * </ul>
 *
 * <h3>异常策略</h3>
 * <p>任何异常触发回滚（最外层事务）。</p>
 *
 * @since 7.0.0
 */
@Slf4j
public class SolonTxExecutorAdapter implements ITxExecutor {

    private final DataSourceConfig config;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "DataSourceConfig由容器注入，视为不可变")
    public SolonTxExecutorAdapter(DataSourceConfig config) {
        this.config = config;
    }

    @Override
    public DataSource getDataSource() {
        return  config.getDataSource();
    }

    @Override
    public boolean hasBinding(DataSource dataSource) {
        if (SolonConnectionHolder.hasBinding(dataSource)) {
            return true;
        }
        // Solon 原生 @Tran 已激活：视为已有事务绑定，
        // 此时 DB.Tx.run 不再开新事务，dlz-db 的 SQL 会通过
        // SolonSqlExecutorAdapter 复用 @Tran 的连接。
        return TranUtilsBridge.AVAILABLE && TranUtilsBridge.inTrans()
                && TranUtilsBridge.getConnection() != null;
    }

    @Override
    public void bind(DataSource dataSource, Connection connection) {
        SolonConnectionHolder.bind(dataSource, connection);
        // 如果 solon-data 可用，也注册到 Solon 事务管理器（从 DB.Dynamic 获取 DataSource）
    }

    @Override
    public void unBind(DataSource dataSource) {
        SolonConnectionHolder.unbind(dataSource);
    }
}
