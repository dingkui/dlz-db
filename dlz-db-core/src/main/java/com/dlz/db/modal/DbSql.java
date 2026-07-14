package com.dlz.db.modal;

import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.wrapper.SqlExecute;
import com.dlz.db.modal.wrapper.SqlQuery;

import java.util.List;
import java.util.Map;

public class DbSql {
    public SqlQuery selectWrapper(String sqlKey, Map<String, Object>... maps) {
        final SqlQuery sqlQuery = new SqlQuery(RequireUtil.requireSqlKey(sqlKey));
        for (Map<String, Object> map : maps) {
            if(map != null && !map.isEmpty()){
                sqlQuery.addParas(map);
            }
        }
        return sqlQuery;
    }

    public SqlExecute executeWrapper(String sqlKey, Map<String, Object>... maps) {
        final SqlExecute sqlExecute = new SqlExecute(RequireUtil.requireSqlKey(sqlKey));
        for (Map<String, Object> map : maps) {
            if(map != null && !map.isEmpty()){
                sqlExecute.addParas(map);
            }
        }
        return sqlExecute;
    }

    public int execute(String sqlKey, Map<String, Object>... params) {
        return executeWrapper(sqlKey,params).execute();
    }

    public ResultMap one(String sqlKey, Map<String, Object>... params) {
        return selectWrapper(sqlKey,params).queryOne();
    }

    public <T> T one(String sqlKey, Class<T> type, Map<String, Object>... params) {
        return selectWrapper(sqlKey,params).queryOne(RequireUtil.requireType( type));
    }

    public List<ResultMap> list(String sqlKey, Map<String, Object>... params) {
        return selectWrapper(sqlKey,params).queryList();
    }

    public <T> List<T> list(String sqlKey, Class<T> type, Map<String, Object>... params) {
        return selectWrapper(sqlKey,params).queryList(RequireUtil.requireType( type));
    }

    public long count(String sqlKey, Map<String, Object>... params) {
        return selectWrapper(sqlKey,params).count();
    }
}
