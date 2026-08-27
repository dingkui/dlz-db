package com.dlz.db.dialect;

import com.dlz.db.dialect.rowMapper.ResultMapRowMapper;

/**
 * 数据库方言注册描述。
 *
 * <p>方言的身份使用稳定字符串表示，具体 SQL 行为由方言实现逐步提供。
 * 该接口不依赖某个固定数据库枚举，便于应用或扩展模块注册自定义方言。</p>
 */
public interface DbDialect {
    /** 方言唯一标识，例如 {@code mysql}、{@code postgresql}。 */
    String id();

    default SqlDialect sql() {
        return new SqlDialect() {
        };
    }

    /** 数据库结构能力。 */
    default SchemaDialect schema() {
        throw new UnsupportedOperationException("dialect schema capability is not configured: " + id());
    }

    default ResultMappingDialect mapping() {
        return this::createRowMapper;
    }

    default GeneratedKeyDialect generatedKey() {
        return this::lastInsertIdSql;
    }

    default boolean matchesProduct(String productName) {
        return false;
    }

    default boolean matchesUrl(String url) {
        return false;
    }

    default ResultMapRowMapper createRowMapper() {
        return new ResultMapRowMapper();
    }

    default String lastInsertIdSql() {
        return null;
    }
}
