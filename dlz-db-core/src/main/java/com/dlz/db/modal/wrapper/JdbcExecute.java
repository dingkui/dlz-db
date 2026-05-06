package com.dlz.db.modal.wrapper;

import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.modal.para.ParaJdbc;
import lombok.extern.slf4j.Slf4j;

/**
 * 构造单表的查询操作sql
 *
 * @author dingkui
 */
@Slf4j
public class JdbcExecute extends ParaJdbc implements IExecutorUDI {
    private static final long serialVersionUID = 8374167270612933157L;
    public JdbcExecute(String sql, Object... paras) {
        super(sql,paras);
    }
}
