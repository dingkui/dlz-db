package com.dlz.db.modal;

import com.dlz.comm.exception.SystemException;
import com.dlz.comm.util.StringUtils;
import com.dlz.comm.util.system.FieldReflections;
import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.util.DbEntityUtil;

import java.util.function.Function;

public class DbPojo {
    public <T> PojoQuery<T> select(Class<T> re) {
        return PojoQuery.wrapper(re);
    }

    public <T> PojoQuery<T> select(T conditionBean) {
        return PojoQuery.wrapper(conditionBean);
    }

    public <T> PojoDelete<T> delete(Class<T> beanClass) {
        return PojoDelete.wrapper(beanClass);
    }

    public <T> PojoDelete<T> delete(T condition) {
        return PojoDelete.wrapper(condition);
    }

    public <T> PojoUpdate<T> update(Class<T> beanClass) {
        return PojoUpdate.wrapper(beanClass);
    }

    /**
     *
     * @param value
     * @param ignore 字段忽略规则
     * @return
     * @param <T>
     */
    public <T> PojoUpdate<T> update(T value, Function<String, Boolean> ignore) {
        return PojoUpdate.wrapper((Class<T>) value.getClass()).set(value, ignore);
    }

    public <T> PojoUpdate<T> update(T value) {
        return PojoUpdate.wrapper((Class<T>) value.getClass()).set(value);
    }

    //以下都是直接操作执行
    public <T> int insert(T bean) {
        return PojoInsert.wrapper(bean).execute();
    }
    public <T> T insertBean(T bean) {
        final int execute = PojoInsert.wrapper(bean).execute();
        return bean;
    }
    public <T> int insertOrUpdate(T obj) {
        final Class<T> aClass = (Class<T>) obj.getClass();
        final DbEntityUtil.IdInfo idInfo = DbEntityUtil.getIdInfo(aClass);
        final Object id = FieldReflections.getValue(obj, idInfo.getField());
        final String idName = idInfo.getName();
        if (StringUtils.isEmpty(id)) {
            return insert(obj);
        }
        return update(aClass).set(obj, name -> name.equalsIgnoreCase(idName)).eq(idName, id).execute();
    }

    public <T> int updateById(T obj) {
        final Class<T> aClass = (Class<T>) obj.getClass();
        final DbEntityUtil.IdInfo idInfo = DbEntityUtil.getIdInfo(aClass);
        final Object id = FieldReflections.getValue(obj, idInfo.getField());
        final String idName = idInfo.getName();
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return update(aClass).set(obj, name -> name.equalsIgnoreCase(idName)).eq(idName, id).execute();
    }

    public <T> T getById(Class<T> c, Object id) {
        final String idName = DbEntityUtil.getIdName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return select(c).eq(idName, id).queryBean();
    }

    public <T> int deleteByIds(Class<T> c, String ids) {
        final String idName = DbEntityUtil.getIdName(c);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).in(idName, ids).execute();
    }

    public <T> int deleteById(Class<T> c, Object id) {
        final String idName = DbEntityUtil.getIdName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).in(idName, id).execute();
    }
}