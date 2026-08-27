package com.dlz.db.option.point.context;

import com.dlz.db.option.DbOperation;
import com.dlz.db.option.DbOptions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** CRUD 构建阶段的不可变上下文。 */
public final class CrudContext {
    private final DbOperation operation;
    private final String tableName;
    private final Class<?> entityType;
    private final DbOptions options;
    private final Set<String> whereColumns;

    public CrudContext(DbOperation operation, String tableName, Class<?> entityType, DbOptions options) {
        this(operation, tableName, entityType, options, null);
    }

    public CrudContext(DbOperation operation, String tableName, Class<?> entityType, DbOptions options,
                       Set<String> whereColumns) {
        if (operation == null) throw new IllegalArgumentException("operation must not be null");
        if (tableName == null || tableName.trim().isEmpty()) throw new IllegalArgumentException("tableName must not be empty");
        this.operation = operation;
        this.tableName = tableName;
        this.entityType = entityType;
        this.options = options == null ? DbOptions.EMPTY : options;
        this.whereColumns = whereColumns == null ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(whereColumns));
    }

    public DbOperation getOperation() { return operation; }
    public String getTableName() { return tableName; }
    public Class<?> getEntityType() { return entityType; }
    public DbOptions getOptions() { return options; }

    /** 调用方已在 WHERE 中声明的顶层条件列名（用于桩点避免重复注入）。 */
    public Set<String> getWhereColumns() { return whereColumns; }
}
