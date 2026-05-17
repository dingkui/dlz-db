package com.dlz.test.db.config;

import com.dlz.db.convertor.rowMapper.IRowMapper;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.core.func.ConnectionSupplier;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.kit.json.JSONMap;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQLite SQL 执行器 - 基于真实 JDBC 连接
 */
@Slf4j
public class SqliteSqlExecutor implements ISqlExecutor {

    private final DataSource dataSource;
    private final Map<String, HashMap<String, Integer>> tableStructures = new ConcurrentHashMap<>();

    public SqliteSqlExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public ConnectionSupplier getConnectionSupplier() {
        return () -> {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get connection", e);
            }
        };
    }

    @Override
    public List<ResultMap> getList(String sql, Object... args) {
        List<ResultMap> result = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // 设置参数
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            
            // 执行查询
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    ResultMap resultMap = new ResultMap();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        resultMap.put(columnName.toLowerCase(), value);
                    }
                    result.add(resultMap);
                }
            }
        } catch (SQLException e) {
            log.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Error executing query", e);
        }
        
        return result;
    }

    @Override
    public <T> List<T> getList(String sql, IRowMapper<T> mapper, Object... args) {
        List<T> result = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // 设置参数
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            
            // 执行查询
            try (ResultSet rs = ps.executeQuery()) {
                int rowNum = 0;
                while (rs.next()) {
                    result.add(mapper.mapRow(rs, rowNum++));
                }
            }
        } catch (SQLException e) {
            log.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Error executing query", e);
        }
        
        return result;
    }

    @Override
    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // 设置参数
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            
            // 执行更新
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error executing update: {}", sql, e);
            throw new RuntimeException("Error executing update", e);
        }
    }

    @Override
    public Long updateForId(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // 设置参数
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            
            // 执行更新
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }
            
            // 获取生成的ID
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            
            return null;
        } catch (SQLException e) {
            log.error("Error executing update for ID: {}", sql, e);
            throw new RuntimeException("Error executing update for ID", e);
        }
    }

    @Override
    public int[] batch(String sql, List<Object[]> batchArgs) {
        int[] results = new int[batchArgs.size()];
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < batchArgs.size(); i++) {
                Object[] args = batchArgs.get(i);
                
                // 设置参数
                for (int j = 0; j < args.length; j++) {
                    ps.setObject(j + 1, args[j]);
                }
                
                ps.addBatch();
            }
            
            // 执行批量操作
            results = ps.executeBatch();
        } catch (SQLException e) {
            log.error("Error executing batch: {}", sql, e);
            throw new RuntimeException("Error executing batch", e);
        }
        
        return results;
    }

    @Override
    public HashMap<String, Integer> getTableColumnsInfo(String tableName) {
        // 如果缓存中已有，直接返回
        if (tableStructures.containsKey(tableName.toUpperCase())) {
            return tableStructures.get(tableName.toUpperCase());
        }
        
        HashMap<String, Integer> columns = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), null)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    int dataType = rs.getInt("DATA_TYPE");
                    columns.put(columnName.toUpperCase(), dataType);
                }
            }
            
            // 缓存结果
            tableStructures.put(tableName.toUpperCase(), columns);
        } catch (SQLException e) {
            log.warn("Error getting table columns info for: {}", tableName, e);
            // 返回空映射而不是抛出异常
        }
        
        return columns;
    }
}