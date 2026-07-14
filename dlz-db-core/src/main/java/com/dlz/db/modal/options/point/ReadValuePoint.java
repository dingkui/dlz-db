package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.ValueContext;

/** JDBC 读取后、结果映射前的值转换链桩点。 */
public interface ReadValuePoint extends OptionPoint {
    Object convertReadValue(ValueContext context);
}
