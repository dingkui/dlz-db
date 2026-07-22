package com.dlz.db.dialect;

import com.dlz.db.support.DBHolder;
import com.dlz.db.support.bean.TableInfo;
import com.dlz.kit.util.VAL;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** 数据库结构和字段元数据能力。 */
public interface SchemaDialect {
    void createTable(String tableName, Class<?> clazz);
    VAL<String, String[]> getTableColumnSql(String tableName);
    default Set<String> getTableColumnNames(String tableName) {
        VAL<String, String[]> value = getTableColumnSql(tableName);
        Set<String> names = new HashSet<>();
        DBHolder.getSqlExecutor().getList(value.v1, value.v2).forEach(item -> {
            String name = item.getStr("name");
            names.add(name.toLowerCase(Locale.ROOT));
            names.add(name);
        });
        return names;
    }
    TableInfo getTableInfo(String tableName);
    void createColumn(String tableName, String name, Field field);
    String getDbColumnType(Field field);
}
