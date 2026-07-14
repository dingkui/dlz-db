package com.dlz.db.modal.wrapper;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.interceptor.DbPlugin;
import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.para.AQuery;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.ColumnInfo;
import com.dlz.db.support.bean.TableInfo;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.fn.DlzFn;
import com.dlz.kit.fn.DlzFn2;
import com.dlz.kit.json.JSONMap;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 构造单表的更新操作sql
 *
 * @author dingkui
 */
@Slf4j
public class TableUpdate extends AQuery<TableUpdate> implements IExecutorUDI {
    private static final long serialVersionUID = 8374167270612933157L;
    final Map<String, Object> updateSets = new HashMap<>();

    private DlzFn2<String, Object,Boolean> ignore = (name, value) -> value == null
            || name.equals(getTableInfo().requireSinglePrimaryKey().getDbName());
    public TableUpdate(String tableName) {
        super(tableName);
    }

    private TableUpdate setByDbName(String paraName, Object value) {
        if (!PojoCache.isColumnExists(getTableName(),paraName)) {
            log.warn("column is not exists:" + getTableName() + "." + paraName);
            return this;
        }
        updateSets.put(paraName, value);
        return this;
    }

    public TableUpdate set(String paraName, Object value) {
        return setByDbName(PojoCache.getDbName(paraName), value);
    }

    public <T> void set(DlzFn<T, ?> column, Object value) {
        setByDbName(PojoCache.fnName(column), value);
    }

    /**
     * 添加要更新的值集合
     *
     * @param setValues
     */
    public TableUpdate set(Map<String, Object> setValues) {
        for (String str : setValues.keySet()) {
            Object fieldValue = setValues.get(str);
            final String columnName = PojoCache.getDbName(str);
            if (this.ignore.apply(columnName, fieldValue)) {
                continue;
            }
            setByDbName(columnName, fieldValue);
        }
        return this;
    }
    public TableUpdate ignore(DlzFn2<String, Object,Boolean> ignore) {
        this.ignore = ignore;
        return this;
    }

    @Override
    public TableUpdate me() {
        return this;
    }

    @Override
    public String getSql() {
        return WrapperBuildUtil.MAKER_SQL_UPDATE;
    }
    public BatchResult batch(List<JSONMap> valueBeans) {
        return batch(valueBeans, 1000);
    }

    public BatchResult batch(List<JSONMap> valueBeans, int batchSize) {
        BatchResult.Builder result = BatchResult.builder(valueBeans.size(), batchSize);
        if (valueBeans.isEmpty()) {
            return result.build();
        }
        final String tableName = getTableName();
        final TableInfo tableInfo = getTableInfo();
        final ColumnInfo idColumn = tableInfo.requireSinglePrimaryKey();
        final String idFieldName = idColumn.getFieldName();
        final String idDbName = idColumn.getDbName();
        valueBeans.forEach(valueBean -> {
            final Object o = valueBean.get(idFieldName);
            if (o == null) throw new DbParameterException("id must not be null");
        });
        final HashMap<String, Integer> dbColumns = PojoCache.getTableColumnsInfo(tableName);
        final HashMap<String, Integer> sqlColumns = new LinkedHashMap<>(dbColumns.size());
        final HashMap<String, Integer> fields = new LinkedHashMap<>(dbColumns.size());
        for (ColumnInfo column : tableInfo.getColumnInfos()) {
            Integer type = dbColumns.get(column.getDbName());
            if (type != null) {
                sqlColumns.put(column.getDbName(), type);
                fields.put(column.getFieldName(), type);
            }
        }
        String sql = WrapperBuildUtil.buildUpdateSql(tableName, sqlColumns, idDbName);
        final String logicDeleteField = DbPlugin.getLogicDeleteField(tableName);

        for (int start = 0; start < valueBeans.size(); start += batchSize) {
            int end = Math.min(valueBeans.size(), start + batchSize);
            final List<JSONMap> items = valueBeans.subList(start, end);
            try {
                List<Object[]> paramValues = items.stream()
                        .map(value -> {
                            if (logicDeleteField != null && value.get(logicDeleteField) == null) {
                                value.set(logicDeleteField, 0);
                            }
                            return WrapperBuildUtil.buildUpdateParams(value, fields, idFieldName);
                        })
                        .collect(Collectors.toList());
                result.completed(start, DBHolder.getSqlExecutor().batch(sql, paramValues));
                if (!paramValues.isEmpty()) {
                    log.info(SqlUtil.getRunSqlByJdbc(sql, paramValues.get(0)).trim());
                }
                if (result.hasFailure()) {
                    break;
                }
            } catch (Throwable cause) {
                result.failed(start, cause);
                break;
            }
        }
        return result.build();
    }
}
