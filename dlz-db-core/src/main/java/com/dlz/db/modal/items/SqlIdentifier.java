package com.dlz.db.modal.items;

import com.dlz.db.exception.DbParameterException;

import java.util.Objects;

public final class SqlIdentifier {
    private final String value;
    private SqlIdentifier(String value) {
        if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new DbParameterException("非法 SQL 标识符: " + value);
        }
        this.value = value;
    }
    public static SqlIdentifier of(String value) { return new SqlIdentifier(value); }
    public String value() { return value; }
    @Override public String toString() { return value; }
    @Override public boolean equals(Object o) { return o instanceof SqlIdentifier && value.equals(((SqlIdentifier) o).value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
