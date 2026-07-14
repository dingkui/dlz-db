package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.NameContext;

/** Java 字段名到数据库列名的排他转换桩点。 */
public interface ColumnNamePoint extends OptionPoint {
    String mapColumnName(NameContext context);
}
