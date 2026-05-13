package com.dlz.db.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 数据库配置基类。
 * <p>包含所有框架通用的数据库配置，各框架可继承此类添加特定注解。</p>
 *
 * @since 7.0.0
 */
@Getter
@Setter
public class BaseDbProperties {
    /**
     * 数据库支持类
     */
    private String dbSupport = "";
    /**
     * 数据库中blob类型编码
     */
    private String blob_charset = "GBK";
    /**
     * sql路径配置
     **/
    private List<String> sqllist = new ArrayList<>(Arrays.asList("app/*"));
    /**
     * 从数据库中取得sql配置的sql
     **/
    private String sql = "select sql_key as k ,sql_value as s from sys_sql";
    /**
     * 从数据库中取得sql配置是否开启,默认关闭
     **/
    private boolean useDbSql = false;
    /**
     * 数据库结构缓存时间，-1为不失效，单位为秒
     **/
    private int tableCacheTime = -1;
    /**
     * 逻辑删除字段
     **/
    private String logicDeleteField = "IS_DELETED";
    /**
     * sqlHelper配置
     */
    private Helper helper = new Helper();
    /**
     * 日志配置
     */
    private Log log = new Log();

    /**
     * 获取 sqllist 的不可变副本，防止外部修改
     */
    public List<String> getSqllist() {
        return Collections.unmodifiableList(sqllist);
    }

    /**
     * 设置 sqllist，创建副本以防止外部修改
     */
    public void setSqllist(List<String> sqllist) {
        this.sqllist = new ArrayList<>(sqllist);
    }

    /**
     * 获取 helper 的副本，防止外部修改
     */
    public Helper getHelper() {
        Helper copy = new Helper();
        copy.setPackageName(helper.getPackageName());
        copy.setAutoUpdate(helper.isAutoUpdate());
        return copy;
    }

    /**
     * 设置 helper，创建副本以防止外部修改
     */
    public void setHelper(Helper helper) {
        this.helper = new Helper();
        this.helper.setPackageName(helper.getPackageName());
        this.helper.setAutoUpdate(helper.isAutoUpdate());
    }

    /**
     * 获取 log 的副本，防止外部修改
     */
    public Log getLog() {
        Log copy = new Log();
        copy.setShowResult(log.isShowResult());
        copy.setShowRunSql(log.isShowRunSql());
        copy.setShowCaller(log.isShowCaller());
        copy.setSlowSqlThreshold(log.getSlowSqlThreshold());
        return copy;
    }

    /**
     * 设置 log，创建副本以防止外部修改
     */
    public void setLog(Log log) {
        this.log = new Log();
        this.log.setShowResult(log.isShowResult());
        this.log.setShowRunSql(log.isShowRunSql());
        this.log.setShowCaller(log.isShowCaller());
        this.log.setSlowSqlThreshold(log.getSlowSqlThreshold());
    }

    /**
     * sqlHelper配置
     */
    @Getter
    @Setter
    public static class Helper {
        /**
         * 自动更新数据库扫码数据包
         */
        String packageName = "com.dlz";
        /**
         * 是否开启自动更新数据库，生产环境不应开启，可提高启动速度
         */
        boolean autoUpdate = false;
    }

    /**
     * 日志配置
     */
    @Getter
    @Setter
    public static class Log {
        /**
         * 是否显示结果日志
         */
        private boolean showResult = false;
        /**
         * 是否显示运行sql
         */
        private boolean showRunSql = false;
        /**
         * 日志中是否显示运行sql调用处,默认关闭
         */
        private boolean showCaller = false;
        /**
         * 慢 SQL 阈值（毫秒）,默认0,0表示不启用
         */
        private long slowSqlThreshold = 0l;
    }
}
