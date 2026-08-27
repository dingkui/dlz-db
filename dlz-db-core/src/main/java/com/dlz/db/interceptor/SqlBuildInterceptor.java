package com.dlz.db.interceptor;

import com.dlz.db.option.DbOperation;
import com.dlz.db.option.DbOption;

import java.util.List;

/**
 * SQL 构建拦截器：以"供给默认 Option"的方式参与 SQL 构建。
 *
 * <p>拦截器不再直接改写 SQL，而是为每次操作供给一组默认 {@link DbOption}；
 * 这些 Option 通过实现 option.point 包中的桩点（如 WherePoint、InsertFieldPoint）
 * 参与构建。调用点显式传入的同 key Option 优先于拦截器供给的默认值，
 * 因此应用侧可随时用单次 Option 覆盖全局默认行为。
 *
 * <p>内置实现：{@link LogicDeleteInterceptor}（逻辑删除）。
 * 典型扩展场景：租户隔离、数据权限、审计字段。
 *
 * <pre>
 * // 租户隔离：按表动态供给（只对含 tenant_id 的表生效）
 * public class TenantInterceptor implements SqlBuildInterceptor {
 *     public List&lt;DbOption&gt; supplyOptions(DbOperation operation, String tableName) {
 *         return tenantTables.contains(tableName)
 *                 ? Collections.singletonList(new TenantOption())
 *                 : Collections.emptyList();
 *     }
 * }
 * </pre>
 *
 * @author dingkui
 * @since 8.0.0
 */
public interface SqlBuildInterceptor {

    /**
     * 拦截器是否启用。
     * <p>返回 false 时 {@link #supplyOptions} 不会被调用，等价于未注册。
     * 可用于运行时动态开关。
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 为指定操作供给默认 Option。
     *
     * <p>供给的 Option 与调用点传入的 Option 按 {@link DbOption#key()} 合并：
     * 同 key 时调用点优先。Option 是否真正参与构建由其实现的桩点决定
     * （桩点未注册到当前操作类型时自动不参与）。
     *
     * @param operation 当前操作类型
     * @param tableName 目标表名
     * @return 供给的默认 Option；返回 null 或空列表表示本次不供给
     */
    List<DbOption> supplyOptions(DbOperation operation, String tableName);
}
