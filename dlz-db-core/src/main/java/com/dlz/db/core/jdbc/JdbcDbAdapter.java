package com.dlz.db.core.jdbc;

import com.dlz.db.convertor.dbtype.TableColumnMapper;
import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.core.ADbProvider;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.core.ITxExecutor;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.modal.DB;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.SqlHolder;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.db.util.DbLogUtil;
import com.dlz.kit.exception.SystemException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

/**
 * 测试用 SQLite 数据库提供者 - 使用真实 SQLite 数据库
 * 用于单元测试，提供真实的数据库连接
 */
@Slf4j
public class JdbcDbAdapter extends ADbProvider {

    private final JdbcSqlExecutor sqlExecutor;
    private final DlzDbProperties sqlConfig;
    private final DataSource dataSource;
    private final Class<? extends ITxExecutor> txExecutorClass;

    private JdbcDbAdapter(DlzDbProperties sqlConfig,
                          DataSource dataSource,
                          Class<? extends ISqlExecutor> sqlExecutorClass,
                          Class<? extends ITxExecutor> txExecutorClass) {
        // 创建 SQLite 数据源
        this.dataSource = dataSource;

        // 初始化配置
        this.sqlConfig = sqlConfig;

        // 初始化 SQL 执行器
        try {
            this.sqlExecutor = (JdbcSqlExecutor) sqlExecutorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new SystemException("执行器创建失败创建 SQL 执行器失败", e);
        }

        this.txExecutorClass = txExecutorClass;

        DB.Dynamic.setDefaultDataSource(dataSource);

        DBHolder.setDbProvider(this);

        DbLogUtil.init(sqlConfig);
    }

    public static void init(DlzDbProperties sqlConfig,
                            DataSource dataSource,
                            Class<? extends ISqlExecutor> sqlExecutorClass,
                            Class<? extends ITxExecutor> txExecutorClass) {
        final JdbcDbAdapter jdbcDbAdapter = new JdbcDbAdapter(sqlConfig, dataSource, sqlExecutorClass, txExecutorClass);


        DB.Dynamic.setDefaultDataSource(dataSource);

        DBHolder.setDbProvider(jdbcDbAdapter);

        DbLogUtil.init(sqlConfig);

        SqlHolder.init();
        DbConvertUtil.defaultTableColumnMapper = new TableColumnMapper(jdbcDbAdapter.sqlExecutor);

        log.info("sqlExecutor: {}", sqlExecutorClass.getName());
        log.info("txExecutor: {}", txExecutorClass.getName());
        SqlHolder.loadDbSql();
    }


    @Override
    public ITxExecutor createTxExecutor(DataSourceConfig dataSourceConfig) {
        try {
            return txExecutorClass.getDeclaredConstructor(DataSource.class).newInstance(dataSourceConfig.getDataSource());
        } catch (Exception e) {
            throw new SystemException("事务执行器创建失败", e);
        }
        // 对于 SQLite，我们提供一个简单的事务执行器
//        return new JdbcTxExecutor(dataSourceConfig.getDataSource());
    }

    @Override
    public ISqlExecutor getSqlExecutor() {
        return new JdbcSqlExecutor();
    }


    @Override
    public DlzDbProperties getSqlConfig() {
        return sqlConfig;
    }
}