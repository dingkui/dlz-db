package com.dlz.db.internal.anno;

import java.sql.Connection;

@FunctionalInterface
public interface SqlAction<T> {
    T run(Connection conn) throws Exception;
}