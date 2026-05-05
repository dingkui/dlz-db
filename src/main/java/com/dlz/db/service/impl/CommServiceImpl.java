package com.dlz.db.service.impl;

import com.dlz.db.core.SqlExecutor;
import com.dlz.db.service.ICommService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommServiceImpl implements ICommService {
    private SqlExecutor sqlExecutor;

    @Override
    public SqlExecutor getSqlExecutor() {
        return sqlExecutor;
    }

}
