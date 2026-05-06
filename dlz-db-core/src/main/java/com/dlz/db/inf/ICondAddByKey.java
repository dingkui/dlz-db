package com.dlz.db.inf;

import com.dlz.db.enums.DbOprateEnum;

import static com.dlz.db.enums.DbOprateEnum.*;

/**
 * 基于 <b>字符串列名</b> 的条件构造接口。
 *
 * <p><b>设计约定</b>：所有条件方法统一签名 <b>{@code (boolean is, String column, Object value)}</b>；
 * 省略 {@code is} 的重载等价于 {@code is=true}；{@code is=false} 时整条条件跳过，适合动态条件。
 *
 * <p><b>与 {@link ICondAddByLamda} 的差异</b>：
 * <ul>
 *   <li>列名为手写字符串（如 {@code "user_name"}），不做类型安全校验；</li>
 *   <li>适用于列名在运行时决定、或 Bean 中无对应属性、或拼接同表多列的场景；</li>
 *   <li>列名大小写与数据库实际列名一致（通常为 snake_case 小写）。</li>
 * </ul>
 *
 * <pre>
 * // 动态列名场景
 * String sortCol = userInput.getSortField();  // e.g. "create_time"
 * .eq("status", 1).gt(sortCol, someValue)
 *
 * // 同 {@link ICondAddByLamda} 等价
 * .eq("user_id", 42)    // 等价于 .eq(User::getUserId, 42)
 * </pre>
 *
 * <p><b>命名速查</b>：
 * <pre>
 *   eq / ne                     =  / <>
 *   gt / ge / lt / le           > / >= / < / <=
 *   lk / ll / lr / nl           LIKE '%v%' / 'v%' / '%v' / NOT LIKE
 *   in / ni                     IN / NOT IN
 *   bt / nb                     BETWEEN / NOT BETWEEN
 *   isn / isnn                  IS NULL / IS NOT NULL
 *   op                          自定义操作符（{@link DbOprateEnum}）
 * </pre>
 */
public interface ICondAddByKey<T extends ICondAddByKey> extends ICondBase<T> {

    // ========== BETWEEN / NOT BETWEEN ==========

    /**
     * {@code column BETWEEN value1 AND value2}。
     * <pre>.bt("age", 18, 60)   // age BETWEEN 18 AND 60</pre>
     */
    default T bt(String column, Object value1, Object value2) {
        addChildren(bt.mk(column, new Object[]{value1, value2}));
        return me();
    }

    /** 动态条件版 {@link #bt(String, Object, Object)}。 */
    default T bt(boolean is, String column, Object value1, Object value2) {
        if (is) {
            addChildren(bt.mk(column, new Object[]{value1, value2}));
        }
        return me();
    }

