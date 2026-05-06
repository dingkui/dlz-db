package com.dlz.db.core.abstractor;

import com.dlz.db.core.IRowMapper;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.modal.dto.ResultMap;

import java.util.HashMap;
import java.util.List;

/**
 * SQL 执行器抽象适配器。
 * <p>提供 SQL 执行的默认实现，子类可选择性重写方法。</p>
 *
 * @since 7.0.0
 */
public abstract class ASqlExecutorAdapter implements ISqlExecutor {

    @Override
    public List<ResultMap> getList(String sql, Object... args) {
        throw new UnsupportedOperationException("请实现此方法");
    }

    @Override
    public <T> List<T> getList(String sql, IRowMapper<T> rowMapper, Object... args) {
        throw new UnsupportedOperationException("请实现此方法");
    }

    @Override
    public int update(String sql, Object... args) {
        throw new UnsupportedOperationException("请实现此方法");
    }

    @Override
    public Long updateForId(String sql, Object... args) {
        throw new UnsupportedOperationException("请实现此方法");
    }

    @Override
    public void execute(String sql, Object... args) {
        throw new UnsupportedOperationException("请实现此方法");
    }

    @Override
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        throw new UnsupportedOperationException("请实现此方法");
    }

    @Override
    public HashMap<String, Integer> getTableColumnsInfo(String tableName) {
        throw new UnsupportedOperationException("请实现此方法");
    }

    @Override
    public ResultMap getOne(String sql, boolean throwEx, Object... args) {
        throw new UnsupportedOperationException("请实现此方法");
    }
}
