package com.dlz.db.core.anno;

import com.dlz.db.internal.anno.proxy.TableFieldMeta;

import java.lang.annotation.*;
/**
 * 表字段标识
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface TableField {
    /**
     * 数据库字段值
     */
    String value() default "";

    /**
     * 是否为数据库表字段
     * <p>
     * 默认 true 存在，false 不存在
     */
    boolean exist() default true;

    /**
     * 是否进行 select 查询
     * <p>
     * 大字段可设置为 false 不加入 select 查询范围
     */
    boolean select() default true;

    /**
     * 字段中文注释（字段描述）。
     * <p>用于 DDL 生成时的 COMMENT，或映射结果的字段描述。
     * <p>可通过自定义注解扩展本注解（{@code @TableField} 标注到自定义注解上），
     * 从而在不依赖 io.swagger 的情况下提供中文注释，详见 {@link TableFieldMeta}。
     */
    String comment() default "";
}