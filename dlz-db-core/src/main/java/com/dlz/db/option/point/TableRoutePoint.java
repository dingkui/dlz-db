package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;

/** 为单次操作选择物理表名的排他桩点。 */
public interface TableRoutePoint extends OptionPoint {
    String routeTable(CrudContext context);
}
