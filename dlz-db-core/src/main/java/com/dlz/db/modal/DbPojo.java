package com.dlz.db.modal;

import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.options.DbOperation;
import com.dlz.db.modal.options.DbOptions;
import com.dlz.db.modal.options.InsertOption;
import com.dlz.db.modal.options.UpdateOption;
import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.kit.fn.DlzFn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 8.0 Pojo 门面，直接复用现有 wrapper 执行内核。
 */
public class DbPojo {
    public <T> PojoQuery<T> selectWrapper(Class<T> type, DlzFn<T, ?>... fields) {
        final PojoQuery<T> tPojoQuery = new PojoQuery(RequireUtil.requireType(type));
        if (fields != null) {
            tPojoQuery.select(fields);
        }
        return tPojoQuery;
    }

    public <T> PojoInsert<T> insertWrapper(Class<T> type) {
        return new PojoInsert(RequireUtil.requireType(type));
    }

    public <T> PojoInsert<T> insertWrapper(T entity) {
        return new PojoInsert((Class<T>) RequireUtil.requireEntity(entity).getClass()).value(entity);
    }

    public <T> PojoUpdate<T> updateWrapper(Class<T> type) {
        return new PojoUpdate(RequireUtil.requireType(type));
    }

    public <T> PojoUpdate<T> updateWrapper(T entity) {
        return new PojoUpdate((Class<T>) RequireUtil.requireEntity(entity).getClass()).set(entity);
    }

    public <T> PojoDelete<T> deleteWrapper(Class<T> type) {
        return new PojoDelete(RequireUtil.requireType(type));
    }

    public <T> T insert(T entity, DbOption... options) {
        DbOptions resolved = DbOptions.resolve(DbOperation.INSERT, options);
        PojoInsert<T> wrapper = insertWrapper(entity);
        if (resolved.has(InsertOption.INCLUDE_NULL)) {
            wrapper.ignore((name, value) -> false);
        }
        wrapper.options(resolved);
        wrapper.execute();
        return entity;
    }

    @Deprecated
    public <T> T add(T entity) {
        return insert(entity);
    }

    public <T> T insertOrUpdateById(T entity, DbOption... options) {
        RequireUtil.requireEntity(entity);
        IdInfo idInfo = RequireUtil.requireIdInfo(entity.getClass());
        if (idInfo.getValue(entity) == null) {
            return insert(entity, options);
        }
        updateById(entity, options);
        return entity;
    }

    @Deprecated
    public <T> T save(T entity) {
        return insertOrUpdateById(entity);
    }

    public <T> T selectById(Class<T> type, Object id, DbOption... options) {
        RequireUtil.requireId(id);
        DbOptions resolved = DbOptions.resolve(DbOperation.SELECT, options);
        String idColumn = RequireUtil.requireIdInfo(type).getDbName();
        PojoQuery<T> wrapper = selectWrapper(type);
        wrapper.options(resolved);
        return wrapper.eq(idColumn, id).queryBean();
    }

    public <T> List<T> selectByIds(Class<T> type, Collection<?> ids) {
        RequireUtil.requireIds(ids);
        String idColumn = RequireUtil.requireIdInfo(type).getDbName();
        return selectWrapper(type).in(idColumn, new ArrayList<Object>(ids)).queryBeanList();
    }

    public <T> List<T> selectByIds(Class<T> type, String ids) {
        RequireUtil.requireIds(ids);
        String idColumn = RequireUtil.requireIdInfo(type).getDbName();
        return selectWrapper(type).in(idColumn, ids).queryBeanList();
    }

    @SuppressWarnings("unchecked")
    public <T> int updateById(T entity, DbOption... options) {
        RequireUtil.requireEntity(entity);
        Class<T> type = (Class<T>) entity.getClass();
        IdInfo idInfo = RequireUtil.requireIdInfo(type);
        Object id = idInfo.getValue(entity);
        RequireUtil.requireId(id);
        DbOptions resolved = DbOptions.resolve(DbOperation.UPDATE, options);
        PojoUpdate<T> wrapper = updateWrapper(type);
        if (resolved.has(UpdateOption.INCLUDE_NULL)) {
            wrapper.ignore((name, value) -> name.equalsIgnoreCase(idInfo.getDbName()));
        } else {
            wrapper.ignore((name, value) -> value == null || name.equalsIgnoreCase(idInfo.getDbName()));
        }
        wrapper.options(resolved);
        return wrapper
                .set(entity)
                .eq(idInfo.getDbName(), id)
                .execute();
    }

    public <T> int deleteById(Class<T> type, Object id, DbOption... options) {
        RequireUtil.requireId(id);
        DbOptions resolved = DbOptions.resolve(DbOperation.DELETE, options);
        String idColumn = RequireUtil.requireIdInfo(type).getDbName();
        PojoDelete<T> wrapper = deleteWrapper(type);
        wrapper.options(resolved);
        return wrapper.eq(idColumn, id).execute();
    }

    public <T> int deleteByIds(Class<T> type, Collection<?> ids) {
        RequireUtil.requireIds(ids);
        String idColumn = RequireUtil.requireIdInfo(type).getDbName();
        return deleteWrapper(type).in(idColumn, new ArrayList<Object>(ids)).execute();
    }

    public <T> int deleteByIds(Class<T> type, String ids) {
        RequireUtil.requireIds(ids);
        String idColumn = RequireUtil.requireIdInfo(type).getDbName();
        return deleteWrapper(type).in(idColumn, ids).execute();
    }

    public <T> boolean existsById(Class<T> type, Object id) {
        return selectById(type, id) != null;
    }
}
