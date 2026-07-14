package com.dlz.db.modal.options.point.context;

/** JDBC 调用失败时的不可变异常上下文。 */
public final class ExecutionErrorContext {
    private final ExecutionContext execution;
    private final Throwable error;
    private final long elapsedNanos;

    public ExecutionErrorContext(ExecutionContext execution, Throwable error, long elapsedNanos) {
        if (execution == null) throw new IllegalArgumentException("execution must not be null");
        if (error == null) throw new IllegalArgumentException("error must not be null");
        if (elapsedNanos < 0) throw new IllegalArgumentException("elapsedNanos must not be negative");
        this.execution = execution;
        this.error = error;
        this.elapsedNanos = elapsedNanos;
    }

    public ExecutionContext getExecution() { return execution; }
    public Throwable getError() { return error; }
    public long getElapsedNanos() { return elapsedNanos; }
}
