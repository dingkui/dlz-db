package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.FieldContext;
import com.dlz.db.modal.options.point.context.FieldContribution;

/** 聚合 SELECT 投影字段的桩点。 */
public interface SelectFieldPoint extends OptionPoint {
    FieldContribution contributeSelectFields(FieldContext context);
}
