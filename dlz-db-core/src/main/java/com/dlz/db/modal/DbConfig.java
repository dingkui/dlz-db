package com.dlz.db.modal;

import com.dlz.db.interceptor.SqlBuildInterceptor;
import com.dlz.db.support.helper.SqlHelper;

import java.util.List;

/**
 * 配置与扩展注册门面。
 * <p>只管"全局扩展点注册"，不管：
 * <ul>
 *   <li>初始化（init）→ 内部 DBHolder，由 starter 自动调</li>
 *   <li>静态开关（showSql 等）→ DlzDbProperties + yml</li>
 *   <li>数据源管理 → DB.ds</li>
 *   <li>本次调用临时覆盖 → wrapper 链式 .convert()</li>
 * </ul>
 */
public class DbConfig {

    /**
     * 注册插件/拦截器（SQL 构建拦截器：逻辑删除/租户/数据权限/慢SQL 等）。
     * <p>注册一次，全局生效。通常在应用启动时调用。
     */
    public void plugin(SqlBuildInterceptor interceptor) {
        com.dlz.db.interceptor.DbPlugin.registerInterceptor(interceptor);
    }

    /**
     * 查询已注册的插件/拦截器列表（只读）。
     */
    public List<SqlBuildInterceptor> plugins() {
        return com.dlz.db.interceptor.DbPlugin.getInterceptors();
    }

    /**
     * 注册方言实现。
     * <p>方言自带 supports(url) 识别能力，DataSourceConfig.getSqlHelper() 遍历已注册方言匹配。
     * 外部加新数据库（如 KingBase）零框架改动，纯注册。
     *
     * <p>也可通过 SPI 自动发现：方言包放 META-INF/services/...SqlHelper，启动自动注册。
     */
    public void dialect(SqlHelper dialect) {
        // TODO: 维护方言注册表（CopyOnWriteArrayList），供 DataSourceConfig.getSqlHelper() 查找
    }

    /**
     * 查询已注册的方言列表（只读）。
     */
    public List<SqlHelper> dialects() {
        // TODO: 返回方言注册表
        return java.util.Collections.emptyList();
    }
}
