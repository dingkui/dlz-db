package com.dlz.db.option.point;

import com.dlz.db.option.point.context.FieldContext;
import com.dlz.db.option.point.context.FieldContribution;

/** 聚合 INSERT 字段和值的桩点。 */
public interface InsertFieldPoint extends OptionPoint {
    FieldContribution contributeInsertFields(FieldContext context);
}
