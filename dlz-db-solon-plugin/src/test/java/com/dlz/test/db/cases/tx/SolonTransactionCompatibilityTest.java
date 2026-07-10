package com.dlz.test.db.cases.tx;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import com.dlz.test.db.service.SolonCompatibilityService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.noear.solon.Solon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Solon 事务兼容性测试
 *
 * <p><b>核心目的</b>：验证 DLZ-DB 能够与 Solon 原有的 @Transaction 注解完美兼容</p>
 *
 * <p>验证点：</p>
 * <ul>
 *   <li>Solon 的 @Transaction 事务中可以使用 DLZ-DB 的 API ✅</li>
 *   <li>DLZ-DB 的事务遵循 Solon 的事务传播规则 ✅</li>
 *   <li>两者可以混合使用，互不干扰 ✅</li>
 *   <li>不需要用户更换现有的事务写法 ✅</li>
 * </ul>
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SolonTransactionCompatibilityTest extends BaseDBTest {

    private SolonCompatibilityService compatibilityService;

    @BeforeAll
    public static void setupTable() {
        // 创建测试表
        DB.Jdbc.execute("DELETE FROM USER");
    }

    @BeforeEach
    public void setUp() {
        // 从 Solon 容器获取 Service Bean
        compatibilityService = Solon.context().getBean(SolonCompatibilityService.class);

        // 清理数据
        DB.Jdbc.execute("DELETE FROM USER");
    }

    // ==================== Solon @Transaction + DLZ-DB API ====================

    /**
     * 测试1：Solon @Transaction 注解 + DLZ-DB API
     * 验证：Solon 的声明式事务中可以正常使用 DLZ-DB
     */
    @Test
    @Order(1)
    void testSolonTranWithDlzDb() {
        compatibilityService.solonTranWithDlzDb();

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "Solon事务用户")
                .queryBeanList();

        assertEquals(2, users.size(), "应该插入2条记录");
        log.info("✅ Solon @Transaction 中可以正常使用 DLZ-DB API");
    }

    /**
     * 测试2：Solon @Transaction 注解 + 异常回滚
     * 验证：Solon 事务能正确捕获 DLZ-DB 操作并回滚
     */
    @Test
    @Order(2)
    void testSolonTranRollback() {
        long size = DB.Pojo.select(User.class)
                .like(User::getName, "Solon回滚用户")
                .count();
        try {
            compatibilityService.solonTranRollback();
            fail("应该抛出 RuntimeException");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        long size2 = DB.Pojo.select(User.class)
                .like(User::getName, "Solon回滚用户")
                .count();

        assertEquals(0, size2-size, "异常应该触发回滚，没有记录插入");
        log.info("✅ Solon @Transaction 能正确回滚 DLZ-DB 操作");
    }

    /**
     * 测试3：Solon @Transaction + DLZ-DB 查询
     */
    @Test
    @Order(3)
    void testSolonTranQuery() {
        // 先准备数据
        User user1 = new User();
        user1.setName("查询测试A");
        user1.setAge(25);
        DB.Pojo.add(user1);

        User user2 = new User();
        user2.setName("查询测试B");
        user2.setAge(30);
        DB.Pojo.add(user2);

        // 在 Solon 事务中查询
        List<User> users = compatibilityService.solonTranQuery("查询测试");

        assertEquals(2, users.size(), "应该查询到2条记录");
        log.info("✅ Solon @Transaction 中可以执行 DLZ-DB 查询");
    }

    /**
     * 测试4：Solon @Transaction + DLZ-DB 更新
     */
    @Test
    @Order(4)
    void testSolonTranUpdate() {
        // 先插入数据
        User user = new User();
        user.setName("更新前");
        user.setAge(20);
        DB.Pojo.add(user);
        Long userId = user.getId();

        // 在 Solon 事务中更新
        compatibilityService.solonTranUpdate(userId, "更新后");

        User updatedUser = DB.Pojo.select(User.class)
                .eq(User::getId, userId)
                .queryBean();

        assertNotNull(updatedUser);
        assertEquals("更新后", updatedUser.getName());
        log.info("✅ Solon @Transaction 中可以执行 DLZ-DB 更新");
    }

    /**
     * 测试5：Solon @Transaction + DLZ-DB 删除
     */
    @Test
    @Order(5)
    void testSolonTranDelete() {
        // 先插入数据
        User user = new User();
        user.setName("待删除用户");
        user.setAge(25);
        DB.Pojo.add(user);
        Long userId = user.getId();

        // 在 Solon 事务中删除
        compatibilityService.solonTranDelete(userId);

        User deletedUser = DB.Pojo.select(User.class)
                .eq(User::getId, userId)
                .queryBean();

        assertNull(deletedUser, "用户应该被删除");
        log.info("✅ Solon @Transaction 中可以执行 DLZ-DB 删除");
    }

    // ==================== Solon @Transaction + DLZ-DB.Tx.run() 混合使用 ====================

    /**
     * 测试6：Solon @Transaction 中包含 DLZ-DB.Tx.run()
     * 验证：DLZ-DB 的编程式事务能加入 Solon 的外层事务
     */
    @Test
    @Order(6)
    void testSolonTranWithDlzNestedTx() {
        compatibilityService.solonTranWithDlzNestedTx();

        List<User> outerUsers = DB.Pojo.select(User.class)
                .eq(User::getName, "Solon外层用户")
                .queryBeanList();

        List<User> innerUsers = DB.Pojo.select(User.class)
                .eq(User::getName, "DLZ内层用户")
                .queryBeanList();

        assertEquals(1, outerUsers.size(), "外层数据应该提交");
        assertEquals(1, innerUsers.size(), "内层数据应该提交（加入同一事务）");
        log.info("✅ DLZ-DB.Tx.run() 能加入 Solon @Transaction");
    }

    /**
     * 测试7：Solon @Transaction 失败时，DLZ-DB 操作也回滚
     * 验证：Solon 事务回滚会影响其中所有的 DLZ-DB 操作
     */
    @Test
    @Order(7)
    void testSolonTranFailRollbackDlz() {
        try {
            compatibilityService.solonTranFailRollbackDlz();
            fail("应该抛出 RuntimeException");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        List<User> solonUsers = DB.Pojo.select(User.class)
                .like(User::getName, "Solon用户-会回滚")
                .queryBeanList();

        List<User> dlzUsers = DB.Pojo.select(User.class)
                .like(User::getName, "DLZ用户-也会回滚")
                .queryBeanList();

        assertEquals(0, solonUsers.size(), "Solon 操作应回滚");
        assertEquals(0, dlzUsers.size(), "DLZ-DB 操作也应回滚（同一事务）");
        log.info("✅ Solon @Transaction 回滚会影响其中的 DLZ-DB 操作");
    }

    // ==================== DLZ-DB.Tx.run() + Solon Service 方法调用 ====================

    /**
     * 测试8：DLZ-DB 事务中调用 Solon Service 方法
     * 验证：DLZ-DB 的事务能正确传播到被调用的 Solon 方法
     */
    @Test
    @Order(8)
    void testDlzTxCallSolonMethod() {
        compatibilityService.dlzTxCallSolonMethod();

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "%DLZ调用%")
                .queryBeanList();

        assertEquals(2, users.size(), "两个用户都应该插入（同一事务）");
        log.info("✅ DLZ-DB 事务中可以调用普通 Solon Service 方法");
    }

    /**
     * 测试9：DLZ-DB 事务 + Solon @Transaction 方法调用
     * 验证：从 DLZ 事务调用带 @Transaction 的方法
     */
    @Test
    @Order(9)
    void testDlzTxCallSolonTranMethod() {
        compatibilityService.dlzTxCallSolonTranMethod();

        List<User> dlzUsers = DB.Pojo.select(User.class)
                .eq(User::getName, "DLZ外层用户")
                .queryBeanList();

        List<User> solonUsers = DB.Pojo.select(User.class)
                .eq(User::getName, "@Transaction用户")
                .queryBeanList();

        assertEquals(1, dlzUsers.size(), "DLZ 外层用户应该插入");
        assertEquals(1, solonUsers.size(), "@Transaction 方法中的用户也应该插入");
        log.info("✅ DLZ-DB 事务中可以调用带 @Transaction 的方法");
    }

    // ==================== 复杂业务场景 ====================

    /**
     * 测试10：转账业务 - Solon @Transaction
     */
    @Test
    @Order(10)
    void testTransferBySolonTranSuccess() {
        // 准备两个用户（用 age 字段模拟余额）
        User fromUser = new User();
        fromUser.setName("转出账户-Solon");
        fromUser.setAge(200); // 余额 200
        DB.Pojo.add(fromUser);

        User toUser = new User();
        toUser.setName("转入账户-Solon");
        toUser.setAge(100); // 余额 100
        DB.Pojo.add(toUser);

        // 执行转账
        compatibilityService.transferBySolonTran("转出账户-Solon", "转入账户-Solon", 50);

        // 验证转账结果
        User updatedFrom = DB.Pojo.select(User.class)
                .eq(User::getName, "转出账户-Solon")
                .queryBean();

        User updatedTo = DB.Pojo.select(User.class)
                .eq(User::getName, "转入账户-Solon")
                .queryBean();

        assertNotNull(updatedFrom);
        assertNotNull(updatedTo);
        assertEquals(Integer.valueOf(150), updatedFrom.getAge(), "转出账户应扣款");
        assertEquals(Integer.valueOf(150), updatedTo.getAge(), "转入账户应加款");
        log.info("✅ Solon @Transaction 中的转账业务正常工作");
    }

    /**
     * 测试11：转账业务 - DLZ-DB.Tx.run()
     */
    @Test
    @Order(11)
    void testTransferByDlzTxSuccess() {
        // 准备两个用户
        User fromUser = new User();
        fromUser.setName("转出账户-DLZ");
        fromUser.setAge(200);
        DB.Pojo.add(fromUser);

        User toUser = new User();
        toUser.setName("转入账户-DLZ");
        toUser.setAge(100);
        DB.Pojo.add(toUser);

        // 执行转账
        compatibilityService.transferByDlzTx("转出账户-DLZ", "转入账户-DLZ", 50);

        // 验证转账结果
        User updatedFrom = DB.Pojo.select(User.class)
                .eq(User::getName, "转出账户-DLZ")
                .queryBean();

        User updatedTo = DB.Pojo.select(User.class)
                .eq(User::getName, "转入账户-DLZ")
                .queryBean();

        assertNotNull(updatedFrom);
        assertNotNull(updatedTo);
        assertEquals(Integer.valueOf(150), updatedFrom.getAge(), "转出账户应扣款");
        assertEquals(Integer.valueOf(150), updatedTo.getAge(), "转入账户应加款");
        log.info("✅ DLZ-DB.Tx.run() 中的转账业务正常工作");
    }

    /**
     * 测试12：转账失败回滚 - Solon @Transaction
     */
    @Test
    @Order(12)
    void testTransferBySolonTranInsufficientBalance() {
        // 准备两个用户
        User fromUser = new User();
        fromUser.setName("转出账户-不足");
        fromUser.setAge(30); // 余额 30
        DB.Pojo.add(fromUser);

        User toUser = new User();
        toUser.setName("转入账户-不足");
        toUser.setAge(100);
        DB.Pojo.add(toUser);

        try {
            // 尝试转账 50，余额不足
            compatibilityService.transferBySolonTran("转出账户-不足", "转入账户-不足", 50);
            fail("应该抛出余额不足异常");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        // 验证数据未改变
        User fromAfter = DB.Pojo.select(User.class)
                .eq(User::getName, "转出账户-不足")
                .queryBean();

        assertEquals(Integer.valueOf(30), fromAfter.getAge(), "余额不足时应回滚");
        log.info("✅ Solon @Transaction 中余额不足时正确回滚");
    }

    /**
     * 测试13：批量操作 - Solon @Transaction
     */
    @Test
    @Order(13)
    void testBatchInsertBySolonTran() {
        compatibilityService.batchInsertBySolonTran(10);

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "Solon批量用户")
                .queryBeanList();

        assertEquals(10, users.size(), "应该插入10条记录");
        log.info("✅ Solon @Transaction 中的批量操作正常工作");
    }

    /**
     * 测试14：批量操作 - DLZ-DB.Tx.run()
     */
    @Test
    @Order(14)
    void testBatchInsertByDlzTx() {
        compatibilityService.batchInsertByDlzTx(10);

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "DLZ批量用户")
                .queryBeanList();

        assertEquals(10, users.size(), "应该插入10条记录");
        log.info("✅ DLZ-DB.Tx.run() 中的批量操作正常工作");
    }

    /**
     * 测试15：两种方式混合使用
     * 验证：可以在同一个应用中同时使用 Solon @Transaction 和 DLZ-DB.Tx.run()
     */
    @Test
    @Order(15)
    void testMixedUsage() {
        // 使用 Solon @Transaction
        compatibilityService.solonTranWithDlzDb();

        // 使用 DLZ-DB.Tx.run()
        compatibilityService.batchInsertByDlzTx(5);

        // 验证两种方式的数据都存在
        List<User> solonUsers = DB.Pojo.select(User.class)
                .like(User::getName, "Solon事务用户")
                .queryBeanList();

        List<User> dlzUsers = DB.Pojo.select(User.class)
                .like(User::getName, "DLZ批量用户")
                .queryBeanList();

        assertEquals(2, solonUsers.size(), "Solon @Transaction 的数据应该存在");
        assertEquals(5, dlzUsers.size(), "DLZ-DB.Tx.run() 的数据应该存在");
        log.info("✅ 可以同时使用 Solon @Transaction 和 DLZ-DB.Tx.run()");
    }

    @AfterAll
    public static void cleanup() {
        log.info("========================================");
        log.info("Solon 事务兼容性测试完成！");
        log.info("结论：DLZ-DB 完美兼容 Solon @Transaction");
        log.info("- Solon @Transaction 中可以使用 DLZ-DB API");
        log.info("- DLZ-DB.Tx.run() 可以与 Solon @Transaction 混合使用");
        log.info("- 不需要用户更换现有的事务写法");
        log.info("========================================");
    }
}
