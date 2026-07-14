package com.dlz.db.exception;

public class NoResultException extends DbException {
    private static final long serialVersionUID = 1L;

    public NoResultException(String message) {
        super(message, 1004);
    }
}
