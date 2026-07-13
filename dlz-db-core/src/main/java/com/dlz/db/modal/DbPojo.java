package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.kit.exception.SystemException;
import com.dlz.kit.fn.DlzFn;
import com.dlz.kit.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DbPojo {
    public <T> PojoQuery<T> select(Class<T> re) {
        return PojoQuery.wrapper(re);
    }
    public <T> PojoQuery<T> select(Class<T> re, DlzFn<T, ?>... columns) {
        return new PojoQuery(re).select(columns);
    }
    public <T> PojoDelete<T> delete(Class<T> beanClass) {
        return new PojoDelete(beanClass);
    }

    /**
     *  默认忽略空字段
     * @param value
     * @param <T>
     */
    public <T> PojoUpdate<T> update(T value) {
        if (value == null) {
            return null;
        }
        final Class<?> aClass = value.getClass();


        return new PojoUpdate(aClass).set(value);
    }
    /**
     *  完整更新（包含空字段）
     * @param value
     * @param <T>
     */
    public <T> PojoUpdate<T> updateIntact(T value) {
        return new PojoUpdate(value.getClass()).ignore((name, val) -> false).set(value);
    }
    public <T> PojoUpdate<T> update(Class<T> beanClass) {
        return new PojoUpdate(beanClass);
    }
    public <T> PojoInsert<T> insert(T value) {
        return new PojoInsert(value.getClass()).value(value);
    }

    //以下都是直接操作执行
    public <T> T add(T bean) {
        insert(bean).execute();
        return bean;
    }

    public <T> T save(T obj) {
        final Class<T> aClass = (Class<T>) obj.getClass();
        final IdInfo idInfo = PojoCache.getIdInfo(aClass);
        if (idInfo == null) {
            throw new SystemException("无主键信息,不支持:insertOrUpdate " + aClass.getSimpleName());
        }
        final Object id = idInfo.getValue(obj);
        if (StringUtils.isEmpty(id)) {
            return add(obj);
        }
        return updateById(obj, id, aClass, idInfo);
    }

    private <T> T updateById(T obj, Object idValue, Class<T> aClass, IdInfo idInfo) {
        final String idName = idInfo.getDbName();
        if (StringUtils.isEmpty(idValue)) {
            throw new SystemException(idName + "不能为空");
        }
        final int execute = update(aClass).ignore((name, val) -> val == null||name.equalsIgnoreCase(idName)).set(obj).eq(idName, idValue).execute();
        if (execute == 1) {
            return obj;
        }
        log.warn("更新数据条数应为1,实际更新数据为:{}", execute);
        return null;
    }

    public <T> T updateById(T obj) {
        final Class<T> aClass = (Class<T>) obj.getClass();
        final IdInfo idInfo = PojoCache.getIdInfo(aClass);
        final Object id = idInfo.getValue(obj);
        return updateById(obj, id, aClass, idInfo);
    }

    public <T> T selectById(Class<T> c, Object id) {
        final String idName = PojoCache.getIdDbName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return select(c).eq(idName, id).queryBean();
    }

    public <T> List<T> selectByIds(Class<T> c, Object id) {
        final String idName = PojoCache.getIdDbName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return select(c).in(idName, id).queryBeanList();
    }

    public <T> int deleteByIds(Class<T> c, String ids) {
        final String idName = PojoCache.getIdDbName(c);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).in(idName, ids).execute();
    }

    public <T> int deleteByIds(Class<T> c, List<?> ids) {
        final String idName = PojoCache.getIdDbName(c);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).in(idName, ids).execute();
    }

    public <T> int deleteById(Class<T> c, Object id) {
        final String idName = PojoCache.getIdDbName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).eq(idName, id).execute();
    }
}