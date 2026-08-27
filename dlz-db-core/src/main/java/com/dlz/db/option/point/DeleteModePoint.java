package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.option.point.context.DeleteMode;

/** 删除模式的排他桩点。 */
public interface DeleteModePoint extends OptionPoint {
    DeleteMode chooseDeleteMode(CrudContext context);
}
