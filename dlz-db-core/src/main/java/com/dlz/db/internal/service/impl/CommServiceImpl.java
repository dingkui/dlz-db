package com.dlz.db.internal.service.impl;

import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.internal.service.ICommService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommServiceImpl implements ICommService {
    private ISqlExecutor sqlExecutor;

    @Override
    public ISqlExecutor getSqlExecutor() {
        return sqlExecutor;
    }

}
