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
 * 删除执行器：在"查询构造器"上叠加"执行删除"能力，并内置 <b>逻辑删除</b> 支持。
 *
 * <p><b>逻辑删除规则</b>：当 Bean/表存在配置中 {@code dlz.db.logic-delete-field} 指定的列（如 {@code is_deleted}），
 * 且当前线程未调用 {@link #setLogicDelete(boolean) setLogicDelete(false)} 时，
 * {@link #execute()} 会将 DELETE 自动改写为 {@code UPDATE ... SET is_deleted = 1 WHERE ...}。
 *
 * <pre>
 * // 默认：逻辑删除
 * DB.Table("user").delete().eq("id", 1).execute();
 *
 * // 本次强制物理删除
 * DB.Table("user").delete().setLogicDelete(false).eq("id", 1).execute();
 * </pre>
 */
public interface IExecuteDelete<T extends IExecuteDelete>
        extends IExecutorUDI, ISqlQuery<T> {
    /** 目标表名。 */
    String getTableName();

    /**
     * 执行删除：有逻辑删除列且未关闭开关时，实际执行 UPDATE 软删除；否则执行物理 DELETE。
     * @return 受影响行数
     */
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

    /**
     * 设置本线程下一次 {@link #execute()} 是否启用逻辑删除。
     * <p>{@code true}（默认）：走逻辑删除（UPDATE 软删除）；<br>
     * {@code false}：强制走物理 DELETE。
     * <p>设置仅作用于<b>本线程的下一次执行</b>，execute 完毕会自动清理。
     */
    default T setLogicDelete(boolean physicallyDelete) {
        SqlRunThreadHolder.setLogicDelete(physicallyDelete);
        return me();
    }
}
