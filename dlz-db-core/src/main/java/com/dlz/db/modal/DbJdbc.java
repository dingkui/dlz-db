package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.JdbcExecute;
import com.dlz.db.modal.wrapper.JdbcSelect;

public class DbJdbc {
    public JdbcSelect select(String sql, Object... para) {
        return new JdbcSelect(sql, para);
    }
    public int execute(String sql, Object... para) {
        return new JdbcExecute(sql, para).execute();
    }
}