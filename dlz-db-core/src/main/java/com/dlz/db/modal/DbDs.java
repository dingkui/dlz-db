package com.dlz.db.modal;

import com.dlz.db.ds.DBDynamic;
import com.dlz.db.ds.DataSourceProperty;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 数据源管理门面。
 * <p>只负责数据源注册/切换/删除，不处理事务（事务由 {@link DbTx} 提供）。
 *
 * <p>继承 {@link DBDynamic} 的现有能力（use/getCurrentConfig/getDataSource 等），
 * 在其上提供更短更直觉的命名：add/remove/use/call/test/names/has。
 */
public class DbDs extends DBDynamic {

    /** 注册数据源。 */
    public void add(String name, DataSourceProperty props) {
        props.setName(name);
        setDataSource(props);
    }

    /** 移除数据源，返回是否成功。 */
    public boolean remove(String name) {
        return removeDataSource(name);
    }

    /** 切换到指定数据源执行（无返回值）。 */
    public void use(String name, Runnable r) {
        super.use(name, r);
    }

    /** 切换到指定数据源执行（带返回值）。 */
    public <T> T call(String name, Supplier<T> c) {
        return use(name, c);
    }

    /** 测试数据源连接（不注册），返回是否可连。失败原因见日志。 */
    public boolean test(DataSourceProperty props) {
        try {
            testConnection(props);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 所有数据源名称。 */
    public Set<String> names() {
        return getAllDataSourceNames();
    }

    /** 数据源是否存在。 */
    public boolean has(String name) {
        return getAllDataSourceNames().contains(name);
    }
}
