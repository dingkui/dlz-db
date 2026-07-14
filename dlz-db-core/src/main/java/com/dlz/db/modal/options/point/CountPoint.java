package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.SqlContext;
import com.dlz.db.modal.options.point.context.SqlStatement;

/** COUNT SQL 生成的排他桩点。 */
public interface CountPoint extends OptionPoint {
    SqlStatement buildCountSql(SqlContext context);
}
