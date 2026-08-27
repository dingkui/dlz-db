package com.dlz.db.internal.inf;

import com.dlz.db.interceptor.DbPlugin;
import com.dlz.db.internal.holder.DBHolder;
import com.dlz.db.internal.holder.SqlRunThreadHolder;
import com.dlz.db.internal.para.ParaMap;
import com.dlz.db.option.DbOperation;
import com.dlz.db.option.DbOptionAware;
import com.dlz.db.option.DbOptions;
import com.dlz.db.option.LogicDeleteOption;
import com.dlz.db.option.point.DeleteModePoint;
import com.dlz.db.option.point.LogicDeleteValuePoint;
import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.option.point.context.DeleteMode;
import com.dlz.db.option.point.context.ValueContext;
import com.dlz.db.wrapper.PojoUpdate;
import com.dlz.db.wrapper.TableDelete;
import com.dlz.db.wrapper.TableUpdate;

/**
 * 删除执行器：在"查询构造器"上叠加"执行删除"能力。
 *
 * <p><b>逻辑删除</b>由 Option 桩点体系驱动：拦截器供给的
 * {@link LogicDeleteOption} 实现 {@link LogicDeleteValuePoint}，
 * {@link DeleteModePoint} 决定逻辑/物理模式，本接口只负责 DELETE → UPDATE 改写执行。
 *
 * <pre>
 * // 默认：注册了逻辑删除且表有 deleted 字段 → 逻辑删除
 * DB.table.deleteWrapper("user").eq("id", 1).execute();
 *
 * // 本次强制物理删除
 * DB.table.deleteWrapper("user").ignoreLogicDelete(true).eq("id", 1).execute();
 * </pre>
 */
public interface IExecutorDelete<ME extends IExecutorDelete>
        extends IExecutorUDI, ISqlQuery<ME> {
    /** 目标表名。 */
    String getTableName();

    /**
     * 执行删除。
     * <p>先经桩点判断：逻辑删除生效则改写为 UPDATE（标记值由
     * LogicDeleteValuePoint 提供），否则执行物理 DELETE。
     *
     * @return 受影响行数
     */
    default int execute() {
        try {
            final DbOptions effective = DbPlugin.effectiveOptions(DbOperation.DELETE, getTableName(), userOptions(this));
            final LogicDeleteOption logicOption = effective.get(LogicDeleteOption.class);
            if (logicOption != null
                    && logicOption.appliesTo(getTableName())
                    && !isPhysicalMode(getTableName(), effective)) {
                return rewriteAsLogicDelete(this, effective, logicOption);
            }
            // 无逻辑删除桩点生效，走物理 DELETE
            return DBHolder.doDb(s -> s.execute(this));
        } finally {
            SqlRunThreadHolder.removeLogicDeleteSetting();
        }
    }

    /** 本执行器携带的调用点 Option（未显式设置时为 EMPTY）。 */
    static DbOptions userOptions(IExecutorDelete<?> executor) {
        return executor instanceof DbOptionAware
                ? ((DbOptionAware) executor).getDbOptions()
                : DbOptions.EMPTY;
    }

    /** 调用点是否显式选择了物理删除。 */
    static boolean isPhysicalMode(String tableName, DbOptions effective) {
        DeleteModePoint modePoint = effective.getPointBindings().single(DeleteModePoint.class);
        return modePoint != null && modePoint.chooseDeleteMode(
                new CrudContext(DbOperation.DELETE, tableName, null, effective)) == DeleteMode.PHYSICAL;
    }

    /** 逻辑删除改写：DELETE → UPDATE 逻辑删除标记=1。 */
    static int rewriteAsLogicDelete(IExecutorDelete<?> executor, DbOptions effective, LogicDeleteOption logicOption) {
        final String tableName = executor.getTableName();
        final Object markerValue = logicOption.provideLogicDeleteValue(new ValueContext(
                DbOperation.DELETE, tableName, logicOption.getFieldName(), logicOption.getDbColumnName(),
                null, null));
        final TableUpdate update = new TableUpdate(tableName)
                .options(effective)
                .set(logicOption.getDbColumnName(), markerValue)
                .where(executor.where());
        if (executor instanceof TableDelete) {
            update.getPara().putAll(((TableDelete) executor).getPara());
        } else if (executor instanceof PojoUpdate) {
            final ParaMap pm = ((PojoUpdate) executor).getPm();
            update.getPara().putAll(pm.getPara());
        }
        return DBHolder.doDb(s -> s.execute(update));
    }

    /**
     * 设置本线程下一次 {@link #execute()} 是否启用逻辑删除。
     * <p>{@code true}：强制走物理 DELETE；{@code false}（默认）：走逻辑删除（UPDATE 软删除）。
     * <p>设置仅作用于<b>本线程的下一次执行</b>，execute 完毕会自动清理。
     */
    default ME ignoreLogicDelete(boolean ignoreLogicDelete) {
        SqlRunThreadHolder.setIgnoreLogicDelete(ignoreLogicDelete);
        return me();
    }
}
