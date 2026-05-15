package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.support.DBHolder;

import java.util.List;

public class DbBatch {
    public <T> boolean insert(List<T> bean) {
        return insert(bean, 1000);
    }

    public <T> boolean insert(List<T> bean, int batchSize) {
        if (!bean.isEmpty()) {
            return PojoInsert.wrapper(bean.get(0)).batch(bean, batchSize);
        }
        return false;
    }

    public <T> boolean update(List<T> bean) {
        return update(bean, 1000);
    }

    public <T> boolean update(List<T> bean, int batchSize) {
        if (!bean.isEmpty()) {
            final Class<T> aClass = (Class<T>)bean.get(0).getClass();
            return PojoUpdate.wrapper(aClass).batch(bean, batchSize);
        }
        return false;
    }

    public boolean update(String sql, List<Object[]> valueBeans) {
        return update(sql, valueBeans, 1000);
    }

    public boolean update(String sql, List<Object[]> valueBeans, int batchSize) {
        for (; !valueBeans.isEmpty() && batchSize > 0; valueBeans = valueBeans.subList(batchSize, valueBeans.size())) {
            if (batchSize > valueBeans.size()) {
                batchSize = valueBeans.size();
            }
            List<Object[]> paramValues = valueBeans.subList(0, batchSize);
            DBHolder.getSqlExecutor().batch(sql, paramValues);
        }
        return true;
    }
}