package com.dlz.test.db.cases.modal.batch;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.BatchStatus;
import com.dlz.db.modal.wrapper.PojoInsert;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.AutoIdEntity;
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
public class DBBatchPojoInsertTest extends BaseDBTest {

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
        assertNull(o1.getId());
        assertNull(o2.getId());
        DB.batch.insert(Arrays.asList(o1, o2), 100);
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
        DB.batch.insert(Arrays.asList(o1, o2, o3), 2);
        assertEquals(3, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM Orders").count());
    }

    @Test
    public void batch_insert_empty() {
        DB.batch.insert(Arrays.asList(), 100);
        assertEquals(0, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM Orders").count());
    }

    @Test
    public void batch_insert_single() {
        Orders o1 = new Orders();
        o1.setUserId("single");
        o1.setAmount(100);
        assertNull(o1.getId());
        DB.batch.insert(Arrays.asList(o1));
        assertNotNull(o1.getId());
        assertEquals(1, DB.jdbc.selectWrapper("SELECT COUNT(*) FROM Orders").count());

        //测试 insert - null
        assertThrows(DbParameterException.class, () -> {
            DB.batch.insert(null);
        });

        //测试 insert - 空列表返回 false
        List<Orders> users = Collections.emptyList();
        assertEquals(DB.batch.insert(users).status(), BatchStatus.SUCCESS);
        assertFalse(new PojoInsert(Orders.class).batch(users).isSuccess());
    }

    @Test
    public void batchAssignIdBackfillTest() {
        Orders o1 = new Orders();
        o1.setUserId("batch_u1");
        o1.setAmount(10);

        Orders o2 = new Orders();
        o2.setUserId("batch_u2");
        o2.setAmount(20);

        assertNull(o1.getId());
        assertNull(o2.getId());

        DB.batch.insert(Arrays.asList(o1, o2), 100);

        assertNotNull("batch 后 bean 应被回填 ASSIGN_ID", o1.getId());
        assertNotNull("batch 后 bean 应被回填 ASSIGN_ID", o2.getId());
        assertNotEquals("两个 bean 的 ASSIGN_ID 应不同", o1.getId(), o2.getId());
    }

    @Test
    public void batchAutoNotBackfillTest() {
        AutoIdEntity m1 = new AutoIdEntity();
        m1.setName("batch_auto1");

        AutoIdEntity m2 = new AutoIdEntity();
        m2.setName("batch_auto2");

        DB.batch.insert(Arrays.asList(m1, m2), 100);

        assertNull("AUTO 类型 batch 后不应回填主键（驱动限制）", m1.getId());
    }
}
