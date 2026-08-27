package com.dlz.db.internal.mapper.dbtype;

public interface ITableColumnMapper {
    /**
     * 取得字段对应的类型的值
     *
     * @param  tableName 表名
     * @param  columnName 字段名
     * @param  value  字段值
     */
     Object convertObj4Db(String tableName, String columnName, Object value);
}
