package com.dlz.db.modal.wrapper;

import com.dlz.db.inf.IExecutorInsert;
import com.dlz.db.interceptor.DbPlugin;
import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.para.AParaTable;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.PojoCache;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.fn.DlzFn;
import com.dlz.kit.json.JSONMap;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 构造单表的添加操作sql
 *
 * @author dingkui
 */
@Slf4j
public class TableInsert extends AParaTable implements IExecutorInsert {
    private static final long serialVersionUID = 8374167270612933157L;
    public final Map<String, Object> insertValues = new HashMap<>();
    public TableInsert(String tableName) {
        super(tableName);
    }

    @Override
    public String getSql() {
        return WrapperBuildUtil.MAKER_SQL_INSERT;
    }

    public <T> void value(DlzFn<T, ?> column, Object value) {
        value(PojoCache.fnName(column), value);
    }

    public TableInsert value(String key, Object value) {
        String paraName = DbConvertUtil.toDbName(key);
        if (!PojoCache.isColumnExists(getTableName(), paraName)) {
            log.warn("column is not exists:" + getTableName() + "." + paraName);
            return this;
        }
        insertValues.put(paraName, value);
        return this;
    }

    public TableInsert value(Map<String, Object> values) {
        for (String str : values.keySet()) {
            value(str, values.get(str));
        }
        return this;
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
        final HashMap<String, Integer> dbColumns = PojoCache.getTableColumnsInfo(tableName);
        String sql = WrapperBuildUtil.buildInsertSql(tableName, dbColumns);

        // Table 批量插入走原生 JDBC（buildInsertSql(dbColumns) + batch）
        final String logicDeleteField = DbPlugin.getLogicDeleteField(tableName);
        final HashMap<String, Integer> fields = new LinkedHashMap<>(dbColumns.size());
        dbColumns.forEach((column, type) -> fields.put(DbConvertUtil.toFieldName(column), type));

        for (int start = 0; start < valueBeans.size(); start += batchSize) {
            int end = Math.min(valueBeans.size(), start + batchSize);
            final List<JSONMap> items = valueBeans.subList(start, end);
            try {
                List<Object[]> paramValues = items.stream()
                        .map(value -> {
                            if (logicDeleteField != null) {
                                value.set(logicDeleteField, 0);
                            }
                            return WrapperBuildUtil.buildInsertParams(value, fields);
                        })
                        .collect(Collectors.toList());
                result.completed(start, DBHolder.getSqlExecutor().batch(sql, paramValues));
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
