package com.dlz.db.modal.wrapper;

import com.dlz.db.inf.ICondAddByLamda;
import com.dlz.db.inf.IExecutorDelete;
import com.dlz.db.inf.ISqlQuery;
import com.dlz.db.modal.para.APojoQuery;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 删除语句生成器
 *
 * @author dk
 */
public class PojoDelete<T> extends APojoQuery<PojoDelete<T>,T, TableDelete> implements
        ISqlQuery<PojoDelete<T>>,
        ICondAddByLamda<PojoDelete<T>, T>,
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
