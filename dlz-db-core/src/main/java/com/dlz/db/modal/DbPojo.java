package com.dlz.db.modal;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.kit.fn.DlzFn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 8.0 Pojo 门面，直接复用现有 wrapper 执行内核。
 */
public class DbPojo {
    public <T> PojoQuery<T> selectWrapper(Class<T> type, DlzFn<T, ?>... fields) {
        final PojoQuery<T> tPojoQuery = new PojoQuery(requireType(type));
        if (fields != null) {
            tPojoQuery.select(fields);
        }
        return tPojoQuery;
    }

    public <T> PojoInsert<T> insertWrapper(Class<T> type) {
        return new PojoInsert(requireType(type));
    }

    public <T> PojoInsert<T> insertWrapper(T entity) {
        return new PojoInsert((Class<T>) requireEntity(entity).getClass()).value(entity);
    }

    public <T> PojoUpdate<T> updateWrapper(Class<T> type) {
        return new PojoUpdate(requireType(type));
    }

    public <T> PojoUpdate<T> updateWrapper(T entity) {
        return new PojoUpdate((Class<T>) requireEntity(entity).getClass()).set(entity);
    }

    public <T> PojoDelete<T> deleteWrapper(Class<T> type) {
        return new PojoDelete(requireType(type));
    }

    public <T> T insert(T entity, DbOption... options) {
        insertWrapper(entity).execute();
        return entity;
    }

    @Deprecated
    public <T> T add(T entity) {
        return insert(entity);
    }

    public <T> T insertOrUpdateById(T entity, DbOption... options) {
        requireEntity(entity);
        IdInfo idInfo = requireIdInfo(entity.getClass());
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
        requireType(type);
        requireId(id);
        String idColumn = requireIdInfo(type).getDbName();
        return selectWrapper(type).eq(idColumn, id).queryBean();
    }

    public <T> List<T> selectByIds(Class<T> type, Collection<?> ids) {
        requireType(type);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String idColumn = requireIdInfo(type).getDbName();
        return selectWrapper(type).in(idColumn, new ArrayList<Object>(ids)).queryBeanList();
    }

    public <T> List<T> selectByIds(Class<T> type, String ids) {
        requireType(type);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String idColumn = requireIdInfo(type).getDbName();
        return selectWrapper(type).in(idColumn, ids).queryBeanList();
    }

    @SuppressWarnings("unchecked")
    public <T> int updateById(T entity, DbOption... options) {
        requireEntity(entity);
        Class<T> type = (Class<T>) entity.getClass();
        IdInfo idInfo = requireIdInfo(type);
        Object id = idInfo.getValue(entity);
        requireId(id);
        return updateWrapper(type)
                .ignore((name, value) -> value == null || name.equalsIgnoreCase(idInfo.getDbName()))
                .set(entity)
                .eq(idInfo.getDbName(), id)
                .execute();
    }

    public <T> int deleteById(Class<T> type, Object id, DbOption... options) {
        requireType(type);
        requireId(id);
        String idColumn = requireIdInfo(type).getDbName();
        return deleteWrapper(type).eq(idColumn, id).execute();
    }

    public <T> int deleteByIds(Class<T> type, Collection<?> ids) {
        requireType(type);
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        String idColumn = requireIdInfo(type).getDbName();
        return deleteWrapper(type).in(idColumn, new ArrayList<Object>(ids)).execute();
    }

    public <T> int deleteByIds(Class<T> type, String ids) {
        requireType(type);
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        String idColumn = requireIdInfo(type).getDbName();
        return deleteWrapper(type).in(idColumn, ids).execute();
    }

    public <T> boolean existsById(Class<T> type, Object id) {
        return selectById(type, id) != null;
    }

    public <T> long count(Class<T> type) {
        requireType(type);
        return selectWrapper(type).count();
    }

    private <T> T requireType(T type) {
        if (type == null) {
            throw new DbParameterException("type must not be null");
        }
        return type;
    }

    private <T> T requireEntity(T entity) {
        if (entity == null) {
            throw new DbParameterException("entity must not be null");
        }
        return entity;
    }

    private IdInfo requireIdInfo(Class<?> type) {
        IdInfo idInfo = PojoCache.getIdInfo(type);
        if (idInfo == null) {
            throw new DbParameterException("entity must declare an id: " + type.getName());
        }
        return idInfo;
    }

    private void requireId(Object id) {
        if (id == null) {
            throw new DbParameterException("id must not be null");
        }
    }
}
