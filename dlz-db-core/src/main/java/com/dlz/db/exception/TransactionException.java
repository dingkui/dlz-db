package com.dlz.db.exception;

public class TransactionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public TransactionException(String message, Throwable cause) { super(message, cause); }
}
