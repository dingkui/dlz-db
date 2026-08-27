package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ValueContext;

/** UPDATE 值写入前的转换链桩点。 */
public interface UpdateValuePoint extends OptionPoint {
    Object convertUpdateValue(ValueContext context);
}
