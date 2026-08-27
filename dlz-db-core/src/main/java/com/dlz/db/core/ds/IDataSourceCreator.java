package com.dlz.db.core.ds;

import javax.sql.DataSource;

public interface IDataSourceCreator {
    DataSource createDataSource(DataSourceProperty properties);
}
