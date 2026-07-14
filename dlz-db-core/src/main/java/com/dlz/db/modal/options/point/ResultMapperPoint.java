package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.ResultMappingContext;

/** 最终结果类型转换的排他桩点。 */
public interface ResultMapperPoint<S, T> extends OptionPoint {
    T mapResult(ResultMappingContext<S, T> context);
}
