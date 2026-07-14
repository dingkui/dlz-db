package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;

/** 为单次操作选择物理表名的排他桩点。 */
public interface TableRoutePoint extends OptionPoint {
    String routeTable(CrudContext context);
}
