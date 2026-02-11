package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.SqlExecute;
import com.dlz.db.modal.wrapper.SqlQuery;

public class DbSql {
    public SqlQuery select(String sql) {
        return new SqlQuery(sql);
    }

    public SqlExecute executer(String sql) {
        return new SqlExecute(sql);
    }
}