package com.dlz.db.modal;

import com.dlz.db.modal.wrapper.SqlExecute;
import com.dlz.db.modal.wrapper.SqlQuery;

/**
 * 预设 SQL 门面，#{} 命名参数。
 * <p>支持三种 SQL 来源：
 * <ol>
 *   <li>内联：直接写 "SELECT * FROM u WHERE id=#{id}"</li>
 *   <li>文件预设：&lt;sql id="key.selectUser"&gt;...&lt;/sql&gt;</li>
 *   <li>数据库预设：selectUser= SELECT ...（运行时可改，无需重启）</li>
 * </ol>
 *
 * <p>不支持 ? 占位——需要 ? 用 DB.jdbc。
 */
public class DbSql {

    /**
     * 查询（SELECT），返回 Builder，.set(k,v) 绑定命名参数后终结。
     * <pre>List&lt;User&gt; us = DB.sql.query("WHERE id=#{id}").set("id",1).list(User.class);</pre>
     */
    public SqlQuery query(String sql) {
        return new SqlQuery(sql);
    }

    /**
     * 执行变更类 SQL，返回 Builder，.set(k,v) 绑定参数后 .execute() 终结。
     * <pre>int n = DB.sql.execute("user.deactivate").set("userId",100).execute();</pre>
     */
    public SqlExecute execute(String sql) {
        return new SqlExecute(sql);
    }
}
