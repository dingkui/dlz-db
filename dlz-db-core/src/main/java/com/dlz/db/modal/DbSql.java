package com.dlz.db.modal;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.dto.PageRequest;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.wrapper.SqlQuery;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.SqlHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbSql {
    public SqlQuery selectWrapper(String sqlKey) { return new SqlQuery(SqlHolder.getSql(requireSqlKey(sqlKey))); }
    public SqlQuery select(String sqlKey) { return selectWrapper(sqlKey); }
    public int execute(String sqlKey, Map<String, ?> params) { return DBHolder.getService().executeSql(SqlHolder.getSql(requireSqlKey(sqlKey)), values(params)); }
    public Long executeAndReturnId(String sqlKey, Map<String, ?> params) { return DBHolder.getSqlExecutor().updateForId(SqlHolder.getSql(requireSqlKey(sqlKey)), values(params)); }
    public ResultMap one(String sqlKey, Map<String, ?> params) { return DBHolder.getService().getMap(query(sqlKey, params), true); }
    public <T> T one(String sqlKey, Map<String, ?> params, Class<T> type) { if (type == null) throw new DbParameterException("type must not be null"); return DBHolder.getService().getBean(query(sqlKey, params), type, true); }
    public List<ResultMap> list(String sqlKey, Map<String, ?> params) { return DBHolder.getService().getMapList(query(sqlKey, params)); }
    public <T> List<T> list(String sqlKey, Map<String, ?> params, Class<T> type) { if (type == null) throw new DbParameterException("type must not be null"); return DBHolder.getService().getBeanList(query(sqlKey, params), type); }
    public long count(String sqlKey, Map<String, ?> params) { return DBHolder.getService().getCnt(query(sqlKey, params)); }
    public Page<ResultMap> page(String sqlKey, Map<String, ?> params, PageRequest request) { return pageInternal(query(sqlKey, params), request, null); }
    public <T> Page<T> page(String sqlKey, Map<String, ?> params, PageRequest request, Class<T> type) { if (type == null) throw new DbParameterException("type must not be null"); return pageInternal(query(sqlKey, params), request, type); }
    private SqlQuery query(String key, Map<String, ?> params) { return new SqlQuery(SqlHolder.getSql(requireSqlKey(key))).addParas(values(params)); }
    private Map<String, Object> values(Map<String, ?> params) { if (params == null) return Collections.emptyMap(); return new HashMap<String, Object>(params); }
    @SuppressWarnings("unchecked")
    private <T> Page<T> pageInternal(SqlQuery query, PageRequest request, Class<T> type) { if (request == null) throw new DbParameterException("request must not be null"); com.dlz.db.modal.dto.Page<T> page = type == null ? (com.dlz.db.modal.dto.Page<T>) DBHolder.getService().getPage(query.page(com.dlz.db.modal.dto.Page.build(request.pageNo(), request.pageSize()))) : DBHolder.getService().getPage(query.page(com.dlz.db.modal.dto.Page.build(request.pageNo(), request.pageSize())), type); return Page.of(page.getRecords() == null ? Collections.<T>emptyList() : page.getRecords(), page.getTotal(), request); }
    private String requireSqlKey(String key) { if (key == null || key.trim().isEmpty()) throw new DbParameterException("sql key must not be empty"); return key; }
}
