package com.dlz.db.modal.options.point.context;

import com.dlz.db.mapper.rowMapper.IRowMapper;

/** RowMapper 选择时保留目标类型的不可变上下文。 */
public final class RowMapperContext<T> {
    private final Class<T> resultType;
    private final IRowMapper<T> defaultMapper;
    private final String sql;

    public RowMapperContext(Class<T> resultType, IRowMapper<T> defaultMapper, String sql) {
        if (resultType == null) throw new IllegalArgumentException("resultType must not be null");
        this.resultType = resultType;
        this.defaultMapper = defaultMapper;
        this.sql = sql;
    }

    public Class<T> getResultType() { return resultType; }
    public IRowMapper<T> getDefaultMapper() { return defaultMapper; }
    public String getSql() { return sql; }
}
