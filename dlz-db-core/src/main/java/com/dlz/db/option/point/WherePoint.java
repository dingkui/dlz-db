package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.sql.SqlFragment;

/**
 * 聚合 WHERE 条件片段的桩点。
 * <p>返回 null 表示本次不注入；可通过 {@link CrudContext#getWhereColumns()}
 * 查看调用方已声明的条件列，避免重复注入。
 */
public interface WherePoint extends OptionPoint {
    SqlFragment contributeWhere(CrudContext context);
}
