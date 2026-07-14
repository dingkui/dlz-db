package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.NullFieldMode;

/** UPDATE 对 null 字段包含策略的排他桩点。 */
public interface UpdateNullFieldPoint extends OptionPoint {
    NullFieldMode chooseUpdateNullFields(CrudContext context);
}
