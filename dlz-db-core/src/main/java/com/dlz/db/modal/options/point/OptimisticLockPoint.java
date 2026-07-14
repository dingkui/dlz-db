package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.FieldContext;
import com.dlz.db.modal.options.point.context.FieldContribution;

/** 聚合乐观锁版本字段和值的桩点。 */
public interface OptimisticLockPoint extends OptionPoint {
    FieldContribution contributeOptimisticLock(FieldContext context);
}
