package com.dlz.db.dialect;

/** JDBC 标准主键回填不可用时的方言兜底能力。 */
@FunctionalInterface
public interface GeneratedKeyDialect {
    String lastInsertIdSql();
}
