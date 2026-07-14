package com.dlz.db.modal.options.point.context;

import com.dlz.db.modal.options.DbOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 字段聚合时的只读操作快照。 */
public final class FieldContext {
    private final DbOperation operation;
    private final String tableName;
    private final Class<?> entityType;
    private final List<String> fieldNames;

    public FieldContext(DbOperation operation, String tableName, Class<?> entityType, List<String> fieldNames) {
        if (operation == null) throw new IllegalArgumentException("operation must not be null");
        if (tableName == null || tableName.trim().isEmpty()) throw new IllegalArgumentException("tableName must not be empty");
        this.operation = operation;
        this.tableName = tableName;
        this.entityType = entityType;
        this.fieldNames = fieldNames == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(fieldNames));
    }

    public DbOperation getOperation() { return operation; }
    public String getTableName() { return tableName; }
    public Class<?> getEntityType() { return entityType; }
    public List<String> getFieldNames() { return fieldNames; }
}
