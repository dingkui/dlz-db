package com.dlz.db.modal.options.point.context;

/** 一个字段及其待写入值。 */
public final class FieldValue {
    private final String fieldName;
    private final Object value;

    public FieldValue(String fieldName, Object value) {
        if (fieldName == null || fieldName.trim().isEmpty()) throw new IllegalArgumentException("fieldName must not be empty");
        this.fieldName = fieldName;
        this.value = value;
    }

    public String getFieldName() { return fieldName; }
    public Object getValue() { return value; }
}
