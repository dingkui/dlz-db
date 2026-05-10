package com.dlz.test.db.cases;

import com.dlz.db.core.ADbProvider;
import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.holder.DBHolder;
import com.dlz.test.db.config.DlzDbConfigs;
import com.dlz.test.db.config.SpringDbTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class SpringDbInitTest extends SpringDbTestBase {
    @Autowired
    private DlzDbConfigs dlzDbConfigs;
    @Autowired
    private ADbProvider dbProvider;
    @Autowired
    private ISqlExecutor sqlExecutor;

    @Test
    public void dlzDbConfigsInitialized() {
        assertNotNull(dlzDbConfigs);
        assertNotNull(dbProvider);
        assertNotNull(sqlExecutor);
        assertSame(dbProvider, DBHolder.dbProvider);
        assertSame(sqlExecutor, DBHolder.getSqlExecutor());
    }
}
