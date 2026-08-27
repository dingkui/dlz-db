package com.dlz.db.wrapper;

import com.dlz.db.internal.inf.IExecutorQuery;
import com.dlz.db.internal.inf.ISqlPage;
import com.dlz.db.internal.inf.ISqlPara;
import com.dlz.db.model.Page;
import com.dlz.db.internal.para.ParaJdbc;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * 构造单表的查询操作sql
 *
 * @author dingkui
 */
@Slf4j
public class JdbcSelect extends ParaJdbc implements Serializable, ISqlPara, ISqlPage<JdbcSelect>, IExecutorQuery<JdbcSelect> {
    private static final long serialVersionUID = 8374167270612933157L;
    public JdbcSelect(String sql, Object... paras) {
        super(sql, paras);
    }
    @Override
    public JdbcSelect page(Page<?> page) {
        if (page != null) {
            super.setPage(page);
        }
        return this;
    }

    @Override
    public JdbcSelect me() {
        return this;
    }
}
