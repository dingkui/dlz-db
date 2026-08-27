package com.dlz.db.util;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.internal.holder.PojoCache;
import com.dlz.db.internal.bean.IdInfo;
import com.dlz.kit.json.JSONMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RequireUtil {

    public static Object requireId(Object id) {
        if (id==null) throw new DbParameterException("id must not be null");
        return id;
    }

    public static JSONMap requireValues(JSONMap values) {
        if (values == null) throw new DbParameterException("values must not be null");
        return values;
    }
    public static Collection<?> requireIds(Collection<?> ids) {
        if (ids == null) throw new DbParameterException("id must not be null");
        return ids;
    }
    public static String requireIds(String ids) {
        if (ids == null) throw new DbParameterException("id must not be null");
        return ids;
    }

    public static <T> T requireEntity(T entity) {
        if (entity == null) {
            throw new DbParameterException("entity must not be null");
        }
        return entity;
    }

    public static IdInfo requireIdInfo(Class<?> type) {
        IdInfo idInfo = PojoCache.getIdInfo(requireType(type));
        if (idInfo == null) {
            throw new DbParameterException("entity must declare an id: " + type.getName());
        }
        return idInfo;
    }

    public static String requireSqlKey(String key) {
        if (key == null || key.trim().isEmpty()) throw new DbParameterException("sql key must not be empty");
        return key;
    }
    public static String requireJdbcSql(String key) {
        if (key == null || key.trim().isEmpty()) throw new DbParameterException("jdbc sql must not be empty");
        return key;
    }
    public static <T> Class<T> requireType(Class<T> type) {
        if (type == null) throw new DbParameterException("type must not be empty");
        return type;
    }


    public static List<?> requireList(List<?> list) {
        if (list == null) throw new DbParameterException("values must not be empty");
        return list;
    }

    public static void requireBatchSize(int size) {
        if (size < 1) throw new DbParameterException("batchSize must be greater than zero");
    }


    public static String requireTableName(String table) {
        return DbConvertUtil.validateDbName(table, "表名");
    }

    public static String requireIdColumn(String table) {
        final String idFieldName = PojoCache.getIdFieldName(requireTableName(table));
        if (idFieldName == null) throw new DbParameterException("table must declare an id: " + table);
        return idFieldName;
    }

    public static List<JSONMap> requireMaps(List<?> values) {
        List<JSONMap> maps = new ArrayList<>(values.size());
        for (Object value : values) {
            if (!(value instanceof Map)) throw new DbParameterException("table batch values must be Map");
            maps.add(new JSONMap(value));
        }
        return maps;
    }
}
