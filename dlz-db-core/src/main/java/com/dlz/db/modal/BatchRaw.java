package com.dlz.db.modal;

import com.dlz.db.support.DBHolder;

import java.util.List;

/**
 * 批量执行原生 SQL Builder（? 占位）。
 * <pre>DB.batch.execute("INSERT INTO log(msg) VALUES(?)", paramList).size(100).execute();</pre>
 */
public class BatchRaw {
    private final String sql;
    private final List<Object[]> paramList;
    private int batchSize = 1000;

    BatchRaw(String sql, List<Object[]> paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

    /** 设置每批大小。 */
    public BatchRaw size(int n) {
        this.batchSize = n;
        return this;
    }

    /** 执行批量，返回是否成功。 */
    public boolean execute() {
        if (paramList == null || paramList.isEmpty()) {
            return false;
        }
        List<Object[]> remaining = paramList;
        int size = batchSize;
        while (!remaining.isEmpty() && size > 0) {
            if (size > remaining.size()) {
                size = remaining.size();
            }
            List<Object[]> chunk = remaining.subList(0, size);
            DBHolder.getSqlExecutor().batch(sql, chunk);
            remaining = remaining.subList(size, remaining.size());
        }
        return true;
    }
}
