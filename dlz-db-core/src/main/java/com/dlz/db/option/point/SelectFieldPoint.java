package com.dlz.db.option.point;

import com.dlz.db.option.point.context.FieldContext;
import com.dlz.db.option.point.context.FieldContribution;

/** 聚合 SELECT 投影字段的桩点。 */
public interface SelectFieldPoint extends OptionPoint {
    FieldContribution contributeSelectFields(FieldContext context);
}
