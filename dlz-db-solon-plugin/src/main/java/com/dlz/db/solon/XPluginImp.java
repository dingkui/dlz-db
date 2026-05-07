package com.dlz.db.solon;

import com.dlz.db.convertor.dbtype.TableColumnMapper;
import com.dlz.db.core.ADbProvider;
import com.dlz.db.core.ICacheExecutor;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.helper.support.HelperScan;
import com.dlz.db.holder.DBHolder;
import com.dlz.db.holder.SqlHolder;
import com.dlz.db.modal.DB;
import com.dlz.db.service.ICommService;
import com.dlz.db.service.impl.CommServiceImpl;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.db.util.DbLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import redis.clients.jedis.JedisPool;

import javax.sql.DataSource;

/**
 * Solon 插件入口：通过 Solon SPI 自动加载，将 DLZ-DB 注册到 Solon 容器。
 *
 * <h3>启动流程</h3>
 * <ol>
 *   <li>从配置 {@code dlz.db} 绑定 {@link SolonDbProperties}。</li>
 *   <li>等待用户应用注册 {@link DataSource}，注册到 {@link DB#Dynamic} 默认数据源。</li>
 *   <li>构建 {@link SolonDbProvider} 并写入 {@link DBHolder}。</li>
 *   <li>构建 {@link SolonSqlExecutorAdapter} 与 {@link CommServiceImpl}，注册到 Solon 容器。</li>
 *   <li>触发 {@link SqlHolder#init()} 与 {@link SqlHolder#loadDbSql()}，加载 SQL 资源。</li>
 *   <li>若存在 {@link JedisPool}，自动构建 {@link ICacheExecutor}。</li>
 * </ol>
 *
 * @since 7.0.0
 */
@Slf4j
public class XPluginImp implements Plugin {

    @Override
    public void start(AppContext context) throws Throwable {
        // 1. 绑定配置
        final SolonDbProperties properties = Solon.cfg().getProp("dlz.db").toBean(SolonDbProperties.class);

        // 2. 注册 DbProvider（先于 DataSource，便于其他组件引用）
        SolonDbProvider provider = new SolonDbProvider(properties);
        DBHolder.setDbProvider(provider);
        DbLogUtil.init(properties);
        context.wrapAndPut(ADbProvider.class, provider);
        log.info("init dbProvider: {}", SolonDbProvider.class.getName());

        // 3. 等 DataSource 就绪后初始化 SqlExecutor / CommService
        context.getBeanAsync(DataSource.class, dataSource -> {
            try {
                // 注册到 DB.Dynamic（兼容 SqlHolder/SpringSqlExecutor 等的取数据源逻辑）
                DB.Dynamic.setDefaultDataSource(dataSource);

                // 构建 SqlExecutor
                SolonSqlExecutorAdapter sqlExecutor = new SolonSqlExecutorAdapter();
                DBHolder.sqlExecutor = sqlExecutor;

                // 加载 SQL 资源（依赖 dbProvider.getResourceLoader()）
                SqlHolder.init();
                DbConvertUtil.defaultTableColumnMapper = new TableColumnMapper(sqlExecutor);
                log.info("init sqlExecutor: {}", SolonSqlExecutorAdapter.class.getName());
                log.info("init tableCloumnMapper: {}", TableColumnMapper.class.getName());
                SqlHolder.loadDbSql();

                // 自动更新数据库结构
                if (properties.getHelper().isAutoUpdate()) {
                    log.info("dlzHelper autoUpdate ...");
                    HelperScan.scan(properties.getHelper().getPackageName());
                }

                // 注册到 Solon 容器
                context.wrapAndPut(ISqlExecutor.class, sqlExecutor);

                ICommService commService = new CommServiceImpl(sqlExecutor);
                context.wrapAndPut(ICommService.class, commService);
                log.info("init commService: {}", CommServiceImpl.class.getName());
            } catch (Throwable e) {
                log.error("DLZ-DB 初始化失败", e);
                throw new RuntimeException(e);
            }
        });

        // 4. 可选：JedisPool 就绪 → 注册缓存
        context.getBeanAsync(JedisPool.class, pool -> {
            try {
                ICacheExecutor cache = new SolonJedisCacheAdapter(pool);
                context.wrapAndPut(ICacheExecutor.class, cache);
                log.info("init cacheExecutor: {}", SolonJedisCacheAdapter.class.getName());
            } catch (Throwable e) {
                log.warn("注册缓存执行器失败", e);
            }
        });
    }
}
