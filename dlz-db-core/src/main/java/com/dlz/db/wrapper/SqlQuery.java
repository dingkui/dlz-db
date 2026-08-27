package com.dlz.db.wrapper;

import com.dlz.db.internal.inf.IExecutorQuery;
import com.dlz.db.internal.inf.ISqlPage;
import com.dlz.db.mapper.name.IConvertorToFieldName;
import com.dlz.db.model.Page;
import com.dlz.db.internal.para.ParaMap;
import com.dlz.db.internal.holder.SqlRunThreadHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * 构造单表的查询操作sql
 *
 * @author dingkui
 */
@Slf4j
@SuppressWarnings("rawtypes")
public class SqlQuery extends ParaMap<SqlQuery> implements ISqlPage<SqlQuery>, IExecutorQuery<SqlQuery> {
    private static final long serialVersionUID = 8374167270612933157L;
    public SqlQuery(String sql) {
        super(sql);
    }
    @Override
    public SqlQuery me() {
        return this;
    }
    @Override
    public SqlQuery page(Page<?> page) {
        if (page != null) {
            this.setPage(page);
        }
        return this;
    }
    public SqlQuery convert(IConvertorToFieldName convertor) {
        SqlRunThreadHolder.setConvertorToFieldName(convertor);
        return this;
    }
}
