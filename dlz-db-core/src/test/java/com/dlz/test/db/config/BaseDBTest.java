package com.dlz.test.db.config;

import com.dlz.db.holder.DBHolder;
import org.junit.jupiter.api.BeforeAll;

/**
 * 测试基类 - 自动初始化 MockDbProvider
 * 所有需要数据库功能的测试类继承此类即可
 */
public abstract class BaseDBTest {

    @BeforeAll
    static void initMockDb() {
        // 设置 MockDbProvider
        MockDbProvider mockProvider = new MockDbProvider();
        DBHolder.setDbProvider(mockProvider);
    }
}
