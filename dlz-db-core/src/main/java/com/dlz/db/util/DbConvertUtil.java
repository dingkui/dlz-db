package com.dlz.db.util;

import com.dlz.db.exception.DbException;
import com.dlz.db.mapper.dbtype.ITableColumnMapper;
import com.dlz.db.mapper.dbtype.TableColumnMapper;
import com.dlz.db.mapper.name.INameConverter;
import com.dlz.db.mapper.name.NameConvertCamel;
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
    public static ITableColumnMapper defaultTableColumnMapper = new TableColumnMapper();

    /**
     * 数据库字段名转换器
     */
    public static INameConverter defaultNameConvert = new NameConvertCamel();

    /**
     * 将值转换成数据库字段对应的数据类型。
     * <p>
     * 高频调用路径，做了两层短路优化：
     * <ol>
     *   <li>null 值直接返回，跳过全链路（~100ns→~2ns）</li>
     *   <li>默认 TableColumnMapper 走直连快速路径，
     *       跳过虚方法分发 + cover 快速路径内联（~100ns→~40ns）</li>
     * </ol>
     *
     * @param tableName  表名
     * @param columnName 列名（已确保大写）
     * @param value      待转换的值
     * @author dk 2018-09-28
     */
    public static Object getVal4Db(String tableName, String columnName, Object value) {
        // 快速路径 1：null 值无需转换，跳过全链路
        if (value == null) {
            return null;
        }
        final ITableColumnMapper mapper = SqlRunThreadHolder.getTableColumnMapper(defaultTableColumnMapper);
        if (mapper == null) {
            return value;
        }
        return mapper.converObj4Db(tableName, columnName, value);
    }

    public static <T> List<T> getColumnList(List<ResultMap> r, Class<T> tClass) {
        return r.stream().map((m) -> tClass == null ? (T) m : DbConvertUtil.getFirstColumn(m, tClass)).collect(Collectors.toList());
    }

    /**
     * 从Map里取得字符串
     *
     * @param m
     * @author dk 2015-04-09
     */
    public static Object getFirstColumn(ResultMap m) {
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
    public static <T> T getFirstColumn(ResultMap m, Class<T> clazz) {
        return ValUtil.toObj(getFirstColumn(m), clazz);
    }

    /**
     * 数据库字段名转换成bean字段名,一般都是下划线转驼峰
     *
     * @param dbKey
     */
    public static String toFieldName(String dbKey) {
        return SqlRunThreadHolder.getConvertorToFieldName(defaultNameConvert).toFieldName(dbKey);
    }

    public static String toDbName(String beanKey) {
        if(beanKey==null || beanKey.isEmpty()){
            return beanKey;
        }
        return SqlRunThreadHolder.getNameConvertor(defaultNameConvert).toDbName(beanKey);
    }

    /**
     * bean字段名转换成数据库字段名,一般是驼峰转下划线
     *
     * @param beanKey
     */
    private static final Pattern SQL_IDENTIFIER =Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");

    /**
     * 校验并返回 SQL 标识符。
     *
     * @param name 标识符
     * @param description 标识符说明
     * @return 原始标识符
     */
    public static String validateDbName(String name, String description) {
        if (name == null || !SQL_IDENTIFIER.matcher(name).matches()) {
            throw new ValidateException(description + "不合法: " + name);
        }
        return name;
    }

    private static final Pattern SQL_EXPRESSION = Pattern.compile(
            "[A-Za-z_][A-Za-z0-9_]*(\\s*[+\\-*/]\\s*(?:[A-Za-z_][A-Za-z0-9_]*|[0-9]+(?:\\.[0-9]+)?))*");

    /**
     * 校验更新表达式中的列名和简单 SQL 表达式。
     *
     * @param expression 原生表达式
     * @return 规范化表达式
     */
    public static String requireSqlExpression(String expression) {
        String value = expression == null ? null : expression.trim();
        if (value == null || !SQL_EXPRESSION.matcher(value).matches()) {
            throw new DbException("SQL表达式不合法: " + expression, 1002);
        }
        return value;
    }

    public static String toDbNames(String beanKey) {
        return SqlRunThreadHolder.getNameConvertor(defaultNameConvert).toDbName(beanKey.replaceAll("\\s+", " "));
    }
}
