package com.dlz.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体类描述注解（类级别），用于生成 DDL 表注释。
 * <p>兼容 Swagger {@code @ApiModel}：当字段上未标注 {@code @Schema} 时，
 * 框架会尝试读取 Swagger 的 {@code @ApiModel} 注解。
 *
 * @author dingkui
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Schema {
    /**
     * 表描述
     */
    String value() default "";
}
