package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.kit.exception.SystemException;
import com.dlz.kit.util.StringUtils;
import com.dlz.kit.util.system.FieldReflections;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

@Slf4j
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
     * @param <T>
     * @return
     */
    public <T> PojoUpdate<T> update(T value, Function<String, Boolean> ignore) {
        return PojoUpdate.wrapper((Class<T>) value.getClass()).set(value, ignore);
    }

    public <T> PojoUpdate<T> update(T value) {
        return PojoUpdate.wrapper((Class<T>) value.getClass()).set(value);
    }

    //以下都是直接操作执行
    public <T> T insert(T bean) {
        if (PojoInsert.wrapper(bean).execute() == 1) {
            return bean;
        }
        throw new SystemException("插入失败");
    }

    public <T> T insertOrUpdate(T obj) {
        final Class<T> aClass = (Class<T>) obj.getClass();
        final IdInfo idInfo = PojoCache.getIdInfo(aClass);
        if (idInfo == null) {
            throw new SystemException("无主键信息,不支持:insertOrUpdate " + aClass.getSimpleName());
        }
        final Object id = idInfo.getValue(obj);
        if (StringUtils.isEmpty(id)) {
            return insert(obj);
        }
        return updateById(obj, id, aClass, idInfo);
    }

    private <T> T updateById(T obj, Object idValue, Class<T> aClass, IdInfo idInfo) {
        final String idName = idInfo.getName();
        if (StringUtils.isEmpty(idValue)) {
            throw new SystemException(idName + "不能为空");
        }
        final int execute = update(aClass).set(obj, name -> name.equalsIgnoreCase(idName)).eq(idName, idValue).execute();
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
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return select(c).eq(idName, id).queryBean();
    }

    public <T> List<T> selectByIds(Class<T> c, Object id) {
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return select(c).in(idName, id).queryBeanList();
    }

    public <T> int deleteByIds(Class<T> c, String ids) {
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).in(idName, ids).execute();
    }

    public <T> int deleteByIds(Class<T> c, List<?> ids) {
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).in(idName, ids).execute();
    }

    public <T> int deleteById(Class<T> c, Object id) {
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).eq(idName, id).execute();
    }
}