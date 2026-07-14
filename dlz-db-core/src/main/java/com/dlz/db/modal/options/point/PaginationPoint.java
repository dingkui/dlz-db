package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.Pagination;
import com.dlz.db.modal.options.point.context.SqlContext;
import com.dlz.db.modal.options.point.context.SqlStatement;

/** 分页 SQL 改写的排他桩点。 */
public interface PaginationPoint extends OptionPoint {
    SqlStatement applyPagination(SqlContext context, Pagination pagination);
}
