package com.dlz.db.service.impl;

import com.dlz.db.dao.IDlzDao;
import com.dlz.db.service.ICommService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommServiceImpl implements ICommService {
    private IDlzDao dao;

    @Override
    public IDlzDao getDao() {
        return dao;
    }

}
