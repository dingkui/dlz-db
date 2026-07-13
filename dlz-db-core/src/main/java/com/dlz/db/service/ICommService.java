package com.dlz.db.service;

/**
 * 从数据库中取得单条map类型数据：{adEnddate=2015-04-08 13:47:12.0}
 * sql语句，可以带参数如：select AD_ENDDATE FROM JOB_AD t WHERE ad_id=#{ad_id}
 * paraMap ：Map<String,Object> m=new HashMap<String,Object>();m.put("ad_id", "47");
 */
public interface ICommService extends IDbJdbcService, IDbExecuteService, IDbOneService, IDbListService, IDbColumnService {


}
