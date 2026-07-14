package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.ValueContext;

/** 逻辑删除标记值的排他桩点。 */
public interface LogicDeleteValuePoint extends OptionPoint {
    Object provideLogicDeleteValue(ValueContext context);
}
