package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ExecutionResultContext;

/** JDBC 调用成功后的结果处理链桩点。 */
public interface AfterExecutionPoint extends OptionPoint {
    <R> R afterExecution(ExecutionResultContext<R> context);
}
