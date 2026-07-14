package com.dlz.db.modal.wrapper;

import com.dlz.db.annotation.IdType;
import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.interceptor.DbPlugin;
import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.para.AParaPojo;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.kit.fn.DlzFn2;
import com.dlz.kit.util.system.FieldReflections;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 插入语句生成器
 *
 * @author dk
 */
public class PojoInsert<T> extends AParaPojo<T, TableInsert> implements IExecutorUDI {
    // 插入时字段是否忽略
    private DlzFn2<String, Object,Boolean> ignore = (name, value) -> value==null;
    public PojoInsert(Class<T> valueBean) {
        super(valueBean);
        setPm(new TableInsert(getTableName()));
    }

    @Override
    protected void wrapValues(List<Field> fields, T bean) {
        fields.forEach(field -> {
            Object value = FieldReflections.getValue(bean, field);
            final String columnName = PojoCache.getDbName(field);
            if (ignore.apply(columnName, value)) {
                return;
            }
            getPm().value(columnName, value);
        });
    }

    public PojoInsert<T> value(T value) {
        this.valueBean = value;
        return this;
    }
    public PojoInsert<T> ignore(DlzFn2<String, Object,Boolean> ignore) {
        this.ignore = ignore;
        return this;
    }

    public BatchResult batch(List<T> valueBeans) {
        return batch(valueBeans, 1000);
    }

    public BatchResult batch(List<T> valueBeans, int batchSize) {
        BatchResult.Builder result = BatchResult.builder(valueBeans.size(), batchSize);
        if (valueBeans.isEmpty()) {
            return result.build();
        }
        final Class<T> beanClass = getBeanClass();
        String dbName = PojoCache.getTableName(beanClass);
        final IdInfo idInfo = PojoCache.getIdInfo(beanClass);
        final Field idField = idInfo == null ? null : idInfo.getField();
        final boolean databaseAutoId = idInfo != null && idInfo.getType() == IdType.AUTO;
        final boolean generateId = idInfo != null && idInfo.getType() != IdType.AUTO;

        final List<Field> fields = PojoCache.getBeanFields(beanClass)
                .stream()
                .filter(field -> !databaseAutoId || idField != field)
                .collect(Collectors.toList());
        String sql = WrapperBuildUtil.buildInsertSql(dbName, fields);

        // Pojo 批量插入走原生 JDBC（buildInsertSql(fields) + batch），不经过 TableInsert.buildInsertSql
        final Field logicDeleteField = DbPlugin.getLogicDeleteField(dbName, beanClass);

        for (int start = 0; start < valueBeans.size(); start += batchSize) {
            int end = Math.min(valueBeans.size(), start + batchSize);
            final List<T> items = valueBeans.subList(start, end);
            try {
                if (generateId) {
                    WrapperBuildUtil.fillAutoIds(dbName, idInfo, items);
                }
                List<Object[]> paramValues = items.stream()
                        .map(value -> {
                            if (logicDeleteField != null) {
                                FieldReflections.setValue(value, logicDeleteField, 0);
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
