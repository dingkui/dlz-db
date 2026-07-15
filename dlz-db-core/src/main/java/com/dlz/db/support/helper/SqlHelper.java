package com.dlz.db.support.helper;

import com.dlz.db.support.DBHolder;
import com.dlz.db.support.bean.TableInfo;
import com.dlz.kit.util.VAL;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public abstract class SqlHelper {
    /**
     * 创建表
     * @param tableName
     * @param clazz
     */
    public abstract void createTable(String tableName, Class<?> clazz);

    /**
     * 获取表所有字段
     * @param tableName
          */
    public abstract VAL<String,String[]> getTableColumnSql(String tableName);

    public Set<String> getTableColumnNames(String tableName) {
        // 构建查询字段信息的SQL语句
        final VAL<String, String[]> val = getTableColumnSql(tableName);

        // 获取表所有字段
        Set<String> re = new HashSet();
        DBHolder.getSqlExecutor().getList(val.v1, val.v2).forEach(item -> {
            final String name = item.getStr("name");
            re.add(name.toLowerCase());
            re.add(name);
        });
        // 执行查询并获取结果
        return re;
    }

    /**
     * 获取表详细信息（含字段、主键、注释）
     * @param tableName
          */
    public abstract TableInfo getTableInfo(String tableName);

    /**
     * 根据bean属性创建字段
     * @param tableName
     * @param name
     * @param field
     */
    public abstract void createColumn(String tableName, String name, Field field);

    /**
     * 根据属性取得数据库字段属性
     * @param field
          */
    public abstract String getDbColumnType(Field field);
}

