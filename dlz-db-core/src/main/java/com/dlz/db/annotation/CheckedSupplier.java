package com.dlz.db.annotation;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Exception;
}
