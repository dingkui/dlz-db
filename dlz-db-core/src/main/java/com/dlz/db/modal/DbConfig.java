package com.dlz.db.modal;

import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.core.ITxExecutor;
import com.dlz.db.enums.DbTypeEnum;
import com.dlz.db.exception.DbParameterException;
import com.dlz.db.interceptor.DbPlugin;
import com.dlz.db.interceptor.SqlBuildInterceptor;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.SqlHolder;

import javax.sql.DataSource;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DbConfig {
    private enum State { NEW, INITIALIZING, READY, CLOSED }

    private final DlzDbProperties properties = new DlzDbProperties();
    private State state = State.NEW;
    private DataSource dataSource;
    private ISqlExecutor sqlExecutor;
    private Supplier<DataSource> dataSourceMaker;
    private Supplier<ISqlExecutor> sqlExecutorMaker;
    private Function<DataSource, ITxExecutor> txExecutorMaker;

    public synchronized DbConfig plugin(SqlBuildInterceptor plugin) {
        ensureMutable();
        require(plugin);
        DbPlugin.registerInterceptor(plugin);
        return this;
    }

    public synchronized DbConfig dialect(DbTypeEnum dialect) {
        ensureMutable();
        if (dialect == null) throw new DbParameterException("dialect must not be null");
        properties.setDbSupport(dialect.name());
        return this;
    }

    public synchronized DbConfig sql(String key, String sql) {
        ensureMutable();
        if (key == null || key.trim().isEmpty() || sql == null || sql.trim().isEmpty()) {
            throw new DbParameterException("sql key and value must not be empty");
        }
        SqlHolder.addSqlSetting(key.startsWith("key.") ? key : "key." + key, sql, true);
        return this;
    }

    public synchronized DbConfig logicDeleteField(String field) {
        ensureMutable();
        if (field == null || field.trim().isEmpty()) throw new DbParameterException("field must not be empty");
        properties.setLogicDeleteField(field);
        return this;
    }

    public synchronized DbConfig columnNameConvertor(Function<String, String> convertor) {
        ensureMutable();
        require(convertor);
        return this;
    }

    public synchronized DbConfig init(DlzDbProperties props,
                                      Supplier<DataSource> dataSourceMaker,
                                      Supplier<ISqlExecutor> sqlExecutorMaker,
                                      Function<DataSource, ITxExecutor> txExecutorMaker) {
        if (props == null || dataSourceMaker == null || sqlExecutorMaker == null || txExecutorMaker == null) {
            throw new DbParameterException("configuration arguments must not be null");
        }
        if (state == State.READY) return this;
        if (state != State.NEW) throw new DbParameterException("configuration is not initializable: " + state);
        state = State.INITIALIZING;
        try {
            DBHolder.init(props, dataSourceMaker, sqlExecutorMaker, txExecutorMaker);
            state = State.READY;
        } catch (RuntimeException e) {
            state = State.NEW;
            throw e;
        }
        return this;
    }

    public synchronized DbConfig dataSource(DataSource dataSource) {
        ensureMutable();
        require(dataSource);
        this.dataSource = dataSource;
        this.dataSourceMaker = () -> this.dataSource;
        return this;
    }

    public synchronized DbConfig sqlExecutor(ISqlExecutor executor) {
        ensureMutable();
        require(executor);
        this.sqlExecutor = executor;
        this.sqlExecutorMaker = () -> this.sqlExecutor;
        return this;
    }

    public synchronized DbConfig txExecutor(Function<DataSource, ITxExecutor> maker) {
        ensureMutable();
        require(maker);
        this.txExecutorMaker = maker;
        return this;
    }

    public synchronized DbConfig init() {
        if (dataSourceMaker == null || sqlExecutorMaker == null || txExecutorMaker == null) {
            throw new DbParameterException("data source, sql executor and tx executor are required");
        }
        return init(properties, dataSourceMaker, sqlExecutorMaker, txExecutorMaker);
    }

    private void ensureMutable() {
        if (state == State.READY || state == State.CLOSED) {
            throw new DbParameterException("configuration is immutable after initialization");
        }
    }

    private void require(Object value) {
        if (value == null) throw new DbParameterException("configuration value must not be null");
    }
}
