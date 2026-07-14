package com.dlz.db.modal;

import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.options.DbOperation;
import com.dlz.db.modal.options.DbOptions;
import com.dlz.db.modal.options.point.InsertNullFieldPoint;
import com.dlz.db.modal.options.point.UpdateNullFieldPoint;
import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.NullFieldMode;
import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.db.modal.wrapper.TableQuery;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.kit.json.JSONMap;

import java.util.ArrayList;
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
        DbOptions resolved = DbOptions.resolve(DbOperation.INSERT, options);
        TableInsert wrapper = insertWrapper(table);
        if (includeInsertNullFields(resolved, table)) {
            wrapper.ignore((name, value) -> false);
        }
        wrapper.options(resolved);
        return wrapper.value(values).execute();
    }

    public Long insertWithAutoKey(String table, JSONMap values, DbOption... options) {
        RequireUtil.requireValues(values);
        DbOptions resolved = DbOptions.resolve(DbOperation.INSERT, options);
        TableInsert wrapper = insertWrapper(table);
        if (includeInsertNullFields(resolved, table)) {
            wrapper.ignore((name, value) -> false);
        }
        wrapper.options(resolved);
        return wrapper.value(values).insertWithAutoKey();
    }

    public int insertOrUpdate(String table, JSONMap values, DbOption... options) {
        RequireUtil.requireValues(values);
        String idColumn = RequireUtil.requireIdColumn(table);
        Object id = values.get(idColumn);
        if (id == null) return insert(table, values, options);
        return updateById(table, values, options);
    }

    public ResultMap selectById(String table, Object id, DbOption... options) {
        RequireUtil.requireId(id);
        DbOptions resolved = DbOptions.resolve(DbOperation.SELECT, options);
        final String idColumn = RequireUtil.requireIdColumn(table);
        return selectWrapper(table).options(resolved).eq(idColumn, id).queryOne();
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
        DbOptions resolved = DbOptions.resolve(DbOperation.UPDATE, options);
        String idColumn = RequireUtil.requireIdColumn(table);
        Object id = RequireUtil.requireId(values.get(idColumn));
        TableUpdate wrapper = updateWrapper(table);
        if (includeUpdateNullFields(resolved, table)) {
            wrapper.ignore((name, value) -> name.equals(wrapper.getTableInfo()
                    .requireSinglePrimaryKey().getDbName()));
        }
        return wrapper.options(resolved).set(values).eq(idColumn, id).execute();
    }

    public int deleteById(String table, Object id, DbOption... options) {
        RequireUtil.requireId(id);
        DbOptions resolved = DbOptions.resolve(DbOperation.DELETE, options);
        final String idColumn = RequireUtil.requireIdColumn(table);
        return deleteWrapper(table).options(resolved).eq(idColumn, id).execute();
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

    private boolean includeInsertNullFields(DbOptions options, String tableName) {
        InsertNullFieldPoint point = options.getPointBindings().single(InsertNullFieldPoint.class);
        return point != null && point.chooseInsertNullFields(new CrudContext(
                DbOperation.INSERT, tableName, null, options)) == NullFieldMode.INCLUDE;
    }

    private boolean includeUpdateNullFields(DbOptions options, String tableName) {
        UpdateNullFieldPoint point = options.getPointBindings().single(UpdateNullFieldPoint.class);
        return point != null && point.chooseUpdateNullFields(new CrudContext(
                DbOperation.UPDATE, tableName, null, options)) == NullFieldMode.INCLUDE;
    }
}
