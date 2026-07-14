package com.dlz.db.modal.para;

import com.dlz.db.inf.ICondAddByFn;
import com.dlz.db.inf.ISqlQuery;
import com.dlz.db.modal.condition.Condition;
import com.dlz.db.support.PojoCache;


/**
 * 构造单表的更新操作sql
 *
 * @author dingkui
 */
public abstract class AQuery<T extends AQuery> extends AParaTable<T> implements
        ISqlQuery<T>,
        ICondAddByFn<T> {
    private Condition whereCond = Condition.where(getTableName());
    private boolean allowFullQuery = false;//是否允许全表查询，默认不允许
    String idDbName;
    protected AQuery(String tableName) {
        super(tableName);
        idDbName = PojoCache.getIdDbName(tableName);
    }

    public Condition where() {
        return whereCond;
    }
    public T where(Condition cond) {
        this.whereCond = cond.clone();
        return me();
    }

    public T setAllowFullQuery(boolean allowFullQuery) {
        this.allowFullQuery = allowFullQuery;
        return me();
    }
    public boolean isAllowFullQuery() {
        return this.allowFullQuery;
    }
}
