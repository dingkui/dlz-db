package com.dlz.db.spring;

import com.dlz.db.core.RowMapper;
import com.dlz.db.core.SqlExecutor;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.util.DbLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

/**
 * {@link SqlExecutor} 的 Spring JDBC 实现。
 * <p>基于 {@link JdbcTemplate}，继承 Spring 事务上下文（{@code @Transactional} 自然生效）。</p>
 * <p>v7.0 起，原 {@code com.dlz.db.dao.DlzDao} 已被此类取代。</p>
 */
@Slf4j
public class SpringSqlExecutor implements SqlExecutor {
    private final JdbcTemplate dao;

    public SpringSqlExecutor(JdbcTemplate jdbcTemplate) {
        this.dao = jdbcTemplate;
    }

    @Override
    public List<ResultMap> getList(String sql, Object... args) {
        return getList(sql, DB.Dynamic.getRowMapper(), args);
    }

    @Override
    public <T> List<T> getList(String sql, RowMapper<T> mapper, Object... args) {
        // 适配：自研 RowMapper -> Spring RowMapper（方法签名一致，方法引用即可）
        org.springframework.jdbc.core.RowMapper<T> springMapper = mapper::mapRow;
        return doDb(() -> args.length > 0 ? dao.query(sql, springMapper, args) : dao.query(sql, springMapper),
                (t, r) -> DbLogUtil.generateSqlMessage(t, r, "getList", sql, args));
    }

    @Override
    public int update(String sql, Object... args) {
        return doDb(() -> args.length > 0 ? dao.update(sql, args) : dao.update(sql),
                (t, r) -> DbLogUtil.generateSqlMessage(t, r, "update", sql, args));
    }

    @Override
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
            if (keyHolder.getKey() == null) {
                log.warn("无自动增长主键");
                return null;
            }
            return keyHolder.getKey().longValue();
        }, (t, r) -> DbLogUtil.generateSqlMessage(t, r, "updateForId", sql, args));
    }

    @Override
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
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        return doDb(() -> dao.batchUpdate(sql, batchArgs),
                (t, r) -> DbLogUtil.generateSqlMessage(t, "batchUpdate", sql, batchArgs));
    }

    @Override
    public HashMap<String, Integer> getTableColumnsInfo(String tableName) {
        String sql = "select * from " + tableName + " limit 0";
        ResultSetExtractor<HashMap<String, Integer>> extractor = rs -> {
            HashMap<String, Integer> infos = new HashMap<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i < columnCount + 1; i++) {
                String columnLabel = rsmd.getColumnLabel(i).toUpperCase();
                infos.put(columnLabel, rsmd.getColumnType(i));
            }
            return infos;
        };
        return dao.query(sql, extractor);
    }
}