    /**
     * 单值形式的 BETWEEN。{@code value} 支持多种形式：
     * <ul>
     *   <li>字符串："a1,a2"（逗号分隔）</li>
     *   <li>JSON：[a1,a2]</li>
     *   <li>数组：{@code new Object[]{a1, a2}}</li>
     *   <li>{@link java.util.List}：{@code Arrays.asList(a1, a2)}</li>
     * </ul>
     * <pre>
     * .bt("create_time", "2024-01-01,2024-12-31")
     * .bt("age", Arrays.asList(18, 60))
     * </pre>
     */
    default T bt(String column, Object value) {
        addChildren(bt.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #bt(String, Object)}。 */
    default T bt(boolean is, String column, Object value) {
        if (is) {
            addChildren(bt.mk(column, value));
        }
        return me();
    }

    /** {@code column NOT BETWEEN value1 AND value2}。 */
    default T nb(String column, Object value1, Object value2) {
        addChildren(nb.mk(column, new Object[]{value1, value2}));
        return me();
    }

    /** 动态条件版 {@link #nb(String, Object, Object)}。 */
    default T nb(boolean is, String column, Object value1, Object value2) {
        if (is) {
            addChildren(nb.mk(column, new Object[]{value1, value2}));
        }
        return me();
    }

    /** 单值形式的 NOT BETWEEN，{@code value} 格式同 {@link #bt(String, Object)}。 */
    default T nb(String column, Object value) {
        addChildren(nb.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #nb(String, Object)}。 */
    default T nb(boolean is, String column, Object value) {
        if (is) {
            addChildren(nb.mk(column, value));
        }
        return me();
    }

    // ========== IS NULL / IS NOT NULL ==========

    /**
     * {@code column IS NOT NULL}。
     * <pre>.isnn("email")   // email IS NOT NULL</pre>
     */
    default T isnn(String column) {
        addChildren(isnn.mk(column, null));
        return me();
    }

    /** 动态条件版 {@link #isnn(String)}。 */
    default T isnn(boolean is, String column) {
        if (is) {
            addChildren(isnn.mk(column, null));
        }
        return me();
    }

    /**
     * {@code column IS NULL}。
     * <pre>.isn("delete_time")   // delete_time IS NULL</pre>
     */
    default T isn(String column) {
        addChildren(isn.mk(column, null));
        return me();
    }

    /** 动态条件版 {@link #isn(String)}。 */
    default T isn(boolean is, String column) {
        if (is) {
            addChildren(isn.mk(column, null));
        }
        return me();
    }

    // ========== EQ / NE ==========

    /**
     * {@code column = value}。
     * <pre>.eq("status", 1)   // status = 1</pre>
     */
    default T eq(String column, Object value) {
        addChildren(eq.mk(column, value));
        return me();
    }

    /**
     * 动态条件版 {@link #eq(String, Object)}。动态条件首选写法。
     * <pre>.eq(name != null, "name", name)</pre>
     */
    default T eq(boolean is, String column, Object value) {
        if (is) {
            addChildren(eq.mk(column, value));
        }
        return me();
    }

    /** {@code column <> value}。 */
    default T ne(String column, Object value) {
        addChildren(ne.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #ne(String, Object)}。 */
    default T ne(boolean is, String column, Object value) {
        if (is) {
            addChildren(ne.mk(column, value));
        }
        return me();
    }

    // ========== 大小比较 ==========

    /** {@code column > value}。 */
    default T gt(String column, Object value) {
        addChildren(gt.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #gt(String, Object)}。 */
    default T gt(boolean is, String column, Object value) {
        if (is) {
            addChildren(gt.mk(column, value));
        }
        return me();
    }

    /** {@code column >= value}。 */
    default T ge(String column, Object value) {
        addChildren(ge.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #ge(String, Object)}。 */
    default T ge(boolean is, String column, Object value) {
        if (is) {
            addChildren(ge.mk(column, value));
        }
        return me();
    }

    /** {@code column < value}。 */
    default T lt(String column, Object value) {
        addChildren(lt.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #lt(String, Object)}。 */
    default T lt(boolean is, String column, Object value) {
        if (is) {
            addChildren(lt.mk(column, value));
        }
        return me();
    }

    /** {@code column <= value}。 */
    default T le(String column, Object value) {
        addChildren(le.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #le(String, Object)}。 */
    default T le(boolean is, String column, Object value) {
        if (is) {
            addChildren(le.mk(column, value));
        }
        return me();
    }

    // ========== LIKE 系列 ==========

    /**
     * {@code column LIKE '%value%'}（双侧模糊）。
     * <pre>.lk("name", "张")   // name LIKE '%张%'</pre>
     */
    default T lk(String column, Object value) {
        addChildren(lk.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #lk(String, Object)}。 */
    default T lk(boolean is, String column, Object value) {
        if (is) {
            addChildren(lk.mk(column, value));
        }
        return me();
    }

    /**
     * {@code column LIKE 'value%'}（右模糊，通常可命中前缀索引）。
     * <pre>.ll("name", "张")   // name LIKE '张%'</pre>
     */
    default T ll(String column, Object value) {
        addChildren(ll.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #ll(String, Object)}。 */
    default T ll(boolean is, String column, Object value) {
        if (is) {
            addChildren(ll.mk(column, value));
        }
        return me();
    }

    /**
     * {@code column LIKE '%value'}（左模糊，通常无法命中索引）。
     * <pre>.lr("email", "@qq.com")</pre>
     */
    default T lr(String column, Object value) {
        addChildren(lr.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #lr(String, Object)}。 */
    default T lr(boolean is, String column, Object value) {
        if (is) {
            addChildren(lr.mk(column, value));
        }
        return me();
    }

    /** {@code column NOT LIKE '%value%'}。 */
    default T nl(String column, Object value) {
        addChildren(nl.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #nl(String, Object)}。 */
    default T nl(boolean is, String column, Object value) {
        if (is) {
            addChildren(nl.mk(column, value));
        }
        return me();
    }

    // ========== IN / NOT IN ==========

    /**
     * {@code column IN (...)}。{@code value} 可为：
     * <ul>
     *   <li>数组 / {@link java.util.Collection}</li>
     *   <li>逗号分隔字符串 {@code "1,2,3"}</li>
     *   <li>以 {@code "sql:"} 开头的子查询片段 {@code "sql:SELECT id FROM ..."}</li>
     * </ul>
     * <b>不可传单值</b>（传单值不会按 {@code col = value} 处理，行为未定义）。
     * <pre>
     * .in("id", "1,2,3")
     * .in("id", Arrays.asList(1, 2, 3))
     * .in("id", "sql:SELECT user_id FROM vip WHERE level > 5")
     * </pre>
     */
    default T in(String column, Object value) {
        addChildren(in.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #in(String, Object)}。 */
    default T in(boolean is, String column, Object value) {
        if (is) {
            addChildren(in.mk(column, value));
        }
        return me();
    }

    /** {@code column NOT IN (...)}，{@code value} 格式同 {@link #in(String, Object)}。 */
    default T ni(String column, Object value) {
        addChildren(ni.mk(column, value));
        return me();
    }

    /** 动态条件版 {@link #ni(String, Object)}。 */
    default T ni(boolean is, String column, Object value) {
        if (is) {
            addChildren(ni.mk(column, value));
        }
        return me();
    }

    // ========== 自定义操作符 ==========

    /**
     * 以自定义操作符添加条件。适用于上述枚举未覆盖的场景。
     * <pre>.op("name", DbOprateEnum.eq, "admin")</pre>
     *
     * @param op 操作符枚举；实际调用 {@code op.mk(column, value)}，语义由枚举决定。
     */
    default T op(String column, DbOprateEnum op, Object value) {
        addChildren(op.mk(column, value));
        return me();
    }
}
