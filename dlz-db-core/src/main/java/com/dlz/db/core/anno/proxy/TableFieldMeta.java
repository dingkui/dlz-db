package com.dlz.db.core.anno.proxy;

import com.dlz.db.core.anno.TableField;
import com.dlz.db.core.anno.TableName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link TableField} / {@link TableName} 元注解读取器。
 * <p>既支持字段/类上直接标注 {@code @TableField}/{@code @TableName}，
 * 也支持用户自定义注解通过将 {@code @TableField}/{@code @TableName} 标注到自定义注解上来“扩展”它们
 * （元注解方式，因为两个注解的 {@code @Target} 均包含 {@code ElementType.ANNOTATION_TYPE}）。
 * <p>从而可在不依赖 io.swagger 的情况下，用自定义注解提供字段/表的中文注释。
 * <p>示例自定义注解：
 * <pre>{@code
 * @TableField
 * @Documented
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target(ElementType.FIELD)
 * public @interface MyField {
 *     String value() default "";       // 对应 TableField.value
 *     boolean exist() default true;    // 对应 TableField.exist
 *     boolean select() default true;   // 对应 TableField.select
 *     String comment() default "";     // 对应 TableField.comment
 * }
 * }</pre>
 *
 * @author dingkui
 */
public final class TableFieldMeta {

    private TableFieldMeta() {
    }

    /**
     * 读取字段上生效的 {@code @TableField} 注解。
     *
     * @param field 字段
     * @return 直接标注的 {@code @TableField}；若无，则返回标注了 {@code @TableField} 元注解的自定义注解实例；
     *         都无则返回 null
     */
    public static Annotation findField(Field field) {
        if (field == null) {
            return null;
        }
        TableField direct = field.getAnnotation(TableField.class);
        if (direct != null) {
            return direct;
        }
        for (Annotation ann : field.getAnnotations()) {
            if (ann.annotationType().getAnnotation(TableField.class) != null) {
                return ann;
            }
        }
        return null;
    }

    /**
     * 读取类上生效的 {@code @TableName} 注解。
     *
     * @param clazz 类
     * @return 直接标注的 {@code @TableName}；若无，则返回标注了 {@code @TableName} 元注解的自定义注解实例；
     *         都无则返回 null
     */
    public static Annotation findClass(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        TableName direct = clazz.getAnnotation(TableName.class);
        if (direct != null) {
            return direct;
        }
        for (Annotation ann : clazz.getAnnotations()) {
            if (ann.annotationType().getAnnotation(TableName.class) != null) {
                return ann;
            }
        }
        return null;
    }

    /**
     * 读取字段的 {@code TableField.value}（数据库字段名）。
     *
     * @param field 字段
     * @return 显式配置的 value；未配置返回 null
     */
    public static String value(Field field) {
        return readString(findField(field), "value");
    }

    /**
     * 读取字段的 {@code TableField.exist}。
     *
     * @param field 字段
     * @return 显式配置的 exist；未配置返回 null（由调用方决定默认值）
     */
    public static Boolean exist(Field field) {
        Object v = read(findField(field), "exist");
        return v instanceof Boolean ? (Boolean) v : null;
    }

    /**
     * 读取字段的 {@code TableField.select}。
     *
     * @param field 字段
     * @return 显式配置的 select；未配置返回 null（由调用方决定默认值）
     */
    public static Boolean select(Field field) {
        Object v = read(findField(field), "select");
        return v instanceof Boolean ? (Boolean) v : null;
    }

    /**
     * 读取字段的中文注释。
     * <p>优先级：直接/元注解 {@code @TableField.comment}。
     *
     * @param field 字段
     * @return 中文注释；未配置返回 null
     */
    public static String comment(Field field) {
        return readString(findField(field), "comment");
    }

    /**
     * 读取类的中文注释。
     * <p>优先级：直接/元注解 {@code @TableName.comment}。
     *
     * @param clazz 类
     * @return 中文注释；未配置返回 null
     */
    public static String comment(Class<?> clazz) {
        return readString(findClass(clazz), "comment");
    }

    private static String readString(Annotation ann, String methodName) {
        Object v = read(ann, methodName);
        return v == null ? null : v.toString();
    }

    private static Object read(Annotation ann, String methodName) {
        if (ann == null) {
            return null;
        }
        try {
            Method m = ann.annotationType().getDeclaredMethod(methodName);
            m.setAccessible(true);
            if (m.getReturnType() == void.class) {
                return null;
            }
            return m.invoke(ann);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
