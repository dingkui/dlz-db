package com.dlz.db.inf;

import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.db.holder.DBHolder;
import com.dlz.db.holder.SqlHolder;
import com.dlz.db.holder.SqlRunThreadHolder;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.modal.wrapper.TableDelete;
import com.dlz.db.modal.wrapper.TableUpdate;

/**
 * 删除执行器=执行器+查询构造器
 */
public interface IExecuteDelete<T extends IExecuteDelete>
        extends IExecutorUDI, ISqlQuery<T> {
    String getTableName();
    default int execute() {
        final String logicDeleteField = SqlHolder.properties.getLogicDeleteField();
        if(!SqlRunThreadHolder.isLogicDelete() || !BeanInfoHolder.isColumnExists(getTableName(), logicDeleteField)){
            try {
                return DBHolder.doDb(s -> s.execute(this));
            }finally {
                SqlRunThreadHolder.removeLogicDeleteSetting();
            }
        }
        final TableUpdate update = new TableUpdate(getTableName())
                .set(logicDeleteField, 1)
                .where(this.where());
        if(this instanceof TableDelete){
            update.getPara().putAll(((TableDelete) this).getPara());
        }else if(this instanceof PojoUpdate){
            final ParaMap pm = ((PojoUpdate) this).getPm();
            update.getPara().putAll(pm.getPara());
        }
        return DBHolder.doDb(s -> s.execute(update));
    }

    default T setLogicDelete(boolean physicallyDelete) {
        SqlRunThreadHolder.setLogicDelete(physicallyDelete);
        return me();
    }
}
