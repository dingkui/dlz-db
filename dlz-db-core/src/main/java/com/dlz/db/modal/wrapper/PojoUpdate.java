package com.dlz.db.modal.wrapper;

import com.dlz.db.inf.ICondAddByLamda;
import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.inf.ISqlQuery;
import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.para.APojoQuery;
import com.dlz.db.support.DBHolder;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.IdInfo;
import com.dlz.kit.exception.ValidateException;
import com.dlz.kit.fn.DlzFn;
import com.dlz.kit.fn.DlzFn2;
import com.dlz.kit.util.system.FieldReflections;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 更新语句生成器
 *
 * @author dk
 *
 */
public class PojoUpdate<T> extends APojoQuery<PojoUpdate<T>, T, TableUpdate> implements
        ISqlQuery<PojoUpdate<T>>,
        ICondAddByLamda<PojoUpdate<T>, T>,
        IExecutorUDI {
    IdInfo idInfo;
    private DlzFn2<String, Object,Boolean> ignore = (name, value) -> value==null || (idInfo!=null && name.equals(idInfo.getDbName())) ;
    public PojoUpdate(Class<T> beanClass) {
        super(beanClass);
        idInfo = PojoCache.getIdInfo(beanClass);
        setPm(new TableUpdate(getTableName()));
    }

    @Override
    protected void wrapValues(List<Field> fields, T bean) {
        for (Field field : fields) {
            final Object fieldValue = FieldReflections.getValue(bean, field);
            final String columnName = PojoCache.getDbName(field);
            if (this.ignore.apply(columnName, fieldValue)) {
                continue;
            }
            getPm().set(columnName, fieldValue);
        }
    }


    public PojoUpdate<T> set(DlzFn<T, ?> column, Object value) {
        getPm().set(column, value);
        return this;
    }
    public PojoUpdate<T> set(String column, Object value) {
        getPm().set(column, value);
        return this;
    }

    public PojoUpdate<T> set(T bean) {
        this.valueBean = bean;
        return this;
    }

    /**
     * 以原生 SQL 片段更新字段，形如 "col = expr"。
     * 内部等价于 {@code set(col, "sql:" + expr)}，复用 sql: 前缀分支。
     * <p>⚠️ expr 为原生 SQL，严禁拼接外部输入。
     * <pre>
     *   .setSql("score = score + 10")
     *   .setSql("view_count = view_count + 1")
     * </pre>
     */
    public PojoUpdate<T> setSql(String sqlFragment) {
        if (sqlFragment == null || sqlFragment.trim().isEmpty()) {
            return this;
        }
        int eq = sqlFragment.indexOf('=');
        if (eq <= 0) {
            throw new ValidateException("setSql 需要 'col = expr' 形式: " + sqlFragment);
        }
        String col = sqlFragment.substring(0, eq).trim();
        String expr = sqlFragment.substring(eq + 1).trim();
        getPm().set(col, "sql:" + expr);
        return this;
    }

    public PojoUpdate<T> ignore(DlzFn2<String, Object,Boolean> ignore) {
        this.ignore = ignore;
        return this;
    }



    @Override
    public PojoUpdate<T> me() {
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
        final List<Field> fields = PojoCache.getBeanFields(beanClass);
        String sql = WrapperBuildUtil.buildUpdateSql(dbName, fields, idInfo.getDbName());
        for (int start = 0; start < valueBeans.size(); start += batchSize) {
            int end = Math.min(valueBeans.size(), start + batchSize);
            final List<T> items = valueBeans.subList(start, end);
            try {
                List<Object[]> paramValues = items.stream()
                        .map(value -> WrapperBuildUtil.buildUpdateParams(value, fields, idInfo.getField()))
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
