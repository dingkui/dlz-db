package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.DeletedDataMode;

/** 已删除数据查询策略的排他桩点。 */
public interface DeletedDataPoint extends OptionPoint {
    DeletedDataMode chooseDeletedData(CrudContext context);
}
