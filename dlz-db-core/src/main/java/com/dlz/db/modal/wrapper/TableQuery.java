package com.dlz.db.modal.wrapper;

import com.dlz.db.mapper.name.IConvertorToFieldName;
import com.dlz.db.inf.IExecutorQuery;
import com.dlz.db.inf.ISqlPage;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.para.AQuery;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.SqlRunThreadHolder;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.fn.DlzFn;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 构造单表的查询操作sql
 *
 * @author dingkui
 */
@Slf4j
@SuppressWarnings("rawtypes")
public class TableQuery extends AQuery<TableQuery> implements ISqlPage<TableQuery>, IExecutorQuery<TableQuery> {
    private static final long serialVersionUID = 8374167270612933157L;
    String columns="*";

    public TableQuery(String tableName) {
        super(tableName);
    }

    public TableQuery select(String... columns) {
        if (columns.length > 0) {
            this.columns = Arrays.stream(columns)
                    .map(column -> DbConvertUtil.validateDbName(column, "查询字段"))
                    .map(com.dlz.db.util.DbConvertUtil::toDbNames)
                    .collect(Collectors.joining(","));
        }
        return this;
    }
    @SuppressWarnings("unchecked")
    public <T> TableQuery select(DlzFn<T, ?>... columns) {
        if (columns.length > 0) {
            this.columns = Arrays.stream(columns).map(PojoCache::fnName).collect(Collectors.joining(","));
        }
        return this;
    }

    @Override
    public String getSql() {
        return WrapperBuildUtil.MAKER_SQL_SEARCH;
    }

    @Override
    public TableQuery me() {
        return this;
    }

    @Override
    public TableQuery page(Page page) {
        if (page != null) {
            this.setPage(page);
        }
        return this;
    }
    public TableQuery convert(IConvertorToFieldName convertor) {
        SqlRunThreadHolder.setConvertorToFieldName(convertor);
        return this;
    }
}
