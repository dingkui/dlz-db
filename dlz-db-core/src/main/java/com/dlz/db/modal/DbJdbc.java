package com.dlz.db.modal;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.dto.PageRequest;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.wrapper.JdbcSelect;
import com.dlz.db.support.DBHolder;
import com.dlz.db.util.DbConvertUtil;

import java.util.Collections;
import java.util.List;

public class DbJdbc {
    public JdbcSelect selectWrapper(String sql, Object... params) { requireSql(sql); return new JdbcSelect(sql, params); }
    public int execute(String sql, Object... params) { return DBHolder.getSqlExecutor().update(requireSql(sql), params); }
    public Long executeAndReturnId(String sql, Object... params) { return DBHolder.getSqlExecutor().updateForId(requireSql(sql), params); }
    public ResultMap one(String sql, Object... params) { return DBHolder.getSqlExecutor().getOne(requireSql(sql), true, params); }
    public <T> T oneAs(String sql, Class<T> type, Object... params) { requireType(type); return DbConvertUtil.getFirstColumn(one(sql, params), type); }
    public List<ResultMap> list(String sql, Object... params) { return DBHolder.getSqlExecutor().getList(requireSql(sql), params); }
    public <T> List<T> listAs(String sql, Class<T> type, Object... params) { requireType(type); return DbConvertUtil.getColumnList(list(sql, params), type); }
    public long count(String sql, Object... params) { return DBHolder.getSqlExecutor().getFirstColumn(requireSql(sql), Long.class, params); }
    public Page<ResultMap> page(String sql, PageRequest request, Object... params) { return page(selectWrapper(sql, params), request); }
    public <T> Page<T> pageAs(String sql, PageRequest request, Class<T> type, Object... params) { requireType(type); return pageAsInternal(selectWrapper(sql, params), request, type); }
    @SuppressWarnings("unchecked")
    private Page<ResultMap> page(JdbcSelect wrapper, PageRequest request) { requireRequest(request); com.dlz.db.modal.dto.Page<ResultMap> page = DBHolder.getService().getPage(wrapper.page(com.dlz.db.modal.dto.Page.build(request.pageNo(), request.pageSize()))); return Page.of(page.getRecords() == null ? Collections.<ResultMap>emptyList() : page.getRecords(), page.getTotal(), request); }
    private <T> Page<T> pageAsInternal(JdbcSelect wrapper, PageRequest request, Class<T> type) { requireRequest(request); com.dlz.db.modal.dto.Page<T> page = DBHolder.getService().getPage(wrapper.page(com.dlz.db.modal.dto.Page.build(request.pageNo(), request.pageSize())), type); return Page.of(page.getRecords() == null ? Collections.<T>emptyList() : page.getRecords(), page.getTotal(), request); }
    private String requireSql(String sql) { if (sql == null || sql.trim().isEmpty()) throw new DbParameterException("sql must not be empty"); return sql; }
    private void requireType(Class<?> type) { if (type == null) throw new DbParameterException("type must not be null"); }
    private void requireRequest(PageRequest request) { if (request == null) throw new DbParameterException("request must not be null"); }
}
