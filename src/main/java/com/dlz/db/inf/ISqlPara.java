package com.dlz.db.inf;

import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.items.SqlItem;

/**
 * 参数转sql构造器
 */
public interface ISqlPara {
    JdbcItem jdbcSql();

    JdbcItem jdbcCnt();

    SqlItem getSqlItem();
}
