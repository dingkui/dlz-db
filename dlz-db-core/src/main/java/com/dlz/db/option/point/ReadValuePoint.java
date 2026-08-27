package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ValueContext;

/** JDBC 读取后、结果映射前的值转换链桩点。 */
public interface ReadValuePoint extends OptionPoint {
    Object convertReadValue(ValueContext context);
}
