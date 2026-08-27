package com.dlz.db.option.point;

import com.dlz.db.option.point.context.Pagination;
import com.dlz.db.option.point.context.SqlContext;
import com.dlz.db.option.point.context.SqlStatement;

/** 分页 SQL 改写的排他桩点。 */
public interface PaginationPoint extends OptionPoint {
    SqlStatement applyPagination(SqlContext context, Pagination pagination);
}
