package com.dlz.db.dialect;

import com.dlz.db.dialect.rowMapper.ResultMapRowMapper;

/** 结果集映射能力。 */
@FunctionalInterface
public interface ResultMappingDialect {
    ResultMapRowMapper createRowMapper();
}
