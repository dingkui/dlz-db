package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;

/** 条件缺失时安全策略的排他桩点。 */
public interface ConditionSafetyPoint extends OptionPoint {
    boolean allowEmptyCondition(CrudContext context);
}
