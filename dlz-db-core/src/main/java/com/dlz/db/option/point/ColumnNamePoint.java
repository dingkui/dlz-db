package com.dlz.db.option.point;

import com.dlz.db.option.point.context.NameContext;

/** Java 字段名到数据库列名的排他转换桩点。 */
public interface ColumnNamePoint extends OptionPoint {
    String mapColumnName(NameContext context);
}
