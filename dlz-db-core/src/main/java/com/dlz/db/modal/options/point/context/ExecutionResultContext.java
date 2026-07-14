package com.dlz.db.modal.options.point.context;

/** JDBC 调用成功后的泛型结果上下文。 */
public final class ExecutionResultContext<R> {
    private final ExecutionContext execution;
    private final R result;
    private final long elapsedNanos;

    public ExecutionResultContext(ExecutionContext execution, R result, long elapsedNanos) {
        if (execution == null) throw new IllegalArgumentException("execution must not be null");
        if (elapsedNanos < 0) throw new IllegalArgumentException("elapsedNanos must not be negative");
        this.execution = execution;
        this.result = result;
        this.elapsedNanos = elapsedNanos;
    }

    public ExecutionContext getExecution() { return execution; }
    public R getResult() { return result; }
    public long getElapsedNanos() { return elapsedNanos; }
}
