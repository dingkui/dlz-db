package com.dlz.db.modal;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.db.modal.wrapper.TableQuery;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.PojoCache;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.json.JSONMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DbTable {
    public TableQuery selectWrapper(String table) { return new TableQuery(tableName(table)); }
    public TableInsert insertWrapper(String table) { return new TableInsert(tableName(table)); }
    public TableUpdate updateWrapper(String table) { return new TableUpdate(tableName(table)); }
    public TableDelete deleteWrapper(String table) { return new TableDelete(tableName(table)); }

    public int insert(String table, JSONMap values, DbOption... options) {
        requireValues(values);
        return insertWrapper(table).value(values).execute();
    }

    public Long insertWithAutoKey(String table, JSONMap values, DbOption... options) {
        requireValues(values);
        return insertWrapper(table).value(values).insertWithAutoKey();
    }

    public int insertOrUpdate(String table, JSONMap values, DbOption... options) {
        requireValues(values);
        String idColumn = idColumn(table);
        Object id = values.get(idColumn);
        if (id == null) return insert(table, values, options);
        Object removed = values.remove(idColumn);
        try {
            return updateWrapper(table).set(values).eq(idColumn, id).execute();
        } finally {
            values.put(idColumn, removed);
        }
    }

    public ResultMap selectById(String table, Object id, DbOption... options) {
        requireId(id);
        return selectWrapper(table).eq(idColumn(table), id).queryOne();
    }

    public List<ResultMap> selectByIds(String table, Collection<?> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return selectWrapper(table).in(idColumn(table), ids).queryList();
    }

    public List<ResultMap> selectByIds(String table, String ids) {
        if (ids == null || ids.trim().isEmpty()) return Collections.emptyList();
        return selectWrapper(table).in(idColumn(table), ids).queryList();
    }

    public int updateById(String table, JSONMap values, DbOption... options) {
        requireValues(values);
        String idColumn = idColumn(table);
        Object id = values.get(idColumn);
        requireId(id);
        Object removed = values.remove(idColumn);
        try {
            return updateWrapper(table).set(values).eq(idColumn, id).execute();
        } finally {
            values.put(idColumn, removed);
        }
    }

    public int deleteById(String table, Object id, DbOption... options) {
        requireId(id);
        return deleteWrapper(table).eq(idColumn(table), id).execute();
    }

    public int deleteByIds(String table, Collection<?> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return deleteWrapper(table).in(idColumn(table), ids).execute();
    }
    public int deleteByIds(String table, String ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return deleteWrapper(table).in(idColumn(table), ids).execute();
    }

    public boolean existsById(String table, Object id) {
        return selectById(table, id) != null;
    }

    public long count(String table) {
        return DBHolder.getService().getLong("SELECT COUNT(*) FROM " + tableName(table));
    }

    private String tableName(String table) {
        return DbConvertUtil.validateDbName(table, "表名");
    }

    private String idColumn(String table) {
        return PojoCache.getIdDbName(tableName(table));
    }

    private void requireId(Object id) {
        if (id == null) throw new DbParameterException("id must not be null");
    }

    private void requireValues(JSONMap values) {
        if (values == null || values.isEmpty()) throw new DbParameterException("values must not be empty");
    }
}
