package com.dlz.db.option.point;

import com.dlz.db.option.point.context.FieldContext;
import com.dlz.db.option.point.context.FieldContribution;

/** 聚合 UPDATE 字段和值的桩点。 */
public interface UpdateFieldPoint extends OptionPoint {
    FieldContribution contributeUpdateFields(FieldContext context);
}
