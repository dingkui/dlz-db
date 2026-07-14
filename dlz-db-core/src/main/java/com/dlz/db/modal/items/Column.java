package com.dlz.db.modal.items;

import com.dlz.db.exception.DbParameterException;

public final class Column {
    private final SqlIdentifier identifier;
    private Column(SqlIdentifier identifier) { this.identifier = identifier; }
    public static Column of(String name) { return new Column(SqlIdentifier.of(name)); }
    public static Column of(SqlIdentifier identifier) {
        if (identifier == null) throw new DbParameterException("column must not be null");
        return new Column(identifier);
    }
    public SqlIdentifier identifier() { return identifier; }
    public String name() { return identifier.value(); }
}
