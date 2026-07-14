package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;

/** 条件缺失时安全策略的排他桩点。 */
public interface ConditionSafetyPoint extends OptionPoint {
    boolean allowEmptyCondition(CrudContext context);
}
