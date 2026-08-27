package com.dlz.db.option.point;

import com.dlz.db.option.point.context.SqlContext;
import com.dlz.db.option.point.context.SqlStatement;

/** COUNT SQL 生成的排他桩点。 */
public interface CountPoint extends OptionPoint {
    SqlStatement buildCountSql(SqlContext context);
}
