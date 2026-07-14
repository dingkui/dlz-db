package com.dlz.db.exception;

public class UnsafeMutationException extends DbException {
    private static final long serialVersionUID = 1L;

    public UnsafeMutationException(String message) {
        super(message, 1002);
    }
}
