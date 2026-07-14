package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.ExecutionErrorContext;

/** JDBC 调用失败时的异常转换链桩点。 */
public interface ExecutionErrorPoint extends OptionPoint {
    RuntimeException onExecutionError(ExecutionErrorContext context);
}
