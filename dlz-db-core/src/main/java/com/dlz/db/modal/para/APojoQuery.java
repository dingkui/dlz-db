package com.dlz.db.modal.para;

import com.dlz.db.inf.ICondAddByLamda;
import com.dlz.db.inf.ISqlQuery;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.support.PojoCache;
import com.dlz.kit.fn.DlzFn2;
import com.dlz.kit.util.system.FieldReflections;

import java.lang.reflect.Field;
import java.util.List;


/**
 * 构造单表的更新操作sql
 *
 * @author dingkui
 */
public abstract class APojoQuery<ME extends APojoQuery, T, PM extends AQuery>
        extends AParaPojo<T, PM>
        implements ISqlQuery<ME>, ICondAddByLamda<ME, T> {
    private DlzFn2<String, Object,Boolean> queryIgnore = (name, value) -> value==null;
    protected APojoQuery(Class<T> beanClass) {
        super(beanClass);
    }

    @Override
    protected void wrapQuery(List<Field> fields, T bean) {
        fields.forEach(field -> {
            Object value = FieldReflections.getValue(bean, field);
            final String columnName = PojoCache.getColumnName(field);
            if (this.queryIgnore.apply(columnName, value)) {
                return;
            }
            if(value == null){
                getPm().isNull(columnName);
            }else{
                getPm().eq(columnName, value);
            }
        });
    }

    public ME queryIgnore(DlzFn2<String, Object,Boolean> queryIgnore) {
        this.queryIgnore = queryIgnore;
        return me();
    }
    public Condition where() {
        return getPm().where();
    }
    public ME where(Condition cond) {
        getPm().where(cond);
        return me();
    }
    public ME where(T bean) {
        this.valueBean = bean;
        return me();
    }

    public ME setAllowFullQuery(boolean allowFullQuery) {
        getPm().setAllowFullQuery(allowFullQuery);
        return me();
    }
    public boolean isAllowFullQuery() {
        return getPm().isAllowFullQuery();
    }

}
