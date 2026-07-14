package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.FieldContext;
import com.dlz.db.modal.options.point.context.FieldContribution;

/** 聚合 INSERT 字段和值的桩点。 */
public interface InsertFieldPoint extends OptionPoint {
    FieldContribution contributeInsertFields(FieldContext context);
}
