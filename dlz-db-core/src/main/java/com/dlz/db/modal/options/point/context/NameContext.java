package com.dlz.db.modal.options.point.context;

import com.dlz.db.modal.options.DbOperation;

/** 字段名或列名转换时的不可变上下文。 */
public final class NameContext {
    private final DbOperation operation;
    private final String tableName;
    private final String sourceName;

    public NameContext(DbOperation operation, String tableName, String sourceName) {
        if (sourceName == null || sourceName.trim().isEmpty()) throw new IllegalArgumentException("sourceName must not be empty");
        this.operation = operation;
        this.tableName = tableName;
        this.sourceName = sourceName;
    }

    public DbOperation getOperation() { return operation; }
    public String getTableName() { return tableName; }
    public String getSourceName() { return sourceName; }
}
