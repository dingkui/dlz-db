package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.ValueContext;

/** INSERT 值写入前的转换链桩点。 */
public interface InsertValuePoint extends OptionPoint {
    Object convertInsertValue(ValueContext context);
}
