package com.dlz.db.modal.wrapper;

import com.dlz.comm.fn.DlzFn;
import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.db.modal.para.AQuery;
import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.util.DbConvertUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


/**
 * 构造单表的更新操作sql
 *
 * @author dingkui
 */
@Slf4j
public class TableUpdate extends AQuery<TableUpdate> implements IExecutorUDI {
    private static final long serialVersionUID = 8374167270612933157L;
    final Map<String, Object> updateSets = new HashMap<>();

    public TableUpdate(String tableName) {
        super(tableName);
    }

    public TableUpdate set(String paraName, Object value) {
        paraName = DbConvertUtil.toDbColumnNames(paraName);
        if (!BeanInfoHolder.isColumnExists(getTableName(),paraName)) {
            log.warn("column is not exists:" + getTableName() + "." + paraName);
            return this;
        }
        updateSets.put(paraName, value);
        return this;
    }

    public <T> void set(DlzFn<T, ?> column, Object value) {
        set(BeanInfoHolder.fnName(column), value);
    }

    /**
     * 添加要更新的值集合
     *
     * @param setValues
          */
    public TableUpdate set(Map<String, Object> setValues) {
        for (String str : setValues.keySet()) {
            set(str, setValues.get(str));
        }
        return this;
    }

    @Override
    public TableUpdate me() {
        return this;
    }

    @Override
    public String getSql() {
        return WrapperBuildUtil.MAKER_SQL_UPDATE;
    }
}
