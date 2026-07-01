package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.PojoUpdate;

import java.util.List;

/**
 * 批量更新 Builder（Pojo）。
 * <pre>DB.batch.update(list).size(200).execute();</pre>
 */
public class BatchUpdate<T> {
    private final List<T> list;
    private int batchSize = 1000;

    BatchUpdate(List<T> list) {
        this.list = list;
    }

    /** 设置每批大小。 */
    public BatchUpdate<T> size(int n) {
        this.batchSize = n;
        return this;
    }

    /** 执行批量更新，返回是否成功。 */
    public boolean execute() {
        if (list == null || list.isEmpty()) {
            return false;
        }
        final Class<T> aClass = (Class<T>) list.get(0).getClass();
        return new PojoUpdate<>(aClass).batch(list, batchSize);
    }
}
