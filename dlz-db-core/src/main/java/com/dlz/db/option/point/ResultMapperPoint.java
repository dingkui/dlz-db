package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ResultMappingContext;

/** 最终结果类型转换的排他桩点。 */
public interface ResultMapperPoint<S, T> extends OptionPoint {
    T mapResult(ResultMappingContext<S, T> context);
}
