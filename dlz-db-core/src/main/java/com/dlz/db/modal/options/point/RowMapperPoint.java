package com.dlz.db.modal.options.point;

import com.dlz.db.mapper.rowMapper.IRowMapper;
import com.dlz.db.modal.options.point.context.RowMapperContext;

/** 查询行映射器的排他选择桩点。 */
public interface RowMapperPoint extends OptionPoint {
    <T> IRowMapper<T> selectRowMapper(RowMapperContext<T> context);
}
