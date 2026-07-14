package com.dlz.db.modal.options.point.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 可执行 SQL 及其有序参数的不可变快照。 */
public final class SqlStatement {
    private final String sql;
    private final List<Object> parameters;

    public SqlStatement(String sql, List<?> parameters) {
        if (sql == null || sql.trim().isEmpty()) throw new IllegalArgumentException("sql must not be empty");
        this.sql = sql;
        this.parameters = parameters == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<Object>(parameters));
    }

    public String getSql() { return sql; }
    public List<Object> getParameters() { return parameters; }
}
