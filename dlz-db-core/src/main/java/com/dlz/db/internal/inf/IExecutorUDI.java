package com.dlz.db.internal.inf;

import com.dlz.db.internal.holder.DBHolder;
import com.dlz.db.wrapper.PojoUpdate;
import com.dlz.db.wrapper.TableDelete;

/**
 * 增删改（Update / Delete / Insert）执行器的统一入口。
 * <p>任何具备"写"能力的构造器（{@link PojoUpdate}、
 * {@link TableDelete} 等）都实现本接口。
 */
public interface IExecutorUDI extends ISqlPara {
    /**
     * 执行构造出的 SQL，返回受影响行数。
     * <pre>int rows = DB.pojo.update(user).set(...).where(...).execute();</pre>
     */
    default int execute() {
        return DBHolder.doDb(s -> s.execute(this));
    }
}
