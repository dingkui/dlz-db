package com.dlz.db.modal.options.point.context;

import com.dlz.db.modal.options.DbOperation;

/** 单个字段值转换时的不可变上下文。 */
public final class ValueContext {
    private final DbOperation operation;
    private final String tableName;
    private final String fieldName;
    private final String columnName;
    private final Object value;
    private final Class<?> targetType;

    public ValueContext(DbOperation operation, String tableName, String fieldName, String columnName,
                        Object value, Class<?> targetType) {
        this.operation = operation;
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.value = value;
        this.targetType = targetType;
    }

    public DbOperation getOperation() { return operation; }
    public String getTableName() { return tableName; }
    public String getFieldName() { return fieldName; }
    public String getColumnName() { return columnName; }
    public Object getValue() { return value; }
    public Class<?> getTargetType() { return targetType; }
}
