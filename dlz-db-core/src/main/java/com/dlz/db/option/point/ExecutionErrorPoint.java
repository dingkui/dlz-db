package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ExecutionErrorContext;

/** JDBC 调用失败时的异常转换链桩点。 */
public interface ExecutionErrorPoint extends OptionPoint {
    RuntimeException onExecutionError(ExecutionErrorContext context);
}
