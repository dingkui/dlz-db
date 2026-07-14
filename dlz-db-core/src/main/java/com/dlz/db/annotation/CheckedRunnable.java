package com.dlz.db.annotation;

@FunctionalInterface
public interface CheckedRunnable {
    void run() throws Exception;
}
