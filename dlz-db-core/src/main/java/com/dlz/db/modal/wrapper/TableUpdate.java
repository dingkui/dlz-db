package com.dlz.db.modal.wrapper;

import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.para.AQuery;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.fn.DlzFn;
import com.dlz.kit.json.JSONMap;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        paraName = DbConvertUtil.toDbColumnName(paraName);
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
        for (String str : setValues.keySet()) {
            set(str, setValues.get(str));
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
    public boolean batch(List<JSONMap> valueBeans) {
        return batch(valueBeans, 1000);
    }
    public boolean batch(List<JSONMap> valueBeans, int batchSize) {
        if (valueBeans.isEmpty()) {
            return true;
        }
        final String idName = PojoCache.getIdName(getTableName());
        final HashMap<String, Integer> dbColumns = PojoCache.getTableColumnsInfo(getTableName());
        String sql = WrapperBuildUtil.buildUpdateSql(getTableName(), dbColumns, idName);
        final HashMap<String, Integer> fields = new LinkedHashMap<>(dbColumns.size());
        dbColumns.entrySet().forEach(entry -> fields.put(DbConvertUtil.toFieldName(entry.getKey()), entry.getValue()));
        while (!valueBeans.isEmpty() && batchSize > 0) {
            if (batchSize > valueBeans.size()) {
                batchSize = valueBeans.size();
            }
            final List<JSONMap> ts = valueBeans.subList(0, batchSize);

            List<Object[]> paramValues = ts.stream()
                    .map(v -> WrapperBuildUtil.buildUpdateParams(v, fields, idName))
                    .collect(Collectors.toList());
            DBHolder.getSqlExecutor().batch(sql, paramValues);
            valueBeans = valueBeans.subList(batchSize, valueBeans.size());
        }
        return true;
    }
}
