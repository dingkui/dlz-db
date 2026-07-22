package com.dlz.db.dialect;

import com.dlz.db.dialect.schemas.SchamaPostgresql;
import com.dlz.db.dialect.schemas.SchemaDm8;
import com.dlz.db.dialect.schemas.SchemaMysql;
import com.dlz.db.dialect.schemas.SchemaSqlite;
import com.dlz.db.mapper.rowMapper.MySqlColumnMapRowMapper;
import com.dlz.db.mapper.rowMapper.OracleColumnMapRowMapper;
import com.dlz.db.mapper.rowMapper.ResultMapRowMapper;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

final class BuiltinDialects {
    private BuiltinDialects() {
    }

    static void registerAll(Map<String, DbDialect> target) {
        register(target, dialect("mysql", new String[]{"mysql", "mariadb"}, SchemaMysql::new,
                MySqlColumnMapRowMapper::new, "SELECT LAST_INSERT_ID()"));
        register(target, dialect("postgresql", new String[]{"postgresql"}, SchamaPostgresql::new,
                MySqlColumnMapRowMapper::new, null));
        register(target, dialect("oracle", new String[]{"oracle"}, SchemaMysql::new,
                OracleColumnMapRowMapper::new, null));
        register(target, dialect("dm8", new String[]{"dm"}, SchemaDm8::new,
                OracleColumnMapRowMapper::new, null));
        register(target, dialect("sqlite", new String[]{"sqlite"}, SchemaSqlite::new,
                ResultMapRowMapper::new, "SELECT last_insert_rowid()"));
        register(target, dialect("h2", new String[]{"h2", "hsql"}, SchemaMysql::new,
                ResultMapRowMapper::new, "CALL IDENTITY()"));
        register(target, dialect("mssql", new String[]{"sqlserver"}, SchemaMysql::new,
                ResultMapRowMapper::new, null));
    }

    private static void register(Map<String, DbDialect> target, DbDialect dialect) {
        target.put(dialect.id(), dialect);
    }

    private static DbDialect dialect(String id, String[] tokens, Supplier<SchemaDialect> helper,
                                     Supplier<ResultMapRowMapper> mapper, String lastInsertIdSql) {
        return new DbDialect() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public boolean matchesProduct(String productName) {
                return containsToken(productName, tokens);
            }

            @Override
            public boolean matchesUrl(String url) {
                return containsToken(url, tokens);
            }

            @Override
            public ResultMapRowMapper createRowMapper() {
                return mapper.get();
            }

            @Override
            public SchemaDialect schema() {
                return helper.get();
            }

            @Override
            public String lastInsertIdSql() {
                return lastInsertIdSql;
            }
        };
    }

    private static boolean containsToken(String value, String[] tokens) {
        if (value == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        for (String token : tokens) {
            if (normalized.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
