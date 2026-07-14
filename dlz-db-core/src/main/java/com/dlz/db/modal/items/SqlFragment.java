package com.dlz.db.modal.items;

import com.dlz.db.exception.DbParameterException;

public final class SqlFragment {
    private final String sql;
    private SqlFragment(String sql) {
        if (sql == null || sql.trim().isEmpty()) throw new DbParameterException("sql fragment must not be empty");
        this.sql = sql;
    }
    public static SqlFragment trusted(String sql) { return new SqlFragment(sql); }
    public String sql() { return sql; }
}
