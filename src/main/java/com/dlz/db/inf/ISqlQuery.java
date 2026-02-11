package com.dlz.db.inf;

import com.dlz.db.modal.condition.Condition;

/**
 * 查询构造器=AndOr条件构造器
 *
 * @param <T>
 */
public interface ISqlQuery<T extends ISqlQuery> extends
        ICondAndOr<T>,
        ICondAddByKey<T>,
        ICondAuto<T> {
    Condition where();

    default void addChildren(Condition child) {
        where().addChildren(child);
    }

    T where(Condition cond);

    T setAllowFullQuery(boolean allowFullQuery);
    String getTableName();
    boolean isAllowFullQuery();
}
