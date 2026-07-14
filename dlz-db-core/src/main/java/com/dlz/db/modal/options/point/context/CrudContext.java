package com.dlz.db.modal.options.point.context;

import com.dlz.db.modal.options.DbOperation;
import com.dlz.db.modal.options.DbOptions;

/** CRUD 构建阶段的不可变上下文。 */
public final class CrudContext {
    private final DbOperation operation;
    private final String tableName;
    private final Class<?> entityType;
    private final DbOptions options;

    public CrudContext(DbOperation operation, String tableName, Class<?> entityType, DbOptions options) {
        if (operation == null) throw new IllegalArgumentException("operation must not be null");
        if (tableName == null || tableName.trim().isEmpty()) throw new IllegalArgumentException("tableName must not be empty");
        this.operation = operation;
        this.tableName = tableName;
        this.entityType = entityType;
        this.options = options == null ? DbOptions.EMPTY : options;
    }

    public DbOperation getOperation() { return operation; }
    public String getTableName() { return tableName; }
    public Class<?> getEntityType() { return entityType; }
    public DbOptions getOptions() { return options; }
}
