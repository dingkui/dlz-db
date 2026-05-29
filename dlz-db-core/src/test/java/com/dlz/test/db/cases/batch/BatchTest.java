package com.dlz.test.db.cases.batch;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.Orders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * 批量操作专题测试
 * 覆盖批量插入/更新
 */
public class BatchTest extends BaseDBTest {

    @BeforeEach
    public void setUp() {
        DB.Jdbc.execute("delete from Orders");
       // DB.Jdbc.execute("CREATE TABLE Orders (id INTEGER PRIMARY KEY, user_id TEXT, amount INTEGER)");
    }

    @AfterEach
    public void tearDown() {
        DB.Jdbc.execute("delete from Orders");
    }

    @Test
    public void batch_insert() {
        Orders o1 = new Orders();
        o1.setUserId("b1");
        o1.setAmount(10);
        Orders o2 = new Orders();
        o2.setUserId("b2");
        o2.setAmount(20);
        assertNull(o1.getId());
        assertNull(o2.getId());
        DB.Batch.insert(Arrays.asList(o1, o2), 100);
        assertNotNull(o1.getId());
        assertNotNull(o2.getId());
        assertNotEquals(o1.getId(), o2.getId());
    }

    @Test
    public void batch_insert_batchSize() {
        Orders o1 = new Orders();
        o1.setUserId("b1");
        o1.setAmount(10);
        Orders o2 = new Orders();
        o2.setUserId("b2");
        o2.setAmount(20);
        Orders o3 = new Orders();
        o3.setUserId("b3");
        o3.setAmount(30);
        DB.Batch.insert(Arrays.asList(o1, o2, o3), 2);
        assertEquals(3, DB.Jdbc.select("SELECT COUNT(*) FROM Orders").count());
    }

    @Test
    public void batch_insert_empty() {
        DB.Batch.insert(Arrays.asList(), 100);
        assertEquals(0, DB.Jdbc.select("SELECT COUNT(*) FROM Orders").count());
    }

    @Test
    public void batch_insert_single() {
        Orders o1 = new Orders();
        o1.setUserId("single");
        o1.setAmount(100);
        assertNull(o1.getId());
        DB.Batch.insert(Arrays.asList(o1), 100);
        assertNotNull(o1.getId());
        assertEquals(1, DB.Jdbc.select("SELECT COUNT(*) FROM Orders").count());
    }
}
