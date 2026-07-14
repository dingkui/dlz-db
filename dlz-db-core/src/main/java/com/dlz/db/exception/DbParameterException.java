package com.dlz.db.exception;

public class DbParameterException extends DbException {
    private static final long serialVersionUID = 1L;

    public DbParameterException(String message) {
        super(message, 1002);
    }

    public DbParameterException(String message, Throwable cause) {
        super(message, 1002, cause);
    }
}
