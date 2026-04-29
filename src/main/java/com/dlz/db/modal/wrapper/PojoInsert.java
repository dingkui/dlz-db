package com.dlz.db.modal.wrapper;

import com.dlz.db.annotation.IdType;
import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.db.holder.DBHolder;
import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.modal.para.AParaPojo;
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
    public static <T> PojoInsert<T> wrapper(T valueBean) {
        return new PojoInsert(valueBean);
    }

    private PojoInsert(T valueBean) {
        super(valueBean);
        setPm(new TableInsert(getTableName()));
    }

    @Override
    protected void wrapValues(List<Field> fields, T bean) {
        fields.forEach(field -> {
            Object value = FieldReflections.getValue(bean, field);
            if (value != null) {
                getPm().value(BeanInfoHolder.getColumnName(field), value);
            }
        });
    }

    public boolean batch(List<T> valueBeans) {
        return batch(valueBeans, 1000);
    }

    public boolean batch(List<T> valueBeans, int batchSize) {
        if (valueBeans.size() == 0) {
            return true;
        }
        final Class<T> beanClass = getBeanClass();
        String dbName = BeanInfoHolder.getTableName(beanClass);
        final Field idField = BeanInfoHolder.getIdField(beanClass);
        final IdType idType = idField != null ? WrapperBuildUtil.getIdType(idField) : null;

        final List<Field> fields = BeanInfoHolder.getBeanFields(beanClass)
                .stream()
                .filter(field -> idType != IdType.AUTO || idField != field)
                .collect(Collectors.toList());
        String sql = WrapperBuildUtil.buildInsertSql(dbName, fields);
        while (valueBeans.size() > 0 && batchSize > 0) {
            if (batchSize > valueBeans.size()) {
                batchSize = valueBeans.size();
            }
            final List<T> ts = valueBeans.subList(0, batchSize);
            List<Object[]> paramValues = ts.stream()
                    .map(v -> {
                        WrapperBuildUtil.fillAutoId(dbName, idField, idType, v);
                        return WrapperBuildUtil.buildInsertParams(v, fields);
                    })
                    .collect(Collectors.toList());
            DBHolder.getDao().batchUpdate(sql, paramValues);
            valueBeans = valueBeans.subList(batchSize, valueBeans.size());
        }
        return true;
    }
}
