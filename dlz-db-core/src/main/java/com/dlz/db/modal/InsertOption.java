package com.dlz.db.modal;

/**
 * 插入选项。用于 {@link DbPojo#insert(Object, InsertOption...)} 控制插入行为。
 * <p>可变参数，可组合：insert(entity, InsertOption.IGNORE_NULL, InsertOption.ON_DUPLICATE_UPDATE)
 *
 * <p>设计理由：insert 不走链式（它不需要"条件"，只需"选项"），
 * 选项用枚举可变参数比链式更直接，且 5 年后加新选项不破坏签名。
 */
public enum InsertOption {
    /** 忽略 null 字段（默认行为，显式声明用） */
    IGNORE_NULL,
    /** 包含 null 字段（覆盖默认忽略 null） */
    INCLUDE_NULL,
    /** 主键冲突时更新 */
    ON_DUPLICATE_UPDATE,
    /** 主键冲突时忽略 */
    ON_DUPLICATE_IGNORE
}
