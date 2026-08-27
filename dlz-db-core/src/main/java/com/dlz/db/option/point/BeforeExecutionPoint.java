package com.dlz.db.option.point;

import com.dlz.db.option.point.context.ExecutionContext;

/** JDBC 调用前的顺序生命周期桩点。 */
public interface BeforeExecutionPoint extends OptionPoint {
    void beforeExecution(ExecutionContext context);
}
