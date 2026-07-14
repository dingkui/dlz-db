package com.dlz.db.exception;

public class NonUniqueResultException extends DbException {
    private static final long serialVersionUID = 1L;

    public NonUniqueResultException(String message) {
        super(message, 1004);
    }
}
