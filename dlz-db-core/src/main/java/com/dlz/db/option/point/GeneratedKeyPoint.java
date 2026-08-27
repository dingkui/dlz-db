package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ValueContext;

/** INSERT 主键生成的排他桩点。 */
public interface GeneratedKeyPoint extends OptionPoint {
    Object generateKey(ValueContext context);
}
