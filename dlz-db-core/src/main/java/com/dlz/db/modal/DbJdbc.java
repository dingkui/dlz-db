package com.dlz.db.modal;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.dto.Page;
import com.dlz.db.modal.dto.PageRequest;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.modal.wrapper.JdbcExecute;
import com.dlz.db.modal.wrapper.JdbcSelect;

import java.util.List;

public class DbJdbc {
    public JdbcSelect selectWrapper(String sql, Object... params) {
        return new JdbcSelect(requireSql(sql), params);
    }

    public JdbcExecute executeWrapper(String sql, Object... params) {
        return new JdbcExecute(requireSql(sql), params);
    }

    public int execute(String sql, Object... params) {
        return executeWrapper(sql, params).execute();
    }

    public ResultMap one(String sql, Object... params) {
        return selectWrapper(sql, params).queryOne();
    }

    public <T> T one(String sql, Class<T> type, Object... params) {
        return selectWrapper(sql, params).queryOne(requireType(type));
    }

    public List<ResultMap> list(String sql, Object... params) {
        return selectWrapper(sql, params).queryList();
    }

    public <T> List<T> list(String sql, Class<T> type, Object... params) {
        return selectWrapper(sql, params).queryList(requireType(type));
    }

    public long count(String sql, Object... params) {
        return selectWrapper(sql, params).count();
    }

    public Page<ResultMap> page(String sql, PageRequest request, Object... params) {
        return selectWrapper(sql, params)
                .page(toPage(request))
                .queryPage();
    }

    public <T> Page<T> page(String sql, PageRequest request, Class<T> type, Object... params) {
        return selectWrapper(sql, params)
                .page(toPage(request))
                .queryPage(requireType(type));
    }

    private String requireSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new DbParameterException("sql must not be empty");
        }
        return sql;
    }

    private <T> Class<T> requireType(Class<T> type) {
        if (type == null) {
            throw new DbParameterException("type must not be null");
        }
        return type;
    }

    private Page<?> toPage(PageRequest request) {
        if (request == null) {
            throw new DbParameterException("request must not be null");
        }
        return Page.build(request.pageNo(), request.pageSize());
    }
}
