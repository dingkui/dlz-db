package com.dlz.db.util;

import com.dlz.db.convertor.columnname.ColumnNameCamel;
import com.dlz.db.convertor.columnname.IColumnNameConvertor;
import com.dlz.db.convertor.dbtype.ITableColumnMapper;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.support.SqlRunThreadHolder;
import com.dlz.kit.exception.ValidateException;
import com.dlz.kit.util.ValUtil;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据库信息转换器
 *
 * @author dingkui 2017-06-26
 *
 */
public class DbConvertUtil {
    /**
     * 数据库字段信息及内容转换
     */
    public static ITableColumnMapper defaultTableColumnMapper = null;

    /**
     * 数据库字段名转换器
     */
    public static IColumnNameConvertor defaultColumnMapper = new ColumnNameCamel();

    /**
     * 将值转换成数据库字段对应的数据类型
     *
     * @param tableName
     * @param columnName
     * @param value
     * @author dk 2018-09-28
     */
    public static Object getVal4Db(String tableName, String columnName, Object value) {
        final ITableColumnMapper tableColumnMapper = SqlRunThreadHolder.getTableColumnMapper(defaultTableColumnMapper);
        return tableColumnMapper == null ? value : tableColumnMapper.converObj4Db(tableName, columnName, value);
    }

    public static <T> List<T> getColumnList(List<ResultMap> r, Class<T> tClass) {
        return r.stream().map((m) -> tClass == null ? (T) m : DbConvertUtil.getFistColumn(m, tClass)).collect(Collectors.toList());
    }

    /**
     * 从Map里取得字符串
     *
     * @param m
     * @author dk 2015-04-09
     */
    public static Object getFistColumn(ResultMap m) {
        if (m == null) {
            return null;
        }
        for (String a : m.keySet()) {
            if (!"ROWNUM_".equals(a) && !"rownum".equals(a)) {
                return m.get(a);
            }
        }
        return null;
    }

    /**
     * 从Map里取得字符串
     *
     * @param m
     * @author dk 2015-04-09
     */
    public static <T> T getFistColumn(ResultMap m, Class<T> clazz) {
        return ValUtil.toObj(getFistColumn(m), clazz);
    }

    /**
     * 数据库字段名转换成bean字段名,一般都是下划线转驼峰
     *
     * @param dbKey
     */
    public static String toFieldName(String dbKey) {
        return SqlRunThreadHolder.getConvertorToFieldName(defaultColumnMapper).toFieldName(dbKey);
    }

    private final static Pattern COLUMN_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]+$");
    public static String toDbColumnName(String beanKey) {
        beanKey = beanKey.replaceAll("\\s+", " ");
        if (beanKey == null || !COLUMN_NAME_PATTERN.matcher(beanKey).matches()) {
            throw new ValidateException("非法列名: " + beanKey);
        }
        return SqlRunThreadHolder.getColumnNameConvertor(defaultColumnMapper).toDbColumnName(beanKey);
    }

    /**
     * bean字段名转换成数据库字段名,一般是驼峰转下划线
     *
     * @param beanKey
     */
    public static String toDbColumnNames(String beanKey) {
        return SqlRunThreadHolder.getColumnNameConvertor(defaultColumnMapper).toDbColumnName(beanKey.replaceAll("\\s+", " "));
    }
}
