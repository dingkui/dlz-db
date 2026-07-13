package com.dlz.db.modal.wrapper;

import com.dlz.db.annotation.IdType;
import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.interceptor.DbPlugin;
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

    @Override
    protected void wrapQuery(List<Field> fields, T bean) {

    }

    public PojoInsert<T> value(T bean) {
        this.valueBean = bean;
        return this;
    }
    public PojoInsert<T> ignore(DlzFn2<String, Object,Boolean> ignore) {
        this.ignore = ignore;
        return this;
    }

    public boolean batch(List<T> valueBeans) {
        return batch(valueBeans, 1000);
    }

    public boolean batch(List<T> valueBeans, int batchSize) {
        if (valueBeans.isEmpty()) {
            return false;
        }
        final Class<T> beanClass = getBeanClass();
        String dbName = PojoCache.getTableName(beanClass);
        final IdInfo idInfo = PojoCache.getIdInfo(beanClass);
        boolean doAutoId = idInfo != null || idInfo.getType() != IdType.AUTO;

        final List<Field> fields = PojoCache.getBeanFields(beanClass)
                .stream()
                .filter(field -> !doAutoId || idInfo.getField() != field)
                .collect(Collectors.toList());
        String sql = WrapperBuildUtil.buildInsertSql(dbName, fields);

        // Pojo 批量插入走原生 JDBC（buildInsertSql(fields) + batch），不经过 TableInsert.buildInsertSql
        final Field logicDeleteField = DbPlugin.getLogicDeleteField(dbName, beanClass);

        while (!valueBeans.isEmpty() && batchSize > 0) {
            if (batchSize > valueBeans.size()) {
                batchSize = valueBeans.size();
            }
            final List<T> ts = valueBeans.subList(0, batchSize);
            if(doAutoId){
                WrapperBuildUtil.fillAutoIds(dbName, idInfo, ts);
            }
            List<Object[]> paramValues = ts.stream()
                    .map(v -> {
                        if(logicDeleteField!=null){
                            FieldReflections.setValue(v, logicDeleteField, 0);
                        }
                        return WrapperBuildUtil.buildInsertParams(v, fields);
                    })
                    .collect(Collectors.toList());
            DBHolder.getSqlExecutor().batch(sql, paramValues);
            valueBeans = valueBeans.subList(batchSize, valueBeans.size());
        }
        return true;
    }
}
