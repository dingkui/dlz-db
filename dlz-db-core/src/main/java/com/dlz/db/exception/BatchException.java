package com.dlz.db.exception;

public class BatchException extends DbException {
    private static final long serialVersionUID = 1L;

    public BatchException(String message, Throwable cause) {
        super(message, 1003, cause);
    }
}
