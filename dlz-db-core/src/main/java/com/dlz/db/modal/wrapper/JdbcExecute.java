package com.dlz.db.modal.wrapper;

import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.para.ParaJdbc;
import com.dlz.db.support.DBHolder;
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
