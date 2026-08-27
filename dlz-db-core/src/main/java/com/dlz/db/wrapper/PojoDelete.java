package com.dlz.db.wrapper;

import com.dlz.db.internal.inf.ICondAddByLambda;
import com.dlz.db.internal.inf.IExecutorDelete;
import com.dlz.db.internal.inf.ISqlQuery;
import com.dlz.db.internal.para.APojoQuery;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 删除语句生成器
 *
 * @author dk
 */
public class PojoDelete<T> extends APojoQuery<PojoDelete<T>,T, TableDelete> implements
        ISqlQuery<PojoDelete<T>>,
        ICondAddByLambda<PojoDelete<T>, T>,
        IExecutorDelete<PojoDelete<T>> {
    public PojoDelete(Class<T> beanClass) {
        super(beanClass);
        setPm(new TableDelete(getTableName()));
    }

    @Override
    protected void wrapValues(List<Field> fields, T bean) {

    }

    @Override
    public PojoDelete<T> me() {
        return this;
    }

    public int physical(){
        return this.ignoreLogicDelete(true).execute();
    }
}
