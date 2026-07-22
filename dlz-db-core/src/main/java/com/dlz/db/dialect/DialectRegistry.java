package com.dlz.db.dialect;

import com.dlz.db.exception.DbParameterException;

import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 全局方言注册中心。 */
public final class DialectRegistry {
    private static final Map<String, DbDialect> DIALECTS = new ConcurrentHashMap<>();

    static {
        BuiltinDialects.registerAll(DIALECTS);
    }

    private DialectRegistry() {
    }

    public static void register(DbDialect dialect) {
        if (dialect == null) {
            throw new DbParameterException("dialect must not be null");
        }
        String id = normalize(dialect.id());
        if (id == null) {
            throw new DbParameterException("dialect id must not be empty");
        }
        DIALECTS.put(id, dialect);
    }

    public static DbDialect get(String id) {
        String normalized = normalize(id);
        return normalized == null ? null : DIALECTS.get(normalized);
    }

    public static boolean contains(String id) {
        return get(id) != null;
    }

    public static DbDialect resolve(DatabaseMetaData metadata, String jdbcUrl) {
        if (jdbcUrl != null) {
            for (DbDialect dialect : DIALECTS.values()) {
                if (dialect.matchesUrl(jdbcUrl)) {
                    return dialect;
                }
            }
        }
        if (metadata != null) {
            try {
                String product = metadata.getDatabaseProductName();
                for (DbDialect dialect : DIALECTS.values()) {
                    if (dialect.matchesProduct(product)) {
                        return dialect;
                    }
                }
            } catch (Exception ignored) {
                // URL remains the fallback when metadata cannot be read.
            }
        }
        return null;
    }

    public static Collection<DbDialect> all() {
        return Collections.unmodifiableCollection(DIALECTS.values());
    }

    private static String normalize(String id) {
        if (id == null) {
            return null;
        }
        String value = id.trim();
        return value.isEmpty() ? null : value.toLowerCase(java.util.Locale.ROOT);
    }
}
