package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.ValueContext;

/** INSERT 主键生成的排他桩点。 */
public interface GeneratedKeyPoint extends OptionPoint {
    Object generateKey(ValueContext context);
}
