package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.NullFieldMode;

/** INSERT 对 null 字段包含策略的排他桩点。 */
public interface InsertNullFieldPoint extends OptionPoint {
    NullFieldMode chooseInsertNullFields(CrudContext context);
}
