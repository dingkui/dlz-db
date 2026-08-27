package com.dlz.db.option.point;

import com.dlz.db.dialect.rowMapper.IRowMapper;
import com.dlz.db.option.point.context.RowMapperContext;

/** 查询行映射器的排他选择桩点。 */
public interface RowMapperPoint extends OptionPoint {
    <T> IRowMapper<T> selectRowMapper(RowMapperContext<T> context);
}
