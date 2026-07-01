package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.db.modal.wrapper.TableInsert;
import com.dlz.db.modal.wrapper.TableQuery;
import com.dlz.db.modal.wrapper.TableUpdate;
import com.dlz.db.support.PojoCache;
import com.dlz.kit.exception.SystemException;
import com.dlz.kit.json.JSONMap;
import com.dlz.kit.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 表名驱动门面，无需实体。
 * <p>动态表名、报表、低代码、运维工具场景。条件用 String 字段名。
 *
 * <p>边界：不接受 Lambda（要 Lambda 用 DB.pojo）；不接受 SQL（要 SQL 用 DB.jdbc/DB.sql）。
 */
@Slf4j
public class DbTable {

    // ════════════════════════════════════════
    //  链式构建（返回 Builder，需终结方法）
    // ════════════════════════════════════════

    /** 查询构造器。 */
    public TableQuery select(String tableName) {
        return new TableQuery(tableName);
    }

    /** 更新构造器（链式 set + 条件）。 */
    public TableUpdate update(String tableName) {
        return new TableUpdate(tableName);
    }

    /** 删除构造器（链式条件）。 */
    public TableDelete delete(String tableName) {
        return new TableDelete(tableName);
    }

    /** 插入构造器（链式 set 字段）。 */
    public TableInsert insert(String tableName) {
        return new TableInsert(tableName);
    }

    // ════════════════════════════════════════
    //  快捷操作（直接执行，返回结果）
    // ════════════════════════════════════════

    /** insertOrUpdate：id 空→插，id 有→改。返回影响行数。 */
    public int save(String tableName, JSONMap value) {
        final String idName = PojoCache.getIdName(tableName);
        final Object id = value.get(idName);
        if (StringUtils.isEmpty(id)) {
            return insert(tableName).value(value).execute();
        }
        value.remove(idName);
        final int n = update(tableName).set(value).eq(idName, id).execute();
        value.put(idName, id);
        return n;
    }

    /** 按主键查单条，无结果返回 null。 */
    public com.dlz.db.modal.dto.ResultMap selectById(String tableName, Object id) {
        final String idName = PojoCache.getIdName(tableName);
        return select(tableName).eq(idName, id).queryOne();
    }

    /** 按主键查多条（可变参数）。 */
    public List<com.dlz.db.modal.dto.ResultMap> selectByIds(String tableName, Object... ids) {
        if (ids == null || ids.length == 0) {
            throw new SystemException("ids不能为空");
        }
        return selectByIds(tableName, Arrays.asList(ids));
    }

    /** 按主键查多条（Collection）。 */
    public List<com.dlz.db.modal.dto.ResultMap> selectByIds(String tableName, Collection<?> ids) {
        final String idName = PojoCache.getIdName(tableName);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return select(tableName).in(idName, ids).queryList();
    }

    /** 按主键更新，返回影响行数。 */
    public int updateById(String tableName, JSONMap value) {
        final String idName = PojoCache.getIdName(tableName);
        final Object id = value.getLong(idName);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        value.remove(idName);
        final int n = update(tableName).set(value).eq(idName, id).execute();
        value.put(idName, id);
        return n;
    }

    /** 按主键删除，返回影响行数。 */
    public int deleteById(String tableName, Object id) {
        final String idName = PojoCache.getIdName(tableName);
        if (StringUtils.isEmpty(id)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(tableName).eq(idName, id).execute();
    }

    /** 按主键删除多条（可变参数），返回影响行数。 */
    public int deleteByIds(String tableName, Object... ids) {
        if (ids == null || ids.length == 0) {
            throw new SystemException("ids不能为空");
        }
        return deleteByIds(tableName, Arrays.asList(ids));
    }

    /** 按主键删除多条（Collection），返回影响行数。 */
    public int deleteByIds(String tableName, Collection<?> ids) {
        final String idName = PojoCache.getIdName(tableName);
        if (StringUtils.isEmpty(ids)) {
            throw new SystemException(idName + "不能为空");
        }
        return delete(tableName).in(idName, ids).execute();
    }
}
