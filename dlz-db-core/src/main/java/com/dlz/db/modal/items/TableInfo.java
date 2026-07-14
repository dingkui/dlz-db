package com.dlz.db.modal.items;

import com.dlz.db.exception.DbParameterException;

public final class TableInfo {
    private final SqlIdentifier table;
    private final Column idColumn;
    private TableInfo(SqlIdentifier table, Column idColumn) {
        if (table == null || idColumn == null) throw new DbParameterException("table and idColumn must not be null");
        this.table = table; this.idColumn = idColumn;
    }
    public static TableInfo of(String table, String idColumn) { return new TableInfo(SqlIdentifier.of(table), Column.of(idColumn)); }
    public static TableInfo of(SqlIdentifier table, Column idColumn) { return new TableInfo(table, idColumn); }
    public SqlIdentifier table() { return table; }
    public Column idColumn() { return idColumn; }
}
