package com.dlz.db.interceptor;

import com.dlz.db.option.DbOption;
import com.dlz.db.option.DbOperation;
import com.dlz.db.option.LogicDeleteOption;

import java.util.Collections;
import java.util.List;

/**
 * 逻辑删除拦截器：为所有操作供给 {@link LogicDeleteOption}。
 *
 * <p>本身不包含任何 SQL 改写逻辑——WHERE 追加、INSERT 默认值、DELETE 改写
 * 全部由 LogicDeleteOption 实现的桩点（WherePoint / InsertFieldPoint /
 * LogicDeleteValuePoint）统一完成。
 *
 * <h3>开关控制</h3>
 * <ul>
 *   <li>全局开关：{@code DlzDbProperties.logicDeleteField} 为空则不注册</li>
 *   <li>线程级开关：{@code ignoreLogicDelete(true)} 单次强制物理删除</li>
 * </ul>
 *
 * <pre>
 * // 默认（DB.config.logicDeleteField 自动注册）：逻辑删除
 * DB.table.deleteWrapper("user").eq("id", 1).execute();
 *
 * // 本次强制物理删除
 * DB.table.deleteWrapper("user").ignoreLogicDelete(true).eq("id", 1).execute();
 * </pre>
 *
 * @author dingkui
 * @since 8.0.0
 */
public class LogicDeleteInterceptor implements SqlBuildInterceptor {

    private final LogicDeleteOption option;

    public LogicDeleteInterceptor(String fieldName) {
        this.option = new LogicDeleteOption(fieldName);
    }

    public LogicDeleteOption getOption() {
        return option;
    }

    @Override
    public boolean isEnabled() {
        return option.isEnabled();
    }

    @Override
    public List<DbOption> supplyOptions(DbOperation operation, String tableName) {
        return Collections.singletonList(option);
    }
}
