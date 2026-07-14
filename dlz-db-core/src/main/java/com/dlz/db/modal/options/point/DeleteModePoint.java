package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.DeleteMode;

/** 删除模式的排他桩点。 */
public interface DeleteModePoint extends OptionPoint {
    DeleteMode chooseDeleteMode(CrudContext context);
}
