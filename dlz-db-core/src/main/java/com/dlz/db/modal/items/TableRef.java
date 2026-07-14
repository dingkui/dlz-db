package com.dlz.db.modal.items;

import com.dlz.db.exception.DbParameterException;

public final class TableRef {
    private final SqlIdentifier table;
    private final Column idColumn;
    private TableRef(SqlIdentifier table, Column idColumn) {
        if (table == null || idColumn == null) throw new DbParameterException("table and idColumn must not be null");
        this.table = table; this.idColumn = idColumn;
    }
    public static TableRef of(String table, String idColumn) { return new TableRef(SqlIdentifier.of(table), Column.of(idColumn)); }
    public static TableRef of(SqlIdentifier table, Column idColumn) { return new TableRef(table, idColumn); }
    public SqlIdentifier table() { return table; }
    public Column idColumn() { return idColumn; }
}
