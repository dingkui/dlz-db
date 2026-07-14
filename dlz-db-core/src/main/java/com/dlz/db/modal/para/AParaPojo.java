package com.dlz.db.modal.para;

import com.dlz.db.exception.DbException;
import com.dlz.db.inf.ISqlPara;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.items.SqlItem;
import com.dlz.db.support.PojoCache;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 插入语句生成器
 *
 * @author dk
 */
public abstract class AParaPojo<T,P extends ParaMap> implements ISqlPara{
    private final Class<T> beanClass;
    protected T valueBean;
    protected T queryBean;
    private final String tableName;
    private final List<Field> fields;
    private boolean isGenerator = false;
    @Getter
    @Setter
    private P pm;

    public AParaPojo(Class<T> beanClass) {
        this.beanClass = beanClass;
        if(beanClass == Class.class){
            throw new DbException("bean需要为实体对象",1002);
        }
        this.valueBean =null;
        tableName= PojoCache.getTableName(beanClass);
        fields= PojoCache.getBeanFields(beanClass);
    }

    /** 当前构造器持有的 bean（从 Class 构造时为 null）。 */
    public T getValueBean() {
        return valueBean;
    }

    public String getTableName() {
        return tableName;
    }

    protected void generatWithBean() {
        if (!isGenerator) {
            if(valueBean != null){
                wrapValues(fields, valueBean);
            }
            if(queryBean != null){
                wrapQuery(fields, queryBean);
            }
            isGenerator = true;
        }
    }

    /**
     * 自动构建参数
     *
     * @param fields
     * @param bean
     */
    protected void wrapValues(List<Field> fields, T bean) {}


    /**
     * 自动构建参数
     *
     * @param fields
     * @param bean
     */
    protected void wrapQuery(List<Field> fields, T bean) {}


    public Class<T> getBeanClass() {
        return beanClass;
    }


    @Override
    public JdbcItem jdbcSql() {
        generatWithBean();
        return pm.jdbcSql();
    }

    @Override
    public JdbcItem jdbcCnt() {
        throw new RuntimeException("不支持");
    }

    @Override
    public SqlItem getSqlItem() {
        return pm.getSqlItem();
    }
}
