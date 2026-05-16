//package com.dlz.db.core;
//
//import com.dlz.db.convertor.rowMapper.IRowMapper;
//import com.dlz.db.exception.DbException;
//import com.dlz.db.modal.DB;
//import com.dlz.db.modal.dto.ResultMap;
//import com.dlz.db.util.DbLogUtil;
//import com.dlz.kit.exception.ValidateException;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.function.Supplier;
//
///**
// * {@link ISqlExecutor} 的 Solon 实现：自研 SimpleJdbc，不依赖任何外部框架的事务管理。
// *
// * <h3>连接来源</h3>
// * <ol>
// *   <li>当前线程数据源由 {@link DB#Dynamic} 决定。</li>
// *   <li>若存在该数据源的事务连接，则复用且执行后<b>不</b>关闭。</li>
// *   <li>否则新开连接，执行后立即关闭（短连接模式）。</li>
// * </ol>
// *
// * @since 7.0.0
// */
//public interface SolonSqlExecutorAdapter extends ISqlExecutor {
//    Supplier<Connection> getConnectionSupplier();
//
//    @Override
//    default List<ResultMap> getList(String sql, Object... args) {
//        return getList(sql, DB.Dynamic.getRowMapper(), args);
//    }
//
//    @Override
//    default <T> List<T> getList(String sql, IRowMapper<T> rowMapper, Object... args) {
//        return NativeSqlUtil.doConn(conn -> {
//                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                        NativeSqlUtil.bindArgs(ps, args);
//                        try (ResultSet rs = ps.executeQuery()) {
//                            List<T> list = new ArrayList<>();
//                            int rowNum = 0;
//                            while (rs.next()) {
//                                list.add(rowMapper.mapRow(rs, rowNum++));
//                            }
//                            return list;
//                        }
//                    }
//                },
//                getConnectionSupplier(),
//                (t, r) -> DbLogUtil.generateSqlMessage(t, r, "getList", sql, args));
//    }
//
//    @Override
//    default int update(String sql, Object... args) {
//        return NativeSqlUtil.doConn(conn -> {
//                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                        NativeSqlUtil.bindArgs(ps, args);
//                        return ps.executeUpdate();
//                    }
//                },
//                getConnectionSupplier(),
//                (t, r) -> DbLogUtil.generateSqlMessage(t, r, "update", sql, args));
//    }
//
//    @Override
//    default Long updateForId(String sql, Object... args) {
//        return NativeSqlUtil.doConn(conn -> {
//                    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//                        NativeSqlUtil.bindArgs(ps, args);
//                        ps.executeUpdate();
//                        // 1. 标准路径
//                        try (ResultSet keys = ps.getGeneratedKeys()) {
//                            if (keys.next()) {
//                                return keys.getLong(1);
//                            }
//                        } catch (SQLFeatureNotSupportedException ignore) {
//                            // 驱动不支持（如部分版本的 SQLite），走兜底
//                            DbLogUtil.warn("无自动增长主键", ignore);
//                        }
//                        // 2. SQLite 等兜底：按方言查最后插入 ID
//                        Long id = NativeSqlUtil.queryLastInsertIdByDialect(conn);
//                        if (id == null) {
//                            throw new DbException("无自动增长主键", 1002);
//                        }
//                        return id;
//                    }
//                },
//                getConnectionSupplier(),
//                (t, r) -> DbLogUtil.generateSqlMessage(t, r, "updateForId", sql, args));
//    }
//
//    @Override
//    default int[] batch(String sql, List<Object[]> batchArgs) {
//        return NativeSqlUtil.doConn(conn -> {
//                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                        for (Object[] args : batchArgs) {
//                            NativeSqlUtil.bindArgs(ps, args);
//                            ps.addBatch();
//                        }
//                        return ps.executeBatch();
//                    }
//                }, getConnectionSupplier(),
//                (t, r) -> DbLogUtil.generateSqlMessage(t, "batch", sql, batchArgs));
//    }
//
//
//    @Override
//    default HashMap<String, Integer> getTableColumnsInfo(String tableName) {
//        if (tableName == null || !NativeSqlUtil.TABLE_NAME_PATTERN.matcher(tableName).matches()) {
//            throw new ValidateException("非法表名: " + tableName);
//        }
//        // 使用 WHERE 1=0 而非 LIMIT 0，确保跨数据库兼容性（Oracle不支持LIMIT语法）
//        String sql = "select * from " + tableName + " where 1=0";
//        return NativeSqlUtil.doConn(conn -> {
//            try (PreparedStatement ps = conn.prepareStatement(sql);
//                 ResultSet rs = ps.executeQuery()) {
//                HashMap<String, Integer> infos = new HashMap<>();
//                ResultSetMetaData md = rs.getMetaData();
//                int count = md.getColumnCount();
//                for (int i = 1; i <= count; i++) {
//                    // getColumnLabel 优先返回 AS 别名，无别名时返回列名
//                    // toUpperCase 统一处理不同数据库的大小写差异：
//                    // - Oracle: 默认返回大写
//                    // - PostgreSQL: 默认返回小写
//                    // - MySQL: 保持原始大小写
//                    String columnName = md.getColumnLabel(i);
//                    if (columnName == null || columnName.isEmpty()) {
//                        // 兜底：如果 getColumnLabel 返回空，使用 getColumnName
//                        columnName = md.getColumnName(i);
//                    }
//                    infos.put(columnName.toUpperCase(Locale.ROOT), md.getColumnType(i));
//                }
//                return infos;
//            }
//        }, getConnectionSupplier(), null);
//    }
//}
