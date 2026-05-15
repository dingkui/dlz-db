package com.dlz.db.core;

import com.dlz.db.convertor.rowMapper.IRowMapper;
import com.dlz.db.exception.DbException;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.db.util.DbConvertUtil;
import com.dlz.db.util.DbLogUtil;
import com.dlz.kit.fn.DlzFn2;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * SQL 执行器抽象：DLZ-DB 核心对 JDBC 操作的顶层接口。
 * <p>实现类由各 starter 提供：
 * <ul>
 *   <li>Spring Boot：基于 {@code JdbcTemplate}（{@code com.dlz.db.spring.SpringSqlExecutor}）</li>
 *   <li>Solon：基于自研 {@code SimpleJdbc}（规划中）</li>
 * </ul>
 * </p>
 * <p>用户通常无需直接使用，而是通过 {@code DB.Pojo} / {@code DB.Jdbc} / {@code DB.Sql} 等上层 API 访问。</p>
 *
 * @author dingkui
 * @since 7.0.0
 */
public interface ISqlExecutor {

    List<ResultMap> getList(String sql, Object... args);

    <T> List<T> getList(String sql, IRowMapper<T> rowMapper, Object... args);

    int update(String sql, Object... args);

    Long updateForId(String sql, Object... args);

    void execute(String sql, Object... args);

    int[] batch(String sql, List<Object[]> batchArgs);

    HashMap<String, Integer> getTableColumnsInfo(String tableName);

    // ============== 默认工具方法 ==============

    default ResultMap getOne(String sql, boolean checkOne, Object... args) {
        List<ResultMap> list = getList(sql, args);
        if (list.isEmpty()) {
            return null;
        }
        if (checkOne && list.size() > 1) {
            throw new DbException("查询结果为多条", 1004);
        }
        return list.get(0);
    }

    default <T> T getFistColumn(String sql, Class<T> requiredType, Object... args) {
        return DbConvertUtil.getFistColumn(getOne(sql, false, args), requiredType);
    }

    /**
     * 统一的 SQL 执行包装：日志、耗时、异常转换。
     *
     * @param s   真实执行逻辑
     * @param msg 日志消息生成器（耗时 + 结果 -> 消息）
     */
    default <T> T doDb(Supplier<T> s, DlzFn2<Long, T, String> msg) {
        if (msg == null) {
            return s.get();
        }
        long t = System.currentTimeMillis();
        T re = null;
        Exception err = null;
        try {
            re = s.get();
            return re;
        } catch (Exception e) {
            err = e;
            if (e instanceof DbException) {
                throw e;
            }
            throw new DbException("sql执行错误:", 1001);
        } finally {
            DbLogUtil.logInfo(msg, t, re, err);
        }
    }
}
