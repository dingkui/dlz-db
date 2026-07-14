package com.dlz.db.modal.options.point.context;

import com.dlz.db.modal.options.DbOptions;

/** SQL 改写阶段的不可变上下文。 */
public final class SqlContext {
    private final ExecutionKind kind;
    private final String tableName;
    private final SqlStatement statement;
    private final DbOptions options;

    public SqlContext(ExecutionKind kind, String tableName, SqlStatement statement, DbOptions options) {
        if (kind == null) throw new IllegalArgumentException("kind must not be null");
        if (statement == null) throw new IllegalArgumentException("statement must not be null");
        this.kind = kind;
        this.tableName = tableName;
        this.statement = statement;
        this.options = options == null ? DbOptions.EMPTY : options;
    }

    public ExecutionKind getKind() { return kind; }
    public String getTableName() { return tableName; }
    public SqlStatement getStatement() { return statement; }
    public DbOptions getOptions() { return options; }
}
