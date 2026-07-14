package com.dlz.db.modal.options.point;

import com.dlz.db.modal.items.SqlFragment;
import com.dlz.db.modal.options.point.context.CrudContext;

/** 聚合 WHERE 条件片段的桩点。 */
public interface WherePoint extends OptionPoint {
    SqlFragment contributeWhere(CrudContext context);
}
