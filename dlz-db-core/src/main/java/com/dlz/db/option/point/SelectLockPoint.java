package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.option.point.context.SelectLockMode;

/** SELECT 锁策略的排他桩点。 */
public interface SelectLockPoint extends OptionPoint {
    SelectLockMode chooseSelectLock(CrudContext context);
}
