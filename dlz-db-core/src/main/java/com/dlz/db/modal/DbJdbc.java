package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.JdbcExecute;
import com.dlz.db.modal.wrapper.JdbcQuery;

/**
 * 原生 SQL 门面，? 占位符。
 * <p>query() 用于 SELECT（返回 Builder，可 .one()/.list()/.count()/.value()）；
 * execute() 用于 INSERT/UPDATE/DELETE/DDL（直接返回 int）。
 *
 * <p>不做条件构造——需要条件用 DB.pojo 或 DB.table。
 * 不支持命名参数——需要 #{} 用 DB.sql。
 */
public class DbJdbc {

    /**
     * 查询（SELECT），返回 Builder 链式终结。
     * <pre>List&lt;User&gt; us = DB.jdbc.query("SELECT * FROM u WHERE id=?", 1).list(User.class);</pre>
     */
    public JdbcQuery query(String sql, Object... args) {
        return new JdbcQuery(sql, args);
    }

    /**
     * 执行（INSERT/UPDATE/DELETE/DDL），直接返回受影响行数。
     * <pre>int n = DB.jdbc.execute("UPDATE u SET status=? WHERE id=?", 1, 100);</pre>
     */
    public int execute(String sql, Object... args) {
        return new JdbcExecute(sql, args).execute();
    }
}
