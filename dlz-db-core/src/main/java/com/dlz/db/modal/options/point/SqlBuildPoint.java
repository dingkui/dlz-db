package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.SqlContext;
import com.dlz.db.modal.options.point.context.SqlStatement;

/** 最终 JDBC SQL 与参数的改写链桩点。 */
public interface SqlBuildPoint extends OptionPoint {
    SqlStatement buildSql(SqlContext context);
}
