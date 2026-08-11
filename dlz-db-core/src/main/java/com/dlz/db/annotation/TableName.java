package com.dlz.db.annotation;

import java.lang.annotation.*;

/**
 * 数据库表相关
 *
 * @author hubin, hanchunlin
 * @since 2016-01-23
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface TableName {

    /**
     * 实体对应的表名
     */
    String value() default "";

    /**
     * 表中文注释（表描述）。
     * <p>用于 DDL 生成时的 COMMENT。
     * <p>可通过自定义注解扩展本注解（{@code @TableName} 标注到自定义注解上），
     * 从而在不依赖 io.swagger 的情况下提供中文注释。
     */
    String comment() default "";
}