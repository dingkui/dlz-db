package com.dlz.db.interceptor;

import com.dlz.db.option.DbOption;
import com.dlz.db.option.DbOperation;
import com.dlz.db.option.DbOptions;
import com.dlz.db.option.LogicDeleteOption;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQL 构建拦截器注册中心，同时是"默认 Option 合并"的唯一入口。
 *
 * <p>SQL 注入不再由拦截器直接完成：每次构建前调用
 * {@link #effectiveOptions} 合并"拦截器供给的默认 Option"与"调用点 Option"
 * （同 key 调用点优先），再由 Option 实现的桩点统一执行，
 * 因此 SQL 注入路径只有 option.point 一条。
 *
 * @author dingkui
 * @since 8.0.0
 */
@Slf4j
public class DbPlugin {

    /**
     * 已注册的 SQL 构建拦截器列表。
     * 使用 CopyOnWriteArrayList 保证遍历时不加锁（注册频率低，遍历频率高）。
     */
    private static final List<SqlBuildInterceptor> interceptors = new CopyOnWriteArrayList<>();

    /**
     * 注册一个 SQL 构建拦截器。
     * <p>通常在应用启动时调用（如 Spring @PostConstruct 或 DB.config.plugin(...)）。
     *
     * @param interceptor 拦截器实例
     */
    public static void registerInterceptor(SqlBuildInterceptor interceptor) {
        if (interceptor != null && interceptor.isEnabled()) {
            interceptors.add(interceptor);
            log.info("Registered SqlBuildInterceptor: {}", interceptor.getClass().getName());
        }
    }

    /**
     * 移除所有已注册的拦截器（主要用于测试场景重置状态）。
     */
    public static void clearInterceptors() {
        interceptors.clear();
    }

    /**
     * 获取已注册的拦截器列表（只读视图）。
     */
    public static List<SqlBuildInterceptor> getInterceptors() {
        return interceptors;
    }

    /**
     * 获取已注册的拦截器数量。
     */
    public static int getInterceptorCount() {
        return interceptors.size();
    }

    /**
     * 合并拦截器供给的默认 Option 与调用点 Option。
     *
     * <ul>
     *   <li>无拦截器时直接返回调用点 Option（零开销）；</li>
     *   <li>同 {@link DbOption#key()} 时调用点优先（覆盖全局默认）；</li>
     *   <li>不适配当前操作的 Option 不参与合并（不抛错，供跨操作复用的 Option 使用）。</li>
     * </ul>
     *
     * @param operation 当前操作类型
     * @param tableName 目标表名
     * @param userOptions 调用点传入的 Option 集合（可为 null）
     * @return 合并后的 Option 集合
     */
    public static DbOptions effectiveOptions(DbOperation operation, String tableName, DbOptions userOptions) {
        final DbOptions base = userOptions == null ? DbOptions.EMPTY : userOptions;
        if (interceptors.isEmpty()) {
            return base;
        }
        Map<String, DbOption> merged = new LinkedHashMap<>();
        for (SqlBuildInterceptor interceptor : interceptors) {
            if (!interceptor.isEnabled()) {
                continue;
            }
            List<DbOption> supplied = interceptor.supplyOptions(operation, tableName);
            if (supplied == null) {
                continue;
            }
            for (DbOption option : supplied) {
                if (option != null && option.supports(operation)) {
                    merged.putIfAbsent(option.key(), option);
                }
            }
        }
        if (merged.isEmpty()) {
            return base;
        }
        for (DbOption option : base.asList()) {
            merged.put(option.key(), option); // 调用点优先
        }
        for (DbOption option : merged.values().toArray(new DbOption[0])) {
            if (!option.supports(operation)) {
                merged.remove(option.key());
            }
        }
        if (merged.isEmpty()) {
            return base;
        }
        return DbOptions.resolve(operation, merged.values().toArray(new DbOption[0]));
    }

    // ==================== 逻辑删除辅助（供批量等旁路路径使用） ====================

    /**
     * 获取已注册的逻辑删除选项（未注册返回 null）。
     */
    public static LogicDeleteOption getLogicDeleteOption() {
        for (SqlBuildInterceptor interceptor : interceptors) {
            if (interceptor instanceof LogicDeleteInterceptor) {
                return ((LogicDeleteInterceptor) interceptor).getOption();
            }
        }
        return null;
    }

    /**
     * 获取指定表的逻辑删除字段（Bean Field 形式）。
     * <p>若未注册逻辑删除或表不含逻辑删除字段，则返回 null。
     */
    public static Field getLogicDeleteField(String tableName, Class<?> beanClass) {
        LogicDeleteOption option = getLogicDeleteOption();
        return option == null ? null : option.getLogicDeleteField(tableName, beanClass);
    }

    /**
     * 获取指定表的逻辑删除字段名（字符串形式）。
     * <p>若未注册逻辑删除或表不含逻辑删除字段，则返回 null。
     */
    public static String getLogicDeleteField(String tableName) {
        LogicDeleteOption option = getLogicDeleteOption();
        return option == null ? null : option.getLogicDeleteField(tableName);
    }
}
