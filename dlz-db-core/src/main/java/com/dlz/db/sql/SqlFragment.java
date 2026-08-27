package com.dlz.db.sql;

import com.dlz.db.exception.DbParameterException;
import com.dlz.kit.json.JSONMap;

/**
 * 可参与 WHERE 拼接的 SQL 片段。
 *
 * <p>由 {@link com.dlz.db.option.point.WherePoint} 等桩点返回，
 * 框架负责将其并入目标语句的条件树。
 *
 * <pre>
 * // 无参数片段（字面量）
 * SqlFragment.trusted("status = 1");
 *
 * // 带参数片段：SQL 中以 #{key} 引用 paras 中的同名参数
 * JSONMap paras = new JSONMap();
 * paras.put("tenantId", currentTenantId());
 * SqlFragment.of("tenant_id = #{tenantId}", paras);
 * </pre>
 */
public final class SqlFragment {
    private final String sql;
    private final JSONMap paras;

    private SqlFragment(String sql, JSONMap paras) {
        if (sql == null || sql.trim().isEmpty()) throw new DbParameterException("sql fragment must not be empty");
        this.sql = sql;
        this.paras = paras;
    }

    /** 无参数片段：SQL 中不应包含 #{} 占位符。 */
    public static SqlFragment trusted(String sql) {
        return new SqlFragment(sql, null);
    }

    /** 带参数片段：SQL 中以 {@code #{key}} 引用 paras 中的同名参数。 */
    public static SqlFragment of(String sql, JSONMap paras) {
        return new SqlFragment(sql, paras);
    }

    public String sql() {
        return sql;
    }

    public JSONMap getParas() {
        return paras;
    }
}
