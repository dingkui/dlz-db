package com.dlz.db.annotation.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MybatisPlusTableName {
    private final Class<Annotation> idTypeAnnotation;
    private final Method valueMethod;
    protected MybatisPlusTableName() {
        Class<Annotation> idType1;
        Method valueMethodTmp;
        try {
            idType1 = (Class<Annotation>) Class.forName("com.baomidou.mybatisplus.annotation.TableName");
            valueMethodTmp = idType1.getMethod("value");
        } catch (Exception ex) {
            idType1 = null;
            valueMethodTmp = null;
        }
        idTypeAnnotation = idType1;
        valueMethod = valueMethodTmp;
    }

    public String value(Class<?> field){
        return getValueFromAnno(field == null, idTypeAnnotation, field.isAnnotationPresent(idTypeAnnotation), field.getAnnotation(idTypeAnnotation), valueMethod);
    }

    protected static String getValueFromAnno(boolean b, Class<Annotation> idTypeAnnotation, boolean annotationPresent, Annotation annotation, Method valueMethod) {
        if (b || idTypeAnnotation ==null){
            return null;
        }
        if (annotationPresent) {
            Annotation mpAnnotation = annotation;
            try {
                String value = (String) valueMethod.invoke(mpAnnotation);
                if (!value.isEmpty()) {
                    return value;
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
