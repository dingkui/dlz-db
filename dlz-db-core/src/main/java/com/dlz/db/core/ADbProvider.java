package com.dlz.db.core;

import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.service.ICommService;

/**
 * 数据库组件提供者抽象类。
 * <p>整合所有数据库相关组件的获取方式，支持不同框架的实现。</p>
 *
 * @since 7.0.0
 */
public abstract class ADbProvider {

    /**
     * 根据数据源配置创建 TxExecutor 实例。
     *
     * @param dataSourceConfig 数据源配置
     * @return TxExecutor 实例
     */
    public abstract ITxExecutor createTxExecutor(DataSourceConfig dataSourceConfig);

    /**
     * 获取 SqlExecutor 实例。
     *
     * @return SqlExecutor 实例
     */
    public abstract ISqlExecutor getSqlExecutor();

    /**
     * 获取 ICommService 实例。
     *
     * @return ICommService 实例
     */
    public abstract ICommService getService();

    /**
     * 获取 CacheExecutor 实例。
     *
     * @return CacheExecutor 实例
     */
    public abstract ICacheExecutor getCacheExecutor();

    /**
     * 获取 ResourceLoader 实例。
     *
     * @return ResourceLoader 实例
     */
    public abstract IResourceLoader getResourceLoader();

    /**
     * 获取 ClassScanner 实例。
     *
     * @return ClassScanner 实例
     */
    public abstract IClassScanner getClassScanner();

    /**
     * 获取 SqlConfig 实例。
     *
     * @return SqlConfig 实例
     */
    public abstract BaseDbProperties getSqlConfig();
}
