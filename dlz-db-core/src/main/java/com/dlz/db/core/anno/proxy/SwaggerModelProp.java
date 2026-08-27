package com.dlz.db.core.anno.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Swagger {@code @ApiModelProperty} 代理。
 * <p>通过反射读取 Swagger 注解，未引入 Swagger 时不报错，返回 null。
 *
 * @author dingkui
 */
public class SwaggerModelProp {
    private final Class<Annotation> annotationType;
    private final java.lang.reflect.Method valueMethod;

    public SwaggerModelProp() {
        Class<Annotation> annType;
        java.lang.reflect.Method valueMethodTmp;
        try {
            annType = (Class<Annotation>) Class.forName("io.swagger.annotations.ApiModelProperty");
            valueMethodTmp = annType.getMethod("value");
        } catch (ClassNotFoundException ex) {
            annType = null;
            valueMethodTmp = null;
        } catch (NoSuchMethodException ex) {
            annType = null;
            valueMethodTmp = null;
        }
        this.annotationType = annType;
        this.valueMethod = valueMethodTmp;
    }

    /**
     * 读取字段上的 Swagger @ApiModelProperty value 属性。
     *
     * @param field 字段
     * @return 描述文本；未标注或 Swagger 未引入时返回 null
     */
    public String value(Field field) {
        if (field == null || annotationType == null) {
            return null;
        }
        if (field.isAnnotationPresent(annotationType)) {
            Annotation ann = field.getAnnotation(annotationType);
            try {
                String value = (String) valueMethod.invoke(ann);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                return null;
            }
        }
        return null;
    }
}
