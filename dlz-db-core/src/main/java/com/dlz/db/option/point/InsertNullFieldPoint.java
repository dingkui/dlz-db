package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.option.point.context.NullFieldMode;

/** INSERT 对 null 字段包含策略的排他桩点。 */
public interface InsertNullFieldPoint extends OptionPoint {
    NullFieldMode chooseInsertNullFields(CrudContext context);
}
