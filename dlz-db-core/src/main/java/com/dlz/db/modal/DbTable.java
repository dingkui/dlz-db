package com.dlz.db.modal;

import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.db.modal.wrapper.TableQuery;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.db.support.PojoCache;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.json.JSONMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DbTable {
    public TableQuery selectWrapper(String table) {
        return new TableQuery(table);
    }

    public TableInsert insertWrapper(String table) {
        return new TableInsert(table);
    }

    public TableUpdate updateWrapper(String table) {
        return new TableUpdate(table);
    }

    public TableDelete deleteWrapper(String table) {
        return new TableDelete(table);
    }

    public int insert(String table, JSONMap values, DbOption... options) {
        RequireUtil.requireValues(values);
        return insertWrapper(table).value(values).execute();
    }

    public Long insertWithAutoKey(String table, JSONMap values, DbOption... options) {
        RequireUtil.requireValues(values);
        return insertWrapper(table).value(values).insertWithAutoKey();
    }

    public int insertOrUpdate(String table, JSONMap values, DbOption... options) {
        RequireUtil.requireValues(values);
        String idColumn = RequireUtil.requireIdColumn(table);
        Object id = values.get(idColumn);
        if (id == null) return insert(table, values, options);
        return updateWrapper(table).set(values).eq(idColumn, id).execute();
    }

    public ResultMap selectById(String table, Object id, DbOption... options) {
        RequireUtil.requireId(id);
        final String idColumn = RequireUtil.requireIdColumn(table);
        return selectWrapper(table).eq(idColumn, id).queryOne();
    }

    public List<ResultMap> selectByIds(String table, Collection<?> ids) {
        RequireUtil.requireIds(ids);
        final String idColumn = RequireUtil.requireIdColumn(table);
        return selectWrapper(table).in(idColumn, ids).queryList();
    }

    public List<ResultMap> selectByIds(String table, String ids) {
        RequireUtil.requireIds(ids);
        final String idColumn = RequireUtil.requireIdColumn(table);
        if(ids.isEmpty()){
            return new ArrayList<>();
        }
        return selectWrapper(table).in(idColumn, ids).queryList();
    }

    public int updateById(String table, JSONMap values, DbOption... options) {
        RequireUtil.requireValues(values);
        String idColumn = RequireUtil.requireIdColumn(table);
        Object id = RequireUtil.requireId(values.get(idColumn));
        return updateWrapper(table).set(values).eq(idColumn, id).execute();
    }

    public int deleteById(String table, Object id, DbOption... options) {
        RequireUtil.requireId(id);
        final String idColumn = RequireUtil.requireIdColumn(table);
        return deleteWrapper(table).eq(idColumn, id).execute();
    }

    public int deleteByIds(String table, Collection<?> ids) {
        RequireUtil.requireIds(ids);
        if(ids.isEmpty()){
            return 0;
        }
        final String idColumn = RequireUtil.requireIdColumn(table);
        return deleteWrapper(table).in(idColumn, ids).execute();
    }

    public int deleteByIds(String table, String ids) {
        RequireUtil.requireIds(ids);
        final String idColumn = RequireUtil.requireIdColumn(table);
        if(ids.trim().isEmpty()){
            return 0;
        }
        return deleteWrapper(table).in(idColumn, ids).execute();
    }
}
