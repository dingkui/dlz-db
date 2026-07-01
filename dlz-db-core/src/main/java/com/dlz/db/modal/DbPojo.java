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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 实体驱动门面，Lambda 类型安全。
 * <p>有实体类时用，IDE 自动补全 + 重构安全。
 *
 * <p>方法分两类：
 * <ul>
 *   <li>链式构建：select/update/delete（条件多变时用），需终结方法收口</li>
 *   <li>快捷操作：insert/save/*ById（条件固定或无需条件时用），直接执行返回结果</li>
 * </ul>
 */
@Slf4j
public class DbPojo {

    // ════════════════════════════════════════
    //  链式构建（返回 Builder，需终结方法）
    // ════════════════════════════════════════

    /** 查询构造器（查所有字段）。 */
    public <T> PojoQuery<T> select(Class<T> clazz) {
        return PojoQuery.wrapper(clazz);
    }

    /** 查询构造器（指定字段）。 */
    public <T> PojoQuery<T> select(Class<T> clazz, DlzFn<T, ?>... fields) {
        return PojoQuery.wrapper(clazz).select(fields);
    }

    /** 更新构造器（按 Class，链式 set 字段 + 条件）。 */
    public <T> PojoUpdate<T> update(Class<T> clazz) {
        return new PojoUpdate<>(clazz);
    }

    /** 删除构造器（链式条件）。 */
    public <T> PojoDelete<T> delete(Class<T> clazz) {
        return PojoDelete.wrapper(clazz);
    }

    // ════════════════════════════════════════
    //  快捷操作（直接执行，返回结果）
    // ════════════════════════════════════════

    /**
     * 强制插入（不管 id 有无值，覆盖手动主键场景），回填自增 id。
     * <p>默认忽略 null 字段。
     */
    public <T> T insert(T entity) {
        new PojoInsert(entity).execute();
        return entity;
    }

    /**
     * 强制插入，带选项。
     * <pre>DB.pojo.insert(user, InsertOption.IGNORE_NULL, InsertOption.ON_DUPLICATE_UPDATE);</pre>
     */
    public <T> T insert(T entity, InsertOption... options) {
        // TODO: 按 options 处理（IGNORE_NULL/INCLUDE_NULL/ON_DUPLICATE_UPDATE/ON_DUPLICATE_IGNORE）
        return insert(entity);
    }

    /**
     * insertOrUpdate：id 空→插，id 有→改。回填 id。
     * <p>注意：手动主键 + 表中无记录时 save 会误判为 update（0 行），此场景用 {@link #insert}。
     */
    public <T> T save(T entity) {
        final Class<T> aClass = (Class<T>) entity.getClass();
        final IdInfo idInfo = PojoCache.getIdInfo(aClass);
        if (idInfo == null) {
            throw new SystemException("无主键信息,不支持:save " + aClass.getSimpleName());
        }
        final Object id = idInfo.getValue(entity);
        if (StringUtils.isEmpty(id)) {
            return insert(entity);
        }
        return updateById(entity);
    }

    /** 按主键查单条，无结果返回 null。 */
    public <T> T selectById(Class<T> c, Object id) {
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return select(c).eq(idName, id).queryBean();
    }

    /** 按主键查多条（可变参数）。 */
    public <T> List<T> selectByIds(Class<T> c, Object... ids) {
        if (ids == null || ids.length == 0) {
            throw new SystemException("ids不能为空");
        }
        return selectByIds(c, Arrays.asList(ids));
    }

    /** 按主键查多条（Collection）。 */
    public <T> List<T> selectByIds(Class<T> c, Collection<?> ids) {
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return select(c).in(idName, ids).queryBeanList();
    }

    /** 按主键更新（实体字段写入，主键为条件），返回影响行数。 */
    public <T> int updateById(T entity) {
        final Class<T> aClass = (Class<T>) entity.getClass();
        final IdInfo idInfo = PojoCache.getIdInfo(aClass);
        if (idInfo == null) {
            throw new SystemException("无主键信息,不支持:updateById " + aClass.getSimpleName());
        }
        final Object id = idInfo.getValue(entity);
        final String idName = idInfo.getName();
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return update(aClass).set(entity, name -> name.equalsIgnoreCase(idName)).eq(idName, id).execute();
    }

    /** 按主键删除，返回影响行数。 */
    public <T> int deleteById(Class<T> c, Object id) {
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).eq(idName, id).execute();
    }

    /** 按主键删除多条（可变参数），返回影响行数。 */
    public <T> int deleteByIds(Class<T> c, Object... ids) {
        if (ids == null || ids.length == 0) {
            throw new SystemException("ids不能为空");
        }
        return deleteByIds(c, Arrays.asList(ids));
    }

    /** 按主键删除多条（Collection），返回影响行数。 */
    public <T> int deleteByIds(Class<T> c, Collection<?> ids) {
        final String idName = PojoCache.getIdName(c);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(c).in(idName, ids).execute();
    }
}
