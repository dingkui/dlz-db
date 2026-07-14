package com.dlz.db.modal.options;

import java.io.Serializable;

/** 单次数据库操作选项。自定义插件 Option 也应实现该接口。 */
public interface DbOption extends Serializable {
    /** 唯一或冲突分组键；同一次操作中同 key 只能出现一个 Option。 */
    default String key() {
        return getClass().getName();
    }

    /** 当前 Option 是否适用于指定操作。 */
    default boolean supports(DbOperation operation) {
        return true;
    }
}
