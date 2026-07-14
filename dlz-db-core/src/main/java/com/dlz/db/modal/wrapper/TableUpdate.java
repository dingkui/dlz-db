package com.dlz.db.modal.wrapper;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.interceptor.DbPlugin;
import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.para.AQuery;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.PojoCache;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.fn.DlzFn;
import com.dlz.kit.json.JSONMap;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
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

    public TableUpdate(String tableName) {
        super(tableName);
    }

    public TableUpdate set(String paraName, Object value) {
        paraName = DbConvertUtil.toDbName(paraName);
        if (!PojoCache.isColumnExists(getTableName(),paraName)) {
            log.warn("column is not exists:" + getTableName() + "." + paraName);
            return this;
        }
        updateSets.put(paraName, value);
        return this;
    }

    public <T> void set(DlzFn<T, ?> column, Object value) {
        set(PojoCache.fnName(column), value);
    }

    /**
     * 添加要更新的值集合
     *
     * @param setValues
          */
    public TableUpdate set(Map<String, Object> setValues) {
        return set(setValues,null);
    }

    public TableUpdate set(Map<String, Object> setValues, Function<String, Boolean> ignore) {
        for (String str : setValues.keySet()) {
            Object fieldValue = setValues.get(str);
            if (fieldValue != null) {
                if (ignore != null && ignore.apply(str)) {
                    continue;
                }
                set(str, setValues.get(str));
            }
        }
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
        final String idName = PojoCache.getIdDbName(tableName);
        valueBeans.forEach(valueBean -> {
            final Object o = valueBean.get(idName);
            if (o == null) throw new DbParameterException("id must not be null");
        });
        final HashMap<String, Integer> dbColumns = PojoCache.getTableColumnsInfo(tableName);
        final HashMap<String, Integer> normalizedColumns = new LinkedHashMap<>(dbColumns.size());
        dbColumns.forEach((column, type) -> normalizedColumns.put(column.toLowerCase(Locale.ROOT), type));

        String sql = WrapperBuildUtil.buildUpdateSql(tableName, normalizedColumns, DbConvertUtil.toDbName(idName));
        final HashMap<String, Integer> fields = new LinkedHashMap<>(dbColumns.size());
        dbColumns.forEach((column, type) -> fields.put(DbConvertUtil.toFieldName(column.toLowerCase(Locale.ROOT)), type));
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
                            return WrapperBuildUtil.buildUpdateParams(value, fields, idName);
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
