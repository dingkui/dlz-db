package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;

/** 为单次操作选择数据源标识的排他桩点。 */
public interface DataSourceRoutePoint extends OptionPoint {
    String routeDataSource(CrudContext context);
}
