package com.dlz.test.db.cases.modal.batch;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.BatchStatus;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Orders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 批量操作专题测试
 * 覆盖批量插入/更新
 */
public class DBBatchTableInsertTest extends BaseDBTest {

    @BeforeEach
    public void setUp() {
        DB.jdbc.execute("DELETE FROM Orders");
       // DB.Jdbc.execute("CREATE TABLE Orders (id INTEGER PRIMARY KEY, user_id TEXT, amount INTEGER)");
    }

    @AfterEach
    public void tearDown() {
        DB.jdbc.execute("DELETE FROM Orders");
    }

    @Test
    public void batch_insert() {
        Orders o1 = new Orders();
        o1.setUserId("b1");
        o1.setAmount(10);
        Orders o2 = new Orders();
        o2.setUserId("b2");
        o2.setAmount(20);
        DB.batch.insert("Orders", Arrays.asList(new JSONMap(o1), new JSONMap(o2)), 100);
        assertEquals(2, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM Orders").count());
        DB.batch.insert("Orders", Arrays.asList(new JSONMap(o1), new JSONMap(o2)));
        assertEquals(4, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM Orders").count());
    }

    @Test
    public void batch_insert_empty() {
        List<JSONMap> emptyList = Collections.emptyList();
        assertEquals(DB.batch.insert("Orders", emptyList, 100).status(), BatchStatus.SUCCESS);
        assertEquals(DB.batch.insert("Orders", emptyList).status(), BatchStatus.SUCCESS);
        assertThrows(NullPointerException.class, () -> {
            assertEquals(DB.batch.insert("Test_User",null).status(), BatchStatus.SUCCESS);
        });
        assertEquals(0, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM Orders").count());
    }
}
