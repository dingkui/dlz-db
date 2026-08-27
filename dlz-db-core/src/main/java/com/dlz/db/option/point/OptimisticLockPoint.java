package com.dlz.db.option.point;

import com.dlz.db.option.point.context.FieldContext;
import com.dlz.db.option.point.context.FieldContribution;

/** 聚合乐观锁版本字段和值的桩点。 */
public interface OptimisticLockPoint extends OptionPoint {
    FieldContribution contributeOptimisticLock(FieldContext context);
}
