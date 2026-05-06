package com.dlz.db.modal.wrapper;

import com.dlz.db.annotation.IdType;
import com.dlz.db.annotation.TableId;
import com.dlz.db.annotation.proxy.AnnoProxys;
import com.dlz.db.helper.support.SnowFlake;
import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.db.holder.DBHolder;
import com.dlz.db.holder.SqlHolder;
import com.dlz.db.holder.SqlRunThreadHolder;
import com.dlz.db.modal.para.AParaTable;
import com.dlz.db.modal.para.AQuery;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.exception.SystemException;
import com.dlz.kit.util.StringUtils;
import com.dlz.kit.util.id.UuidUtil;
import com.dlz.kit.util.system.FieldReflections;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * sql操作Util
 *
 * @author ding_kui 2010-12-14
 */
@Slf4j
public class WrapperBuildUtil {
    public static final String MAKER_SQL_INSERT = "insert into ${tableName}(${columns}) values(${values})";
    public static final String MAKER_SQL_DELETE = "delete from ${tableName} ${where}";
    public static final String MAKER_SQL_UPDATE = "update ${tableName} set ${sets} ${where}";
    public static final String MAKER_SQL_SEARCHE = "select ${columns} from ${tableName} t ${where} ${otherwhere}";

    private static final String MAKER_TABLENAME = "tableName";
    private static final String MAKER_COLUMS = "columns";
    private static final String MAKER_VALUES = "values";
    private static final String MAKER_STR_SETS = "sets";
    private static final String MAKER_WHERE = "where";
    public static final String logicDeleteField = DBHolder.getSqlConfig().getLogicDeleteField();

    /**
     * 生成查询条件sql
     *
     */
    public static void buildSql(AParaTable maker) {
        maker.getSqlItem().setSqlKey(maker.getSql());
        maker.addPara(MAKER_TABLENAME, maker.getTableName());
        if (maker instanceof AQuery) {
            buildWhere((AQuery) maker);
        }
        if (maker instanceof TableQuery) {
            buildWhereColumns((TableQuery) maker);
        }
        if (maker instanceof TableUpdate) {
            buildUpdateSql((TableUpdate) maker);
        }
        if (maker instanceof TableInsert) {
            buildInsertSql((TableInsert) maker);
        }
    }

    /**
     * 生成查询条件sql
     *
     */
    public static void buildWhereColumns(TableQuery maker) {
        maker.addPara(MAKER_COLUMS, maker.columns);
    }

    /**
     * 生成查询条件sql
     *
     */
    public static void buildWhere(AQuery maker) {
        if (SqlRunThreadHolder.isLogicDelete() && BeanInfoHolder.isColumnExists(maker.getTableName(), logicDeleteField)) {
            if (!maker.where().isContainCondition(logicDeleteField)) {
                maker.where().eq(logicDeleteField, 0);
            }
        }
        String where = maker.where().getRunsql(maker);
        if (!maker.isAllowFullQuery() && StringUtils.isEmpty(where)) {
            where = "where false";
        }
        maker.addPara(MAKER_WHERE, where);
    }

    /**
     * 生成掺入sql
     *
     */
    public static void buildInsertSql(TableInsert maker) {
        StringBuilder sbColumns = new StringBuilder();
        StringBuilder sbValues = new StringBuilder();
        if (maker.insertValues.isEmpty()) {
            throw new SystemException("插入字段信息未设置");
        }
        maker.insertValues.entrySet().forEach(e -> {
            String paraName = e.getKey();
            Object value = e.getValue();
            String clumnName = paraName.replaceAll("`", "");

            if (sbColumns.length() > 0) {
                sbColumns.append(',');
                sbValues.append(',');
            }
            sbColumns.append(paraName);
            if (value instanceof String) {
                String v = ((String) value);
                if (v.startsWith("sql:")) {
                    sbValues.append(DbConvertUtil.toDbColumnName(v.substring(4)));
                    return;
                }
            }
            sbValues.append("#{").append(clumnName).append("}");
            if (value == null)
                value = "";
            maker.addPara(clumnName, DbConvertUtil.getVal4Db(maker.getTableName(), clumnName, value));
        });
        maker.addPara(MAKER_COLUMS, sbColumns.toString());
        maker.addPara(MAKER_VALUES, sbValues.toString());
        maker.addPara(MAKER_TABLENAME, maker.getTableName());
    }

