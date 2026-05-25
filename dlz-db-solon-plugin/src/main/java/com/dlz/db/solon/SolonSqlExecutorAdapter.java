package com.dlz.db.solon;

import com.dlz.db.core.DlzConnectionHolder;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.core.anno.ConnectionSupplier;
import com.dlz.db.core.jdbc.JdbcSqlExecutor;
import com.dlz.db.modal.DB;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * {@link ISqlExecutor} 的 Solon 实现：自研 SimpleJdbc，不依赖任何外部框架的事务管理。
 *
 * <h3>连接来源</h3>
 * <ol>
 *   <li>当前线程数据源由 {@link DB#Dynamic} 决定。</li>
 *   <li>若 {@link SolonConnectionHolder} 中存在该数据源的事务连接，则复用且执行后<b>不</b>关闭。</li>
 *   <li>若 Solon @Tran 事务中存在该数据源的连接，则复用。</li>
 *   <li>否则新开连接，执行后立即关闭（短连接模式）。</li>
 * </ol>
 *
 * @since 7.0.0
 */
@Slf4j
@Component
public class SolonSqlExecutorAdapter extends JdbcSqlExecutor {

    @Override
    public ConnectionSupplier getConnectionSupplier() {
        return () -> {
            DataSource ds = DB.Dynamic.getDataSource();
            // 1. 优先复用 dlz-db 自身事务连接（DB.Tx.run）
            Connection bound = DlzConnectionHolder.get(ds);
            if (bound != null) {
                log.debug("复用 dlz-db 自身事务连接（需要 wrapNoClose）");
                return wrapNoClose(bound);
            }
            // 2. 复用 Solon 原生 @Tran 事务连接
            if (TranUtilsBridge.AVAILABLE && TranUtilsBridge.inTrans()) {
                Connection solonConn = TranUtilsBridge.getConnection();
                if (solonConn != null) {
                    log.debug("复用 Solon @Tran 事务连接（需要 wrapNoClose）");
                    return wrapNoClose(solonConn);
                }
            }
            // 3. 获取新连接
            log.debug("获取新连接");
            return ds.getConnection();
        };
    }
}
