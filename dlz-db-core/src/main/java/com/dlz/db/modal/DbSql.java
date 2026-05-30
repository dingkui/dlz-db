package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.SqlExecute;
import com.dlz.db.modal.wrapper.SqlQuery;

import java.util.Map;

public class DbSql {
    public SqlQuery select(String sql) {
        return new SqlQuery(sql);
    }

    public SqlExecute insert(String sql) {
        return new SqlExecute(sql);
    }

    public SqlExecute update(String sql) {
        return new SqlExecute(sql);
    }

    public SqlExecute delete(String sql) {
        return new SqlExecute(sql);
    }

    public SqlExecute executer(String sql) {
        return new SqlExecute(sql);
    }

    public int execute(String sql, Map<String, Object> paras) {
        return new SqlExecute(sql).addParas(paras).execute();
    }
}