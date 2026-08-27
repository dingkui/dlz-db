package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.option.point.context.DeletedDataMode;

/** 已删除数据查询策略的排他桩点。 */
public interface DeletedDataPoint extends OptionPoint {
    DeletedDataMode chooseDeletedData(CrudContext context);
}
