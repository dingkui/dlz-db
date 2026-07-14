package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.ExecutionResultContext;

/** JDBC 调用成功后的结果处理链桩点。 */
public interface AfterExecutionPoint extends OptionPoint {
    <R> R afterExecution(ExecutionResultContext<R> context);
}
