package com.dlz.db.ds;

import com.dlz.db.dialect.DbDialect;
import com.dlz.db.dialect.DialectRegistry;
import com.dlz.db.dialect.SchemaDialect;
import com.dlz.db.mapper.rowMapper.ResultMapRowMapper;
import com.dlz.kit.exception.SystemException;
import com.dlz.kit.util.ValUtil;
import lombok.AccessLevel;
import lombok.Setter;

import javax.sql.DataSource;

public class DataSourceConfig {
    protected final DataSourceProperty property;

    public DataSourceConfig(DataSourceProperty property) {
        this.property = property;
    }

    @Setter(AccessLevel.NONE)
    private ResultMapRowMapper rowMapper;

    private DbDialect dialect;
    private SchemaDialect schemaDialect;

    public ResultMapRowMapper getRowMapper() {
        if (rowMapper != null) {
            return rowMapper;
        }
        rowMapper = getDialect().mapping().createRowMapper();
        return rowMapper;
    }

    @Setter
    private DataSource dataSource;

    public DataSource getDataSource() {
        if (dataSource != null) {
            return dataSource;
        }
        try {
            IDataSourceCreator dataSourceCreator;
            if (ValUtil.isEmpty(property.getCreatorClassName())) {
                dataSourceCreator = new DataSourceCreatorHikari();
            } else {
                dataSourceCreator = (IDataSourceCreator) Class.forName(property.getCreatorClassName()).newInstance();
            }
            dataSource = dataSourceCreator.createDataSource(property);
            // 创建新的数据源
            return dataSource;
        } catch (Exception e) {
            throw new RuntimeException("取得数据源失败: " + e.getMessage(), e);
        }
    }

    /** 当前数据源使用的方言。 */
    public DbDialect getDialect() {
        if (dialect != null) {
            return dialect;
        }
        dialect = DialectRegistry.resolve(null, property.getUrl());
        if (dialect == null && dataSource != null) {
            try (java.sql.Connection connection = dataSource.getConnection()) {
                dialect = DialectRegistry.resolve(connection.getMetaData(), property.getUrl());
            } catch (Exception ignored) {
                // 保持统一的未识别异常，避免暴露探测连接异常。
            }
        }
        if (dialect == null) {
            throw new SystemException("未识别的数据库方言: " + property.getUrl());
        }
        return dialect;
    }

    public SchemaDialect getSchemaDialect() {
        if (schemaDialect == null) {
            schemaDialect = getDialect().schema();
        }
        return schemaDialect;
    }

    public void close() throws Exception {
        if (dataSource != null) {
            if (dataSource instanceof AutoCloseable) {
                ((AutoCloseable) dataSource).close();
            }
            dataSource = null;
        }
        if (rowMapper != null) {
            rowMapper = null;
        }
        schemaDialect = null;
    }

    public String getName() {
        return property.getName();
    }
    public String getSchema() {
        return property.getSchema();
    }
}
