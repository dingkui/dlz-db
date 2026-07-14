package com.dlz.db.support.bean;

import com.dlz.db.util.DbConvertUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColumnInfo {
    /** Java/JSON 字段名。 */
    private String fieldName;
    /** 数据库原始列名。 */
    private String dbName;
    private String columnType;
    private String columnComment;
    private Class<?> javaType;
    /** 列长度（VARCHAR(255) 的 255） */
    private int columnSize;
    /** 小数位（DECIMAL(10,2) 的 2） */
    private int decimalDigits;
    /** 是否可空 */
    private boolean nullable = true;
    /** 默认值 */
    private String defaultValue;
    /** 是否自增 */
    private boolean autoIncrement;
    /** 是否主键（由 TableInfo.primaryKeys 推导） */
    private boolean primaryKey;

    /**
     * 兼容旧 API：columnName 始终表示数据库列名。
     */
    public String getColumnName() {
        return dbName;
    }

    /**
     * 兼容旧 API，并在读取数据库元数据时一次性派生 Java 字段名。
     */
    public void setColumnName(String columnName) {
        this.dbName = columnName;
        this.fieldName = columnName == null ? null : DbConvertUtil.toFieldName(columnName);
    }

    public void setDbName(String dbName) {
        setColumnName(dbName);
    }

    public ColumnInfo copy() {
        ColumnInfo copy = new ColumnInfo();
        copy.fieldName = fieldName;
        copy.dbName = dbName;
        copy.columnType = columnType;
        copy.columnComment = columnComment;
        copy.javaType = javaType;
        copy.columnSize = columnSize;
        copy.decimalDigits = decimalDigits;
        copy.nullable = nullable;
        copy.defaultValue = defaultValue;
        copy.autoIncrement = autoIncrement;
        copy.primaryKey = primaryKey;
        return copy;
    }
}

