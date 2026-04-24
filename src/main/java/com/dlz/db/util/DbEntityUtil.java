package com.dlz.db.util;

import com.dlz.comm.exception.SystemException;
import com.dlz.db.holder.BeanInfoHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;

/**
 * 数据库实体工具类
 */
public class DbEntityUtil {

    /**
     * 获取实体的主键信息
     *
     * @param clazz 实体类
     * @return 主键信息
     * @throws SystemException 如果未设置可辨识的主键
     */
    public static String getIdName(Class<?> clazz) {
        final Field idField = BeanInfoHolder.getIdField(clazz);
        if (idField == null) {
            throw new SystemException(clazz.getSimpleName() + "未设置可辨识的主键");
        }
        return BeanInfoHolder.getColumnName(idField);
    }

    /**
     * 获取实体的主键信息
     *
     * @param clazz 实体类
     * @return 主键信息
     * @throws SystemException 如果未设置可辨识的主键
     */
    public static IdInfo getIdInfo(Class<?> clazz) {
        final Field idField = BeanInfoHolder.getIdField(clazz);
        if (idField == null) {
            throw new SystemException(clazz.getSimpleName() + "未设置可辨识的主键");
        }
        final String idName = BeanInfoHolder.getColumnName(idField);
        return new IdInfo(idField, idName);
    }

    /**
     * 主键信息封装类
     */
    @AllArgsConstructor
    @Getter
    public static class IdInfo {
        private final Field field;
        private final String name;
    }
}
