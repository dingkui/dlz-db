package com.dlz.db.inf;

import com.dlz.db.holder.DBHolder;

/**
 * 执行器：增删改执行器
 */
public interface IExecutorUDI extends ISqlPara {
    default int execute() {
        return DBHolder.doDb(s -> s.execute(this));
    }
}
