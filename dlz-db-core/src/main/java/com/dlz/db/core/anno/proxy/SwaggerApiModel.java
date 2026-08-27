package com.dlz.db.core.anno.proxy;

import java.lang.annotation.Annotation;

/**
 * Swagger {@code @ApiModel} 代理。
 * <p>通过反射读取 Swagger 注解，未引入 Swagger 时不报错，返回 null。
 *
 * @author dingkui
 */
public class SwaggerApiModel {
    private final Class<Annotation> annotationType;
    private final java.lang.reflect.Method valueMethod;

    public SwaggerApiModel() {
        Class<Annotation> annType;
        java.lang.reflect.Method valueMethodTmp;
        try {
            annType = (Class<Annotation>) Class.forName("io.swagger.annotations.ApiModel");
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
     * 读取类上的 Swagger @ApiModel value 属性。
     *
     * @param clazz 实体类
     * @return 描述文本；未标注或 Swagger 未引入时返回 null
     */
    public String value(Class<?> clazz) {
        if (clazz == null || annotationType == null) {
            return null;
        }
        if (clazz.isAnnotationPresent(annotationType)) {
            Annotation ann = clazz.getAnnotation(annotationType);
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
