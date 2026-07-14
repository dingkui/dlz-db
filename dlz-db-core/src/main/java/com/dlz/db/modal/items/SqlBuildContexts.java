package com.dlz.db.modal.items;

import com.dlz.db.exception.DbParameterException;

public final class SqlBuildContexts {
    private SqlBuildContexts() { }
    public static SqlBuildContext require(SqlBuildContext context) {
        if (context == null) throw new DbParameterException("SqlBuildContext must not be null");
        return context;
    }
}
