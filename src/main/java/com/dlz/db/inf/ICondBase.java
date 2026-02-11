package com.dlz.db.inf;

import com.dlz.comm.inf.IChained;
import com.dlz.db.modal.condition.Condition;

/**
 * 添加and or条件
 * @param <T>
 */
public interface ICondBase<T extends ICondBase> extends IChained<T> {
    void addChildren(Condition child);
}
