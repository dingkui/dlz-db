package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;

/** 为单次操作选择数据源标识的排他桩点。 */
public interface DataSourceRoutePoint extends OptionPoint {
    String routeDataSource(CrudContext context);
}
