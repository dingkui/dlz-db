package com.dlz.db.modal.options.point.context;

import java.sql.PreparedStatement;

/** 单个 JDBC 参数绑定上下文。 */
public final class ParameterContext {
    private final PreparedStatement statement;
    private final int index;
    private final Object value;
    private final Integer jdbcType;
    private final String fieldName;

    public ParameterContext(PreparedStatement statement, int index, Object value, Integer jdbcType, String fieldName) {
        if (statement == null) throw new IllegalArgumentException("statement must not be null");
        if (index < 1) throw new IllegalArgumentException("index must be greater than zero");
        this.statement = statement;
        this.index = index;
        this.value = value;
        this.jdbcType = jdbcType;
        this.fieldName = fieldName;
    }

    public PreparedStatement getStatement() { return statement; }
    public int getIndex() { return index; }
    public Object getValue() { return value; }
    public Integer getJdbcType() { return jdbcType; }
    public String getFieldName() { return fieldName; }
}
