package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.SelectLockMode;

/** SELECT 锁策略的排他桩点。 */
public interface SelectLockPoint extends OptionPoint {
    SelectLockMode chooseSelectLock(CrudContext context);
}
