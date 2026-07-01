package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.PojoInsert;

import java.util.List;

/**
 * 批量插入 Builder（Pojo）。
 * <pre>DB.batch.insert(list).size(500).execute();</pre>
 */
public class BatchInsert<T> {
    private final List<T> list;
    private int batchSize = 1000;

    BatchInsert(List<T> list) {
        this.list = list;
    }

    /** 设置每批大小。 */
    public BatchInsert<T> size(int n) {
        this.batchSize = n;
        return this;
    }

    /** 执行批量插入，返回是否成功。 */
    public boolean execute() {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return new PojoInsert(list.get(0)).batch(list, batchSize);
    }
}
