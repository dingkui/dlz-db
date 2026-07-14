package com.dlz.db.support.bean;

import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.exception.SystemException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TableInfo {
    private String catalog;
    private String schema;
    private String tableName;
    private String tableComment;
    /** 兼容旧 API：元素始终是数据库原始列名。 */
    private List<String> primaryKeys;
    private List<ColumnInfo> columnInfos;

    private transient volatile Map<String, ColumnInfo> columnsByFieldName;
    private transient volatile Map<String, ColumnInfo> columnsByDbName;

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
        clearIndexes();
    }

    public void setColumnInfos(List<ColumnInfo> columnInfos) {
        this.columnInfos = columnInfos;
        clearIndexes();
    }

    public ColumnInfo getColumnByFieldName(String fieldName) {
        initIndexes();
        return columnsByFieldName.get(fieldName);
    }

    public ColumnInfo getColumnByDbName(String dbName) {
        initIndexes();
        return columnsByDbName.get(dbName);
    }

    public List<ColumnInfo> getPrimaryKeyColumns() {
        if (primaryKeys == null) {
            return null;
        }
        initIndexes();
        List<ColumnInfo> result = new ArrayList<>(primaryKeys.size());
        for (String primaryKey : primaryKeys) {
            ColumnInfo column = columnsByDbName.get(primaryKey);
            if (column == null) {
                column = columnsByDbName.get(primaryKey.toLowerCase());
            }
            if (column != null) {
                result.add(column);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public ColumnInfo requireSinglePrimaryKey() {
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            throw new SystemException("表[" + tableName + "]无主键");
        }
        if (primaryKeys.size() > 1) {
            throw new SystemException("表[" + tableName + "]为复合主键，不支持快捷操作，请使用Wrapper自定义条件");
        }
        List<ColumnInfo> columns = getPrimaryKeyColumns();
        if (columns == null || columns.size() != 1) {
            throw new SystemException("表[" + tableName + "]主键元数据不完整: " + primaryKeys.get(0));
        }
        return columns.get(0);
    }

    /** 创建用于共享缓存的深拷贝只读快照。 */
    public TableInfo snapshot() {
        TableInfo copy = new TableInfo();
        copy.catalog = catalog;
        copy.schema = schema;
        copy.tableName = tableName;
        copy.tableComment = tableComment;
        copy.primaryKeys = primaryKeys == null
                ? null : Collections.unmodifiableList(new ArrayList<>(primaryKeys));
        if (columnInfos == null) {
            copy.columnInfos = null;
        } else {
            List<ColumnInfo> columns = new ArrayList<>(columnInfos.size());
            for (ColumnInfo column : columnInfos) {
                columns.add(column.copy());
            }
            copy.columnInfos = Collections.unmodifiableList(columns);
        }
        copy.initIndexes();
        return copy;
    }

    private void clearIndexes() {
        columnsByFieldName = null;
        columnsByDbName = null;
    }

    private void initIndexes() {
        if (columnsByFieldName != null && columnsByDbName != null) {
            return;
        }
        synchronized (this) {
            if (columnsByFieldName != null && columnsByDbName != null) {
                return;
            }
            Map<String, ColumnInfo> byField = new LinkedHashMap<>();
            Map<String, ColumnInfo> byDb = new LinkedHashMap<>();
            if (columnInfos != null) {
                for (ColumnInfo column : columnInfos) {
                    String dbName = column.getDbName();
                    if (column.getFieldName() == null && dbName != null) {
                        column.setFieldName(DbConvertUtil.toFieldName(dbName));
                    }
                    if (column.getFieldName() != null) {
                        byField.put(column.getFieldName(), column);
                    }
                    if (dbName != null) {
                        byDb.put(dbName, column);
                        byDb.putIfAbsent(dbName.toLowerCase(), column);
                    }
                }
            }
            columnsByFieldName = Collections.unmodifiableMap(byField);
            columnsByDbName = Collections.unmodifiableMap(byDb);
        }
    }
}
