package com.dlz.db.modal;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.json.JSONMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class RequireUtil {

    static Object requireId(Object id) {
        if (id==null) throw new DbParameterException("id must not be null");
        return id;
    }

    static JSONMap requireValues(JSONMap values) {
        if (values == null) throw new DbParameterException("values must not be null");
        return values;
    }
    static Collection<?> requireIds(Collection<?> ids) {
        if (ids == null) throw new DbParameterException("id must not be null");
        return ids;
    }
    static String requireIds(String ids) {
        if (ids == null) throw new DbParameterException("id must not be null");
        return ids;
    }


    static <T> T requireType(T type) {
        if (type == null) {
            throw new DbParameterException("type must not be null");
        }
        return type;
    }

    static <T> T requireEntity(T entity) {
        if (entity == null) {
            throw new DbParameterException("entity must not be null");
        }
        return entity;
    }

    static IdInfo requireIdInfo(Class<?> type) {
        IdInfo idInfo = PojoCache.getIdInfo(requireType(type));
        if (idInfo == null) {
            throw new DbParameterException("entity must declare an id: " + type.getName());
        }
        return idInfo;
    }

    static String requireSqlKey(String key) {
        if (key == null || key.trim().isEmpty()) throw new DbParameterException("sql key must not be empty");
        return key;
    }
    static String requireJdbcSql(String key) {
        if (key == null || key.trim().isEmpty()) throw new DbParameterException("jdbc sql must not be empty");
        return key;
    }
    static <T> Class<T> requireType(Class<T> type) {
        if (type == null) throw new DbParameterException("type must not be empty");
        return type;
    }


    static List<?> requireList(List<?> list) {
        if (list == null) throw new DbParameterException("values must not be empty");
        return list;
    }

    static void requireBatchSize(int size) {
        if (size < 1) throw new DbParameterException("batchSize must be greater than zero");
    }


    static String requireTableName(String table) {
        return DbConvertUtil.validateDbName(table, "表名");
    }

    static String requireIdColumn(String table) {
        final String idDbName = PojoCache.getIdDbName(requireTableName(table));
        if (idDbName == null) throw new DbParameterException("table must declare an id: " + table);
        return idDbName;
    }

    static List<JSONMap> requireMaps(List<?> values) {
        List<JSONMap> maps = new ArrayList<>(values.size());
        for (Object value : values) {
            if (!(value instanceof Map)) throw new DbParameterException("table batch values must be Map");
            maps.add(new JSONMap(value));
        }
        return maps;
    }
}
