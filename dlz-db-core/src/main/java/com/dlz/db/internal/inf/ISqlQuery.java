package com.dlz.db.internal.inf;

import com.dlz.db.internal.condition.Condition;

/**
 * 通用查询/写入构造器的条件入口总和。
 *
 * <p>继承自：
 * <ul>
 *   <li>{@link ICondAndOr}：原生 SQL 片段（sql/apply）及嵌套 and/or 组；</li>
 *   <li>{@link ICondAddByKey}：以<b>字符串列名</b>添加 eq/gt/like/in/... 等条件；</li>
 *   <li>{@link ICondAuto}：按 Map 自动批量生成条件。</li>
 * </ul>
 * <p>Lambda 版本（{@link ICondAddByLambda}）通常由具体 Pojo 构造器按需额外混入，不在本接口默认包含。
 *
 * @param <T> 链式返回类型
 */
public interface ISqlQuery<T extends ISqlQuery> extends
        ICondAndOr<T>,
        ICondAddByKey<T>,
        ICondAuto<T> {
    /** 当前构造器持有的 WHERE 根条件节点。 */
    Condition where();

    /** 向 {@link #where()} 根节点追加子条件（由 {@link ICondBase} 约定，业务代码不直接调用）。 */
    default void addChildren(Condition child) {
        where().addChildren(child);
    }

    /** 用给定条件整体替换 WHERE。 */
    T where(Condition cond);

    /**
     * 允许/禁止"最终 WHERE 为空的全表查询或更新"。
     * <p>为 false 且所有用户条件及 Option 条件均为空时，构建器会生成
     * {@code WHERE false}，不会抛异常。逻辑删除等 Option 注入的条件也会使
     * WHERE 非空，因此该开关不能代替业务侧的 UPDATE/DELETE 条件校验。
     * 设为 true 表示明确允许空 WHERE。
     */
    T setAllowFullQuery(boolean allowFullQuery);

    /** 目标表名。 */
    String getTableName();

    /** 是否允许在无 WHERE 条件时执行。 */
    boolean isAllowFullQuery();
}
