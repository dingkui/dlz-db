package com.dlz.db.dialect;

/** SQL 语法能力，例如标识符引用和分页。 */
public interface SqlDialect {
    default String quoteIdentifier(String identifier) {
        return "`" + identifier + "`";
    }

    default String pagination(String sql, long offset, int limit) {
        return sql + " LIMIT " + offset + "," + limit;
    }
}
