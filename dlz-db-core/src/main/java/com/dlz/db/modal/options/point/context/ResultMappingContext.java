package com.dlz.db.modal.options.point.context;

/** 最终结果类型映射的不可变上下文。 */
public final class ResultMappingContext<S, T> {
    private final S source;
    private final Class<T> targetType;

    public ResultMappingContext(S source, Class<T> targetType) {
        if (targetType == null) throw new IllegalArgumentException("targetType must not be null");
        this.source = source;
        this.targetType = targetType;
    }

    public S getSource() { return source; }
    public Class<T> getTargetType() { return targetType; }
}
