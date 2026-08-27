package com.dlz.db.option.point;

import com.dlz.db.internal.items.SqlFragment;
import com.dlz.db.option.point.context.CrudContext;

/** 聚合 WHERE 条件片段的桩点。 */
public interface WherePoint extends OptionPoint {
    SqlFragment contributeWhere(CrudContext context);
}
