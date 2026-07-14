package com.dlz.db.solon;

import com.dlz.db.modal.DB;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.SqlHolder;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

import javax.sql.DataSource;

/**
 * Solon 插件入口：通过 Solon SPI 自动加载，将 DLZ-DB 注册到 Solon 容器。
 *
 * <h3>启动流程</h3>
 * <ol>
 *   <li>从配置 {@code dlz.db} 绑定 {@link SolonDbProperties}。</li>
 *   <li>等待用户应用注册 {@link DataSource}，注册到 {@link DB#ds} 默认数据源。</li>
 *   <li>触发 {@link SqlHolder#init()} 与 {@link SqlHolder#loadDbSql()}，加载 SQL 资源。</li>
 * </ol>
 *
 * @since 7.0.0
 */
@Slf4j
public class DlzDbSolonPlugin implements Plugin {

    @Override
    public void start(AppContext context) throws Throwable {
        // 1. 绑定配置
        final SolonDbProperties properties = Solon.cfg().getProp("dlz.db").toBean(SolonDbProperties.class);

        // 3. 等 DataSource 就绪后初始化 SqlExecutor / CommService
        context.getBeanAsync(DataSource.class, dataSource -> {
            try {
                // 使用 DynamicDataSource 包装原始 DataSource（类似 Spring 的 DynamicJdbcTemplate）
                DynamicDataSource dynamicDataSource = new DynamicDataSource(dataSource);

                // 注册到 Solon 容器，替换原始 DataSource
                context.wrapAndPut(DataSource.class, dynamicDataSource);


                // 2. 注册 DbProvider（先于 DataSource，便于其他组件引用）
                DBHolder.init(properties, () -> context.getBean(DataSource.class), SolonSqlExecutorAdapter::new, SolonTxExecutorAdapter::new);
            } catch (Throwable e) {
                log.error("DLZ-DB 初始化失败", e);
                throw new RuntimeException(e);
            }
        });
    }
}
