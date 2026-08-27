package com.dlz.db.option;

/** 可持有单次操作 Option 快照的执行对象。 */
public interface DbOptionAware {
    DbOptions getDbOptions();
}
