package com.dlz.db.wrapper;

import com.dlz.db.internal.inf.IExecutorUDI;
import com.dlz.db.internal.items.JdbcItem;
import com.dlz.db.internal.para.ParaJdbc;
import com.dlz.db.internal.holder.DBHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * JDBC执行操作sql
 *
 * @author dingkui
 */
@Slf4j
public class JdbcExecute extends ParaJdbc implements IExecutorUDI {
    private static final long serialVersionUID = 8374167270612933157L;
    public JdbcExecute(String sql, Object... paras) {
        super(sql,paras);
    }

    public Long executeAndReturnId() {
        JdbcItem jdbcItem = jdbcSql();
        return DBHolder.getSqlExecutor().updateForId(jdbcItem.sql, jdbcItem.paras);
    }
}
