package com.dlz.db.wrapper;

import com.dlz.db.internal.inf.IExecutorUDI;
import com.dlz.db.internal.para.ParaMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 构造单表的查询操作sql
 *
 * @author dingkui
 */
@Slf4j
public class SqlExecute extends ParaMap<SqlExecute> implements IExecutorUDI {
    private static final long serialVersionUID = 8374167270612933157L;
    public SqlExecute(String sql) {
        super(sql);
    }
}
