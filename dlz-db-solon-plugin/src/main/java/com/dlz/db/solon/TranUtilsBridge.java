package com.dlz.db.solon;

import com.dlz.db.modal.DB;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 桥接 Solon-data 的 {@code TranUtils}，懒加载隔离 ClassNotFoundException。
 *
 * <p>当用户应用引入 {@code solon-data} 后：</p>
 * <ul>
 *   <li>{@link #inTrans()} 返回 Solon 原生 {@code @Tran} 是否激活</li>
 *   <li>{@link #getConnection()} 返回 Solon 事务上下文中的连接（从 Dynamic 获取 DataSource）</li>
 *   <li>{@link #tryBind(Connection)} 尝试将连接绑定到 Solon 事务管理器（从 Dynamic 获取 DataSource）</li>
 * </ul>
 *
 * @since 7.0.0
 */
@Slf4j
final class TranUtilsBridge {

    /** solon-data 是否在 classpath 中。 */
    static final boolean AVAILABLE;

    static {
        boolean ok = false;
        try {
            Class.forName("org.noear.solon.data.tran.TranUtils");
            ok = true;
        } catch (ClassNotFoundException ignored) {
            // solon-data 未引入，纯 dlz-db 事务模式
        }
        AVAILABLE = ok;
    }

    private TranUtilsBridge() {
    }

    /** 当前线程是否处于 Solon @Tran 事务中。 */
    static boolean inTrans() {
        if (!AVAILABLE) return false;
        try {
            return org.noear.solon.data.tran.TranUtils.inTrans();
        } catch (Throwable e) {
            log.warn("TranUtils.inTrans() 调用失败", e);
            return false;
        }
    }

    /**
     * 返回 Solon @Tran 当前持有的连接（从 DB.Dynamic 获取 DataSource）。
     * 未持有返回 null。
     */
    static Connection getConnection() {
        if (!AVAILABLE) return null;
        try {
            DataSource ds = DB.ds.getDataSource();
            return org.noear.solon.data.tran.TranUtils.getConnection(ds);
        } catch (Throwable e) {
            log.warn("TranUtils.getConnection() 调用失败", e);
            return null;
        }
    }
}
