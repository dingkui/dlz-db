package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ValueContext;

/** 条件值写入 JDBC 前的转换链桩点。 */
public interface ConditionValuePoint extends OptionPoint {
    Object convertConditionValue(ValueContext context);
}
