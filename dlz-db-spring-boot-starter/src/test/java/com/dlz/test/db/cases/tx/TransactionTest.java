package com.dlz.test.db.cases.tx;

import com.dlz.db.modal.DB;
import com.dlz.test.config.BaseDBTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 事务专题测试
 * 覆盖提交/回滚/嵌套/返回值
 */
public class TransactionTest extends BaseDBTest {

    @BeforeEach
    public void setUp() {
        DB.jdbc.execute("DELETE FROM user");
        // DB.Jdbc.execute("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, status TEXT, deleted INTEGER DEFAULT 0)");
        DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "alice", 25, "1", 0);
    }

    @AfterEach
    public void tearDown() {
        DB.jdbc.execute("DELETE FROM user");
    }

    @Test
    public void tx_commit() {
        DB.tx.run(() -> DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "tx_c", 10, "1", 0));
        assertEquals(1, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM user WHERE name=?", "tx_c").count());
    }

    @Test
    public void tx_rollback() {
        try {
            DB.tx.run(() -> {
                DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "tx_r", 10, "1", 0);
                throw new RuntimeException("force rollback");
            });
            fail("should throw");
        } catch (Exception ignore) {}
        assertEquals(0, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM user WHERE name=?", "tx_r").count());
    }

    @Test
    public void tx_returnValue() {
        Integer result = DB.tx.run(() -> {
            DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "tx_ret", 10, "1", 0);
            return 42;
        });
        assertEquals(Integer.valueOf(42), result);
        assertEquals(1, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM user WHERE name=?", "tx_ret").count());
    }

    @Test
    public void tx_nested() {
        DB.tx.run(() -> {
            DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "outer", 10, "1", 0);
            DB.tx.run(() -> DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "inner", 20, "1", 0));
        });
        assertEquals(1, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM user WHERE name=?", "outer").count());
        assertEquals(1, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM user WHERE name=?", "inner").count());
    }

    @Test
    public void tx_nested_rollback_inner() {
        try {
            DB.tx.run(() -> {
                DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "outer_ok", 10, "1", 0);
                DB.tx.run(() -> {
                    DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "inner_fail", 20, "1", 0);
                    throw new RuntimeException("inner rollback");
                });
            });
            fail("should throw");
        } catch (Exception ignore) {}
        assertEquals(0, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM user WHERE name=?", "outer_ok").count());
        assertEquals(0, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM user WHERE name=?", "inner_fail").count());
    }

    @Test
    public void tx_multiple_operations() {
        DB.tx.run(() -> {
            DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "tx_m1", 10, "1", 0);
            DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "tx_m2", 20, "1", 0);
            DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", "tx_m3", 30, "1", 0);
        });
        assertEquals(3, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM user WHERE name LIKE 'tx_%'").count());
    }
}
