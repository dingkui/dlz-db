package com.dlz.test.db.solon;

import com.dlz.db.core.ISqlExecutor;
import com.dlz.db.holder.DBHolder;
import com.dlz.db.modal.DB;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.noear.solon.Solon;

/**
 * Solon 插件冒烟测试：验证插件能正确启动并完成基本 CRUD + 事务。
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SolonSmokeTest {

    @BeforeAll
    static void start() {
        // 启动 Solon 应用（会自动加载 META-INF/solon/dlz-db-solon-plugin.properties）
        Solon.start(SolonDbTestApp.class, new String[]{});
    }

    @AfterAll
    static void stop() {
        Solon.stopBlock(true, 0);
    }

    @Test
    @Order(1)
    void pluginStarted() {
        Assertions.assertNotNull(DBHolder.dbProvider, "dbProvider 未注册");
        Assertions.assertNotNull(DBHolder.sqlExecutor, "sqlExecutor 未注册");
        log.info("provider={}, sqlExecutor={}",
                DBHolder.dbProvider.getClass().getSimpleName(),
                DBHolder.sqlExecutor.getClass().getSimpleName());
    }

    @Test
    @Order(2)
    void crud() {
        ISqlExecutor sqlExecutor = DBHolder.sqlExecutor;
        sqlExecutor.execute("drop table if exists smoke");
        sqlExecutor.execute("create table smoke(id integer primary key autoincrement, name text)");
        sqlExecutor.update("insert into smoke(name) values(?)", "tom");
        sqlExecutor.update("insert into smoke(name) values(?)", "jerry");

        int count = sqlExecutor.getList("select count(*) c from smoke").get(0)
                .getInt("c");
        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(3)
    void transactionCommit() {
        DB.Tx.run(() -> {
            DBHolder.sqlExecutor.update("insert into smoke(name) values(?)", "alice");
        });
        int count = DBHolder.sqlExecutor.getList("select count(*) c from smoke where name=?", "alice").get(0)
                .getInt("c");
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(4)
    void transactionRollback() {
        try {
            DB.Tx.run(() -> {
                DBHolder.sqlExecutor.update("insert into smoke(name) values(?)", "bob");
                throw new RuntimeException("force rollback");
            });
            Assertions.fail("应该抛异常");
        } catch (Exception ignore) {
            // 预期
        }
        int count = DBHolder.sqlExecutor.getList("select count(*) c from smoke where name=?", "bob").get(0)
                .getInt("c");
        Assertions.assertEquals(0, count, "异常应触发回滚");
    }
}
