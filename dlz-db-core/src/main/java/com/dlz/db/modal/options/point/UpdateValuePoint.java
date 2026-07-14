package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.ValueContext;

/** UPDATE 值写入前的转换链桩点。 */
public interface UpdateValuePoint extends OptionPoint {
    Object convertUpdateValue(ValueContext context);
}
