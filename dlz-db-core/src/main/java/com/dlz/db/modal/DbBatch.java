package com.dlz.db.modal;

import java.util.List;

/**
 * 批量操作门面。
 * <p>链式：insert/update/execute 返回 Builder，.size(n) 设批次大小，.execute() 执行。
 *
 * <pre>
 * DB.batch.insert(userList).execute();                      // 默认批次
 * DB.batch.insert(userList).size(500).execute();            // 自定义批次
 * DB.batch.update(userList).size(200).execute();
 * DB.batch.execute("INSERT INTO log(msg) VALUES(?)", params).size(100).execute();
 * </pre>
 */
public class DbBatch {

    /** 批量插入（Pojo），返回 Builder。 */
    public <T> BatchInsert<T> insert(List<T> list) {
        return new BatchInsert<>(list);
    }

    /** 批量更新（Pojo），返回 Builder。 */
    public <T> BatchUpdate<T> update(List<T> list) {
        return new BatchUpdate<>(list);
    }

    /** 批量执行原生 SQL（? 占位），返回 Builder。 */
    public BatchRaw execute(String sql, List<Object[]> paramList) {
        return new BatchRaw(sql, paramList);
    }
}
