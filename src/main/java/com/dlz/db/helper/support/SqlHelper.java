package com.dlz.db.helper.support;

import com.dlz.db.helper.bean.TableInfo;
import com.dlz.db.modal.dto.ResultMap;

import java.lang.reflect.Field;
import java.util.List;
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
    public abstract Set<String> getTableColumnNames(String tableName);

    /**
     * 获取表所有索引
     * @param tableName
          */
    public abstract List<ResultMap> getTableIndexs(String tableName);

    /**
     * 获取表所有索引
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
     * 更新默认值
     * @param tableName
     * @param columnName
     * @param value
     */
    public abstract void updateDefaultValue(String tableName, String columnName, String value);

    /**
     * 根据属性取得数据库字段属性
     * @param field
          */
    public abstract String getDbClumnType(Field field);
}
