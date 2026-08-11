package com.dlz.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段描述注解，用于生成 DDL 列注释。
 * <p>兼容 Swagger {@code @ApiModelProperty}：当字段上未标注 {@code @SchemaField} 时，
 * 框架会尝试读取 Swagger 的 {@code @ApiModelProperty} 注解。
 *
 * @author dingkui
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaField {
    /**
     * 字段描述
     */
    String value() default "";
}
