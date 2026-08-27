package com.dlz.db.option;

import com.dlz.db.internal.holder.PojoCache;
import com.dlz.db.internal.holder.SqlRunThreadHolder;
import com.dlz.db.option.point.DeleteModePoint;
import com.dlz.db.option.point.DeletedDataPoint;
import com.dlz.db.option.point.InsertFieldPoint;
import com.dlz.db.option.point.LogicDeleteValuePoint;
import com.dlz.db.option.point.WherePoint;
import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.option.point.context.DeleteMode;
import com.dlz.db.option.point.context.DeletedDataMode;
import com.dlz.db.option.point.context.FieldContribution;
import com.dlz.db.option.point.context.FieldContext;
import com.dlz.db.option.point.context.FieldValue;
import com.dlz.db.option.point.context.ValueContext;
import com.dlz.db.sql.SqlFragment;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.kit.json.JSONMap;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Locale;

/**
 * 逻辑删除选项。
 *
 * <p>通过实现 {@link WherePoint}、{@link InsertFieldPoint}、{@link LogicDeleteValuePoint}
 * 三个桩点参与构建：
 * <table border="1">
 * <tr><th>操作</th><th>自动行为</th></tr>
 * <tr><td>查询/更新/删除</td><td>WHERE 追加 {@code deleted = 0}</td></tr>
 * <tr><td>插入</td><td>VALUES 追加 {@code deleted = 0}</td></tr>
 * <tr><td>删除</td><td>由框架改写为 UPDATE {@code deleted = 1}</td></tr>
 * </table>
 *
 * <p>默认由 {@code DB.config.logicDeleteField(...)} 注册的拦截器全局供给；
 * 也可在单次操作中显式传入以覆盖（同 key 调用点优先）。
 */
public class LogicDeleteOption implements DbOption, WherePoint, InsertFieldPoint, LogicDeleteValuePoint {
    private static final long serialVersionUID = 1L;
    private static final String WHERE_PARA_KEY = "logicDeleteValue";

    private final String fieldName;
    private final String dbColumnName;

    public LogicDeleteOption(String fieldName) {
        fieldName = fieldName.toLowerCase(Locale.ROOT);
        this.fieldName = DbConvertUtil.toFieldName(fieldName);
        this.dbColumnName = DbConvertUtil.toDbName(fieldName);
    }

    @Override
    public String key() {
        return "logicDelete";
    }

    public boolean isEnabled() {
        return dbColumnName != null && !dbColumnName.isEmpty();
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDbColumnName() {
        return dbColumnName;
    }

    // ==================== WherePoint ====================

    @Override
    public SqlFragment contributeWhere(CrudContext context) {
        if (SqlRunThreadHolder.isIgnoreLogicDelete()) {
            return null;
        }
        final String tableName = context.getTableName();
        if (!PojoCache.isColumnExists(tableName, dbColumnName)) {
            return null;
        }
        DeletedDataPoint deletedData = context.getOptions().getPointBindings().single(DeletedDataPoint.class);
        if (deletedData != null && deletedData.chooseDeletedData(context) == DeletedDataMode.INCLUDE) {
            return null;
        }
        DeleteModePoint deleteMode = context.getOptions().getPointBindings().single(DeleteModePoint.class);
        if (deleteMode != null && deleteMode.chooseDeleteMode(context) == DeleteMode.PHYSICAL) {
            return null;
        }
        if (context.getWhereColumns().contains(dbColumnName)) {
            return null;
        }
        JSONMap paras = new JSONMap();
        // WHERE 过滤目标始终是"未删除"行，与操作类型无关
        paras.put(WHERE_PARA_KEY, DbConvertUtil.getVal4Db(tableName, dbColumnName, notDeletedMarker()));
        return SqlFragment.of(dbColumnName + " = #{" + WHERE_PARA_KEY + "}", paras);
    }

    // ==================== InsertFieldPoint ====================

    @Override
    public FieldContribution contributeInsertFields(FieldContext context) {
        if (SqlRunThreadHolder.isIgnoreLogicDelete()) {
            return FieldContribution.EMPTY;
        }
        if (!PojoCache.isColumnExists(context.getTableName(), dbColumnName)) {
            return FieldContribution.EMPTY;
        }
        if (context.getFieldNames().contains(dbColumnName)) {
            return FieldContribution.EMPTY;
        }
        return new FieldContribution(Collections.<String>emptyList(),
                Collections.singletonList(new FieldValue(dbColumnName, markerValue(context.getOperation()))));
    }

    // ==================== LogicDeleteValuePoint ====================

    @Override
    public Object provideLogicDeleteValue(ValueContext context) {
        return markerValue(context.getOperation());
    }

    /** 删除标记值：DELETE 改写为 UPDATE 时的 SET 值；其他操作（INSERT 默认值）为未删除标记。 */
    private Object markerValue(DbOperation operation) {
        return operation == DbOperation.DELETE ? 1 : 0;
    }

    /** WHERE 过滤使用的"未删除"标记值。 */
    private Object notDeletedMarker() {
        return 0;
    }

    // ==================== 框架内部使用的辅助方法 ====================

    /** 当前线程是否跳过逻辑删除且表含逻辑删除字段（DELETE 改写前的判断）。 */
    public boolean appliesTo(String tableName) {
        return !SqlRunThreadHolder.isIgnoreLogicDelete()
                && PojoCache.isColumnExists(tableName, dbColumnName);
    }

    /** 获取指定表的逻辑删除字段名（Bean 属性形式）；未启用或表无该字段返回 null。 */
    public String getLogicDeleteField(String tableName) {
        if (SqlRunThreadHolder.isIgnoreLogicDelete()) {
            return null;
        }
        if (!PojoCache.isColumnExists(tableName, dbColumnName)) {
            return null;
        }
        return fieldName;
    }

    /** 获取指定表的逻辑删除字段（Bean Field 形式）；未启用或表无该字段返回 null。 */
    public Field getLogicDeleteField(String tableName, Class<?> beanClass) {
        if (SqlRunThreadHolder.isIgnoreLogicDelete()) {
            return null;
        }
        if (!PojoCache.isColumnExists(tableName, dbColumnName)) {
            return null;
        }
        return PojoCache.getLogicDeleteInfo(beanClass, fieldName);
    }
}
