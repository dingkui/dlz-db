package com.dlz.db.service;

import com.dlz.db.annotation.IdType;
import com.dlz.db.holder.BeanInfoHolder;
import com.dlz.db.inf.IExecutorInsert;
import com.dlz.db.inf.IExecutorUDI;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.db.modal.wrapper.WrapperBuildUtil;
import com.dlz.kit.util.system.FieldReflections;

import java.lang.reflect.Field;

/**
 * 从数据库中取得单条map类型数据：{adEnddate=2015-04-08 13:47:12.0}
 * sql语句，可以带参数如：select AD_ENDDATE from JOB_AD t where ad_id=#{ad_id}
 * paraMap ：Map<String,Object> m=new HashMap<String,Object>();m.put("ad_id", "47");
 *
  * @throws Exception
 */
public interface IDbExecuteService extends IDbBaseService{

    /**
     * 插入数据库
     * sql语句，可以带参数如：update JOB_AD set AD_text=#{adText} where ad_id in (${ad_id})
     *
     * @param paraMap ：Map<String,Object> m=new HashMap<String,Object>();m.put("ad_id", "47");
          * @throws Exception
     */
    default Long insertWithAutoKey(IExecutorInsert paraMap) {
        return doDb(paraMap, jdbcSql -> getSqlExecutor().updateForId(jdbcSql.sql, jdbcSql.paras));
    }

    /**
     * 更新或插入数据库
     * sql语句，可以带参数如：update JOB_AD set AD_text=#{adText} where ad_id in (${ad_id})
     *
     * @param paraMap ：Map<String,Object> m=new HashMap<String,Object>();m.put("ad_id", "47");
     * @return 影响行数
     */
    default int execute(IExecutorUDI paraMap) {
        if (paraMap instanceof PojoInsert) {
            PojoInsert p = (PojoInsert) paraMap;
            Object bean = p.getBean();
            final Field idField = BeanInfoHolder.getIdField(bean.getClass());
            final IdType idType = idField != null ? WrapperBuildUtil.getIdType(idField) : null;
            if(idType != null){
                WrapperBuildUtil.fillAutoId(BeanInfoHolder.getTableName( bean.getClass()), idField, idType, bean);
            }
            if(idType == IdType.AUTO){
                final Long newid = doDb(paraMap, jdbcSql -> getSqlExecutor().updateForId(jdbcSql.sql, jdbcSql.paras));
                if(newid != null){
                    FieldReflections.setValue(bean, idField, newid);
                }
                return 1;
            }
        }
        return doDb(paraMap, jdbcSql -> getSqlExecutor().update(jdbcSql.sql, jdbcSql.paras));
    }

//    default <T> long insert(T bean){
//        return execute(PojoInsert.wrapper(bean));
//    }
//
//    default <T> long insertWithAutoKey(T bean){
//        final Long newid = insertWithAutoKey(PojoInsert.wrapper(bean));
//        if(newid != null){
//            FieldReflections.setValue(bean, "id", newid);
//        }
//        return newid;
//    }
//
//    default <T> long delete(T bean){
//        return delete(PojoDelete.wrapper(bean));
//    }
//
//    default <T> int updateByIdOrInsert(T bean){
//        Object id = FieldReflections.getValue(bean, "id",false);
//        if(StringUtils.isEmpty(id)){
//            execute(PojoInsert.wrapper(bean));
//            return 1;
//        }
//        return execute(PojoUpdate.wrapper((Class<T>)bean.getClass()).eq("id",id).set(bean));
//    }
//    default <T> int updateById(T bean){
//        Object id = FieldReflections.getValue(bean, "id",false);
//        if(StringUtils.isEmpty(id)){
//            throw new ValidateException("id不能为空");
//        }
//        return execute(PojoUpdate.wrapper((Class<T>)bean.getClass()).eq("id",id).set(bean));
//    }
//    default <T> int deleteById(String id,Class<T> clazz){
//        if(StringUtils.isEmpty(id)){
//            throw new ValidateException("id不能为空");
//        }
//        return execute(PojoDelete.wrapper(clazz).eq("id",id));
//    }
//    default <T> int deleteByIds(String id,Class<T> clazz){
//        if(StringUtils.isEmpty(id)){
//            throw new ValidateException("id不能为空");
//        }
//        return execute(PojoDelete.wrapper(clazz).in("id",id));
//    }
}
