package com.dlz.test.db.config;

import com.dlz.db.core.IRowMapper;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.kit.json.JSONMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测试用 SQL 执行器 - 基于内存数据存储
 */
public class MockSqlExecutor implements ISqlExecutor {

    /** 内存数据存储：表名 -> 行列表 */
    private final Map<String, List<JSONMap>> tableData = new ConcurrentHashMap<>();
    
    /** 自增ID计数器：表名 -> 当前ID */
    private final Map<String, Long> idCounters = new ConcurrentHashMap<>();
    
    /** 表结构信息：表名 -> 列名->类型映射 */
    private final Map<String, HashMap<String, Integer>> tableStructures = new ConcurrentHashMap<>();

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile(
            "(?:from|into|update|join)\\s+([a-zA-Z0-9_]+)", 
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 初始化测试数据
     */
    public void initTestData() {
        // 创建 user 表测试数据
        List<JSONMap> users = new ArrayList<>();
        users.add(createUser(1L, "张三", 25, "zhangsan@example.com", 1, null));
        users.add(createUser(2L, "李四", 30, "lisi@example.com", 1, null));
        users.add(createUser(3L, "王五", 28, "wangwu@example.com", 0, "2023-01-01"));
        tableData.put("user", users);
        idCounters.put("user", 3L);
        
        // 设置 user 表结构
        HashMap<String, Integer> userStructure = new HashMap<>();
        userStructure.put("ID", java.sql.Types.BIGINT);
        userStructure.put("NAME", java.sql.Types.VARCHAR);
        userStructure.put("AGE", java.sql.Types.INTEGER);
        userStructure.put("EMAIL", java.sql.Types.VARCHAR);
        userStructure.put("STATUS", java.sql.Types.INTEGER);
        userStructure.put("DELETE_TIME", java.sql.Types.VARCHAR);
        tableStructures.put("user", userStructure);
    }

    private JSONMap createUser(Long id, String name, int age, String email, int status, String deleteTime) {
        JSONMap user = new JSONMap();
        user.put("id", id);
        user.put("name", name);
        user.put("age", age);
        user.put("email", email);
        user.put("status", status);
        user.put("delete_time", deleteTime);
        return user;
    }

    @Override
    public List<ResultMap> getList(String sql, Object... args) {
        String tableName = extractTableName(sql);
        if (tableName == null) {
            return Collections.emptyList();
        }
        
        List<JSONMap> rows = tableData.getOrDefault(tableName.toLowerCase(), Collections.emptyList());
        
        // 转换为 ResultMap
        List<ResultMap> result = new ArrayList<>();
        for (JSONMap row : rows) {
            ResultMap resultMap = new ResultMap();
            resultMap.putAll(row);
            result.add(resultMap);
        }
        
        return result;
    }

    @Override
    public <T> List<T> getList(String sql, IRowMapper<T> mapper, Object... args) {
        // Mock 环境不支持真正的 ResultSet，返回空列表
        // 如需测试 Bean 映射，请使用 queryList() 获取 ResultMap 后手动转换
        return Collections.emptyList();
    }

    @Override
    public int update(String sql, Object... args) {
        String tableName = extractTableName(sql);
        if (tableName == null) {
            return 0;
        }
        
        List<JSONMap> rows = tableData.get(tableName.toLowerCase());
        if (rows == null) {
            return 0;
        }
        
        // 简单模拟：删除所有数据（实际应该解析 WHERE 条件）
        int oldSize = rows.size();
        rows.clear();
        return oldSize;
    }

    @Override
    public Long updateForId(String sql, Object... args) {
        String tableName = extractTableName(sql);
        if (tableName == null) {
            return null;
        }
        
        // 生成新ID
        Long newId = idCounters.getOrDefault(tableName.toLowerCase(), 0L) + 1;
        idCounters.put(tableName.toLowerCase(), newId);
        
        // 创建新记录
        JSONMap newRow = new JSONMap();
        newRow.put("id", newId);
        
        List<JSONMap> rows = tableData.computeIfAbsent(tableName.toLowerCase(), k -> new ArrayList<>());
        rows.add(newRow);
        
        return newId;
    }

    @Override
    public void execute(String sql, Object... args) {
        // 空实现，不抛异常
    }

    @Override
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        int[] results = new int[batchArgs.size()];
        Arrays.fill(results, 1);
        return results;
    }

    @Override
    public HashMap<String, Integer> getTableColumnsInfo(String tableName) {
        return tableStructures.getOrDefault(tableName.toUpperCase(), new HashMap<>());
    }

    /**
     * 从 SQL 中提取表名
     */
    private String extractTableName(String sql) {
        if (sql == null || sql.isEmpty()) {
            return null;
        }
        
        Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        
        return null;
    }

    /**
     * 添加测试数据（供测试用例使用）
     */
    public void addTestData(String tableName, JSONMap data) {
        List<JSONMap> rows = tableData.computeIfAbsent(tableName.toLowerCase(), k -> new ArrayList<>());
        rows.add(data);
    }

    /**
     * 清空测试数据（供测试用例使用）
     */
    public void clearTestData(String tableName) {
        tableData.remove(tableName.toLowerCase());
    }

    /**
     * 获取测试数据（供测试用例使用）
     */
    public List<JSONMap> getTestData(String tableName) {
        return tableData.getOrDefault(tableName.toLowerCase(), Collections.emptyList());
    }
}
