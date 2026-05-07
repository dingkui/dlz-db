package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.JdbcExecute;
import com.dlz.db.modal.wrapper.JdbcQuery;

public class DbJdbc {
    public JdbcQuery select(String sql, Object... para) {
        return new JdbcQuery(sql, para);
    }

    public int insert(String sql, Object... para) {
        return new JdbcExecute(sql, para).execute();
    }

    public int update(String sql, Object... para) {
        return new JdbcExecute(sql, para).execute();
    }

    public int delete(String sql, Object... para) {
        return new JdbcExecute(sql, para).execute();
    }

    public int execute(String sql, Object... para) {
        return new JdbcExecute(sql, para).execute();
    }
}