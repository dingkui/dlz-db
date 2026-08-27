package com.dlz.db.option.point.context;

/** 查询锁策略。 */
public enum SelectLockMode {
    NONE,
    FOR_UPDATE,
    FOR_SHARE
}