    /**
     * 生成更新信息
     *
     */
    public static void buildUpdateSql(TableUpdate maker) {
        StringBuilder sbSets = new StringBuilder();
        if (maker.updateSets.isEmpty()) {
            throw new SystemException("更新字段信息未设置");
        }
        maker.updateSets.entrySet().forEach(e -> {
            String paraName = e.getKey();
            Object value = e.getValue();
            String clumnName = paraName.replaceAll("`", "");

            if (sbSets.length() > 0) {
                sbSets.append(",");
            }
            sbSets.append(paraName);
            sbSets.append('=');
            if (value instanceof String) {
                String v = ((String) value);
                if (v.startsWith("sql:")) {
                    sbSets.append(DbConvertUtil.toDbColumnName(v.substring(4)));
                    return;
                }
            }
            sbSets.append("#{").append(clumnName).append("}");
            maker.addPara(clumnName, DbConvertUtil.getVal4Db(maker.getTableName(), clumnName, value));
        });
        maker.addPara(MAKER_STR_SETS, sbSets.toString());
    }


    public static String buildInsertSql(String dbName, List<Field> fields) {
        List<String> fieldsPart = new ArrayList<>();
        List<String> placeHolder = new ArrayList<>();
        for (Field field : fields) {
            String dbClumnName = BeanInfoHolder.getColumnName(field);
            if (!dbClumnName.equals("")) {
                fieldsPart.add(dbClumnName);
                placeHolder.add("?");
            }
        }
        return "INSERT INTO " + dbName + " (" + StringUtils.join(",", fieldsPart) + ") VALUES (" + StringUtils.join(",", placeHolder) + ")";
    }

    public static Object[] buildInsertParams(Object object, List<Field> fields) {
        List<Object> params = new ArrayList<>();
        for (Field field : fields) {
            String dbClumnName = BeanInfoHolder.getColumnName(field);
            if (!dbClumnName.equals("")) {
                params.add(FieldReflections.getValue(object, field));
            }
        }
        return params.toArray();
    }

    public static String buildUpdateSql(String dbName, List<Field> fields,String idName) {
        List<String> fieldsPart = new ArrayList<String>();
        for (Field field : fields) {
            String dbClumnName = BeanInfoHolder.getColumnName(field);
            if (!dbClumnName.equals(idName) && !dbClumnName.equals("")) {
                fieldsPart.add(dbClumnName + "=?");
            }
        }
        return "UPDATE " + dbName + " SET " + StringUtils.join(",", fieldsPart) + " WHERE "+idName+" = ?";
    }

    public static Object[] buildUpdateParams(Object object, List<Field> fields,String idName) {
        List<Object> params = new ArrayList<Object>(fields.size());
        Field idField = null;
        for (Field field : fields) {
            String dbClumnName = BeanInfoHolder.getColumnName(field);
            if(dbClumnName.equals(idName)){
                idField = field;
                continue;
            }
            if (!dbClumnName.equals("")) {
                params.add(FieldReflections.getValue(object, field));
            }
        }
        params.add(FieldReflections.getValue(object, idField));
        return params.toArray();
    }

    public static IdType getIdType(Field field) {
        final TableId annotation = field.getAnnotation(TableId.class);
        if (annotation != null) {
            return annotation.type();
        }
        return AnnoProxys.MybatisPlusIdType.type(field);
    }

    public static Object getIdValue(Field field, String tableName) {
        IdType type = getIdType( field);
        if (type == null || type == IdType.AUTO || type == IdType.INPUT) {
            return null;
        } else {
            final String columnName = BeanInfoHolder.getColumnName(field);
            if (type == IdType.ASSIGN_ID) {
                return SnowFlake.id();
            } else if (type == IdType.ASSIGN_UUID) {
                return UuidUtil.uuid();
            } else if (type == IdType.SEQ) {
                return DBHolder.sequence(tableName, 1l);
            } else {
                throw new SystemException(columnName + " idType is " + type + " but null");
            }
        }
    }

    public static void fillAutoId(String dbName,Field idField, IdType idType ,Object obj) {
        if (idType != null) {
            Object idValue = FieldReflections.getValue(obj, idField);
            if(idValue == null){
                if (idType == IdType.INPUT) {
                    throw new SystemException(obj.getClass().getSimpleName()+"."+idField.getName()+"为手动输入,不能为空");
                }
                if(idType != IdType.AUTO){
                    idValue = WrapperBuildUtil.getIdValue(idField, dbName);
                    if(idValue == null){
                        throw new SystemException(obj.getClass().getSimpleName()+"."+idField.getName()+"自动构建id失败");
                    }
                    FieldReflections.setValue(obj, idField, idValue);
                }
            }
        }
    }

}
