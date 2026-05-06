package com.dlz.db.core.abstractor;

import com.dlz.db.core.IRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 行映射器抽象适配器。
 * <p>提供行映射的默认实现，子类可选择性重写方法。</p>
 *
 * @param <T> 映射产出的对象类型
 * @since 7.0.0
 */
public abstract class ARowMapperAdapter<T> implements IRowMapper<T> {

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        throw new UnsupportedOperationException("请实现此方法");
    }
}
