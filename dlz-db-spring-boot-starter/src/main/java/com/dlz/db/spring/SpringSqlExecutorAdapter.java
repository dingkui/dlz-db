package com.dlz.db.spring;

import com.dlz.db.convertor.rowMapper.IRowMapper;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.core.func.ConnectionSupplier;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.util.DbLogUtil;
import com.dlz.kit.exception.ValidateException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * {@link ISqlExecutor} 的 Spring JDBC 实现。
 * <p>基于 {@link JdbcTemplate}，继承 Spring 事务上下文（{@code @Transactional} 自然生效）。</p>
 * <p>v7.0 起，原 {@code com.dlz.db.dao.DlzDao} 已被此类取代。</p>
 */
@Slf4j
public class SpringSqlExecutorAdapter implements ISqlExecutor {
    private final JdbcTemplate dao;

    @Override
    public ConnectionSupplier getConnectionSupplier() {
        return ()->{
//            DataSource ds = DB.Dynamic.getDataSource();
//
//            if (TransactionSynchronizationManager.hasResource(ds)) {
//                ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(ds);
//                conHolder.requested();
//                if (conHolder.getConnectionHandle()!=null) {
//                    return conHolder.getConnectionHandle().getConnection();
//                }
//            }

            return DataSourceUtils.getConnection(dao.getDataSource());
        };
    }

//    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JdbcTemplate由Spring容器管理，视为不可变")
    public SpringSqlExecutorAdapter(JdbcTemplate jdbcTemplate) {
        this.dao = jdbcTemplate;
    }

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_SPRING_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public List<ResultMap> getList(String sql, Object... args) {
        return getList(sql, DB.Dynamic.getRowMapper(), args);
    }

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_SPRING_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public <T> List<T> getList(String sql, IRowMapper<T> mapper, Object... args) {
        // 适配：自研 RowMapper -> Spring RowMapper（方法签名一致，方法引用即可）
        org.springframework.jdbc.core.RowMapper<T> springMapper = mapper::mapRow;
        return doDb(() -> args.length > 0 ? dao.query(sql, springMapper, args) : dao.query(sql, springMapper),
                (t, r) -> DbLogUtil.generateSqlMessage(t, r, "getList", sql, args));
    }

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_SPRING_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public int update(String sql, Object... args) {
        return doDb(() -> args.length > 0 ? dao.update(sql, args) : dao.update(sql),
                (t, r) -> DbLogUtil.generateSqlMessage(t, r, "update", sql, args));
    }

    @Override
    @SuppressFBWarnings(value = {"SQL_INJECTION_SPRING_JDBC", "SQL_INJECTION_JDBC", "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE"}, justification = "框架内部SQL执行入口，参数通过args绑定；PreparedStatement由JdbcTemplate接管关闭")
    public Long updateForId(String sql, Object... args) {
        return doDb(() -> {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            dao.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                int i = 1;
                for (Object arg : args) {
                    ps.setObject(i++, arg);
                }
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                log.warn("无自动增长主键");
                return null;
            }
            return key.longValue();
        }, (t, r) -> DbLogUtil.generateSqlMessage(t, r, "updateForId", sql, args));
    }

    @SuppressFBWarnings(value = "SQL_INJECTION_SPRING_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public void execute(final String sql, Object... args) {
        doDb(() -> {
            if (args.length > 0) {
                dao.execute(sql, (PreparedStatementCallback<Object>) ps -> {
                    for (int i = 0; i < args.length; i++) {
                        ps.setObject(i + 1, args[i]);
                    }
                    ps.execute();
                    return null;
                });
            } else {
                dao.execute(sql);
            }
            return 0;
        }, (t, r) -> DbLogUtil.generateSqlMessage(t, r, "execute", sql, args));
    }

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_SPRING_JDBC", justification = "框架内部SQL执行入口，参数通过args数组绑定")
    public int[] batch(String sql, List<Object[]> batchArgs) {
        return doDb(() -> dao.batchUpdate(sql, batchArgs),
                (t, r) -> DbLogUtil.generateSqlMessage(t, "batchUpdate", sql, batchArgs));
    }

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]+$");

    @Override
    @SuppressFBWarnings(value = "SQL_INJECTION_SPRING_JDBC", justification = "表名已通过白名单校验")
    public HashMap<String, Integer> getTableColumnsInfo(String tableName) {
        if (tableName == null || !TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new ValidateException("非法表名: " + tableName);
        }
        // 使用 WHERE 1=0 而非 LIMIT 0，确保跨数据库兼容性（Oracle不支持LIMIT语法）
        String sql = "select * from " + tableName + " where 1=0";
        ResultSetExtractor<HashMap<String, Integer>> extractor = rs -> {
            HashMap<String, Integer> infos = new HashMap<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i < columnCount + 1; i++) {
                // getColumnLabel 优先返回 AS 别名，无别名时返回列名
                // toUpperCase 统一处理不同数据库的大小写差异：
                // - Oracle: 默认返回大写
                // - PostgreSQL: 默认返回小写
                // - MySQL: 保持原始大小写
                String columnLabel = rsmd.getColumnLabel(i);
                if (columnLabel == null || columnLabel.isEmpty()) {
                    // 兜底：如果 getColumnLabel 返回空，使用 getColumnName
                    columnLabel = rsmd.getColumnName(i);
                }
                infos.put(columnLabel.toUpperCase(Locale.ROOT), rsmd.getColumnType(i));
            }
            return infos;
        };
        return dao.query(sql, extractor);
    }
}
