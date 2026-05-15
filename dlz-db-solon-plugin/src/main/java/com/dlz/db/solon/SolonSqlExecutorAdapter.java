package com.dlz.db.solon;

import com.dlz.db.convertor.rowMapper.IRowMapper;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.exception.DbException;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.util.DbLogUtil;
import com.dlz.kit.exception.ValidateException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * {@link ISqlExecutor} 的 Solon 实现：自研 SimpleJdbc，不依赖任何外部框架的事务管理。
 *
 * <h3>连接来源</h3>
 * <ol>
 *   <li>当前线程数据源由 {@link DB#Dynamic} 决定。</li>
 *   <li>若 {@link SolonConnectionHolder} 中存在该数据源的事务连接，则复用且执行后<b>不</b>关闭。</li>
 *   <li>否则新开连接，执行后立即关闭（短连接模式）。</li>
 * </ol>
 *
 * @since 7.0.0
 */
@Slf4j
public class SolonSqlExecutorAdapter implements ISqlExecutor {

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public List<ResultMap> getList(String sql, Object... args) {
        return getList(sql, DB.Dynamic.getRowMapper(), args);
    }

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public <T> List<T> getList(String sql, IRowMapper<T> rowMapper, Object... args) {
        return doDb(() -> withConn(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindArgs(ps, args);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> list = new ArrayList<>();
                    int rowNum = 0;
                    while (rs.next()) {
                        list.add(rowMapper.mapRow(rs, rowNum++));
                    }
                    return list;
                }
            }
        }), (t, r) -> DbLogUtil.generateSqlMessage(t, r, "getList", sql, args));
    }

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public int update(String sql, Object... args) {
        return doDb(() -> withConn(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindArgs(ps, args);
                return ps.executeUpdate();
            }
        }), (t, r) -> DbLogUtil.generateSqlMessage(t, r, "update", sql, args));
    }

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public Long updateForId(String sql, Object... args) {
        return doDb(() -> withConn(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindArgs(ps, args);
                ps.executeUpdate();
                // 1. 标准路径
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                } catch (SQLFeatureNotSupportedException ignore) {
                    // 驱动不支持（如部分版本的 SQLite），走兜底
                    log.warn("无自动增长主键");
                }
                // 2. SQLite 等兜底：按方言查最后插入 ID
                Long id = queryLastInsertIdByDialect(conn);
                if (id == null) {
                    log.warn("无自动增长主键");
                }
                return id;
            }
        }), (t, r) -> DbLogUtil.generateSqlMessage(t, r, "updateForId", sql, args));
    }

    /**
     * 根据连接所属数据库的方言返回最后插入的自增 ID。
     * <p>仅作为 {@link PreparedStatement#getGeneratedKeys()} 不可用时的兜底。</p>
     */
    @SuppressFBWarnings(value = {"SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"}, justification = "SQL字符串由方言固定常量拼接；getDatabaseProductName可能返回null")
    private Long queryLastInsertIdByDialect(Connection conn) {
        String selectLastId;
        try {
            String product = conn.getMetaData().getDatabaseProductName();
            if (product == null) return null;
            String p = product.toLowerCase(Locale.ROOT);
            if (p.contains("sqlite")) {
                selectLastId = "SELECT last_insert_rowid()";
            } else if (p.contains("mysql") || p.contains("mariadb")) {
                selectLastId = "SELECT LAST_INSERT_ID()";
            } else if (p.contains("h2") || p.contains("hsql")) {
                selectLastId = "CALL IDENTITY()";
            } else {
                return null;
            }
        } catch (Exception e) {
            log.debug("获取数据库产品名失败", e);
            return null;
        }
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(selectLastId)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            log.debug("方言兜底查询最后插入ID失败", e);
        }
        return null;
    }

    @Override
    @SuppressFBWarnings(value = {"SQL_INJECTION_JDBC", "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE"}, justification = "框架内部SQL执行入口，有参时使用PreparedStatement绑定，无参时sql由框架生成")
    public void execute(String sql, Object... args) {
        doDb(() -> withConn(conn -> {
            if (args != null && args.length > 0) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    bindArgs(ps, args);
                    ps.execute();
                }
            } else {
                try (Statement st = conn.createStatement()) {
                    st.execute(sql);
                }
            }
            return 0;
        }), (t, r) -> DbLogUtil.generateSqlMessage(t, r, "execute", sql, args));
    }

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public int[] batch(String sql, List<Object[]> batchArgs) {
        return doDb(() -> withConn(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Object[] args : batchArgs) {
                    bindArgs(ps, args);
                    ps.addBatch();
                }
                return ps.executeBatch();
            }
        }), (t, r) -> DbLogUtil.generateSqlMessage(t, "batch", sql, batchArgs));
    }

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]+$");

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_JDBC", justification = "表名已通过白名单校验")
    public HashMap<String, Integer> getTableColumnsInfo(String tableName) {
        if (tableName == null || !TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new ValidateException("非法表名: " + tableName);
        }
        String sql = "select * from " + tableName + " where 1=0";
        return withConn(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                HashMap<String, Integer> infos = new HashMap<>();
                ResultSetMetaData md = rs.getMetaData();
                int count = md.getColumnCount();
                for (int i = 1; i <= count; i++) {
                    infos.put(md.getColumnLabel(i).toUpperCase(Locale.ROOT), md.getColumnType(i));
                }
                return infos;
            }
        });
    }

    // ============== 内部工具 ==============

    /** 带连接的执行：自动复用事务连接或创建/关闭短连接。 */
    private <T> T withConn(SqlAction<T> action) {
        DataSource ds = DB.Dynamic.getDataSource();
        Connection bound = SolonConnectionHolder.get(ds);
        if (bound != null) {
            try {
                return action.run(bound);
            } catch (DbException e) {
                throw e;
            } catch (Exception e) {
                throw new DbException("SQL 执行失败：" + e.getMessage(), 1003, e);
            }
        }
        try (Connection conn = ds.getConnection()) {
            return action.run(conn);
        } catch (DbException e) {
            throw e;
        } catch (Exception e) {
            throw new DbException("SQL 执行失败：" + e.getMessage(), 1003, e);
        }
    }

    private static void bindArgs(PreparedStatement ps, Object... args) throws SQLException {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    @FunctionalInterface
    private interface SqlAction<T> {
        T run(Connection conn) throws Exception;
    }
}
