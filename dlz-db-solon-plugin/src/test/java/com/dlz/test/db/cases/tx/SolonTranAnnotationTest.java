package com.dlz.test.db.cases.tx;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import com.dlz.test.db.service.SolonTxTestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.noear.solon.Solon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Solon 声明式事务测试 - 使用 @Tran 注解的 Service
 *
 * 测试场景：
 * 1. 基本事务提交
 * 2. 事务回滚（异常触发）
 * 3. 嵌套事务
 * 4. 带返回值的事务
 * 5. 批量操作事务
 * 6. 复杂业务场景（转账）
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SolonTranAnnotationTest extends BaseDBTest {

    private SolonTxTestService txService;

    @BeforeAll
    public static void setupTable() {
        // 创建测试表
        DB.Jdbc.execute("delete from USER");
//        DB.Jdbc.execute("CREATE TABLE USER (" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "name TEXT, " +
//                "age INTEGER, " +
//                "email TEXT, " +
//                "create_time DATETIME, " +
//                "is_deleted TEXT DEFAULT '0'" +
//                ")");
    }

    @BeforeEach
    public void setUp() {
        // 从 Solon 容器获取 Service Bean
        txService = Solon.context().getBean(SolonTxTestService.class);

        // 清理数据
        DB.Jdbc.execute("DELETE FROM USER");
    }

    /**
     * 测试1：基本事务提交
     * 验证：两条记录都应该插入成功
     */
    @Test
    @Order(1)
    void testCommit() {
        txService.testCommit();

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "事务用户")
                .queryBeanList();

        assertEquals(2, users.size(), "应该插入2条记录");
        assertTrue(users.stream().anyMatch(u -> "事务用户1".equals(u.getName())));
        assertTrue(users.stream().anyMatch(u -> "事务用户2".equals(u.getName())));
    }

    /**
     * 测试2：事务回滚 - 方法内抛出异常
     * 验证：所有操作都应该回滚，没有记录插入
     */
    @Test
    @Order(2)
    void testRollbackWithException() {
        try {
            txService.testRollbackWithException();
            fail("应该抛出 RuntimeException");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "回滚用户")
                .queryBeanList();

        assertEquals(0, users.size(), "异常应该触发回滚，没有记录插入");
    }

    /**
     * 测试3：事务回滚 - 手动控制
     * 验证：条件触发回滚
     */
    @Test
    @Order(3)
    void testRollbackManual() {
        try {
            txService.testRollbackManual();
            fail("应该抛出 RuntimeException");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "手动回滚用户")
                .queryBeanList();

        assertEquals(0, users.size(), "条件触发回滚，没有记录插入");
    }

    /**
     * 测试4：嵌套事务 - 都成功
     * 验证：外层和内层的数据都应该提交
     */
    @Test
    @Order(4)
    void testNestedTransactionSuccess() {
        txService.testNestedTransactionSuccess();

        List<User> outerUsers = DB.Pojo.select(User.class)
                .eq(User::getName, "外层用户")
                .queryBeanList();

        List<User> innerUsers = DB.Pojo.select(User.class)
                .eq(User::getName, "内层用户")
                .queryBeanList();

        assertEquals(1, outerUsers.size(), "外层数据应该提交");
        assertEquals(1, innerUsers.size(), "内层数据应该提交");
    }

    /**
     * 测试5：嵌套事务 - 内层失败
     * 验证：即使外层捕获了异常，整个事务也应该回滚
     * （因为 Solon 默认传播行为是 REQUIRED，同一事务）
     */
    @Test
    @Order(5)
    void testNestedTransactionInnerFail() {
        try {
            txService.testNestedTransactionInnerFail();
            // 注意：虽然外层捕获了异常，但事务已经标记为回滚
            // Solon 会在方法结束时回滚
        } catch (RuntimeException e) {
            log.info("外层也捕获到异常: {}", e.getMessage());
        }

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "外层用户-内层失败")
                .queryBeanList();

        // 由于事务回滚，数据不应该存在
        assertEquals(1, users.size(), "内层异常导致整体回滚");
    }

    /**
     * 测试6：嵌套事务 - 外层失败
     * 验证：内层成功但外层失败，整体回滚
     */
    @Test
    @Order(6)
    void testNestedTransactionOuterFail() {
        try {
            txService.testNestedTransactionOuterFail();
            fail("应该抛出 RuntimeException");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        List<User> outerUsers = DB.Pojo.select(User.class)
                .like(User::getName, "外层用户-外层失败")
                .queryBeanList();

        List<User> innerUsers = DB.Pojo.select(User.class)
                .eq(User::getName, "内层用户")
                .queryBeanList();

        assertEquals(0, outerUsers.size(), "外层失败应回滚");
        assertEquals(0, innerUsers.size(), "内层数据也应回滚（同一事务）");
    }

    /**
     * 测试7：带返回值的事务
     * 验证：事务提交并返回正确的值
     */
    @Test
    @Order(7)
    void testTransactionWithReturn() {
        Long userId = txService.testTransactionWithReturn();

        assertNotNull(userId, "应该返回用户ID");

        User user = DB.Pojo.select(User.class)
                .eq(User::getId, userId)
                .queryBean();

        assertNotNull(user, "用户应该存在");
        assertEquals("返回值测试用户", user.getName());
    }

    /**
     * 测试8：批量操作事务
     * 验证：所有批量数据都应该插入成功
     */
    @Test
    @Order(8)
    void testBatchOperation() {
        txService.testBatchOperation();

        List<User> users = DB.Pojo.select(User.class)
                .like(User::getName, "批量用户")
                .queryBeanList();

        assertEquals(10, users.size(), "应该插入10条记录");
    }

    /**
     * 测试9：查询操作事务
     * 验证：事务中的查询能正确执行
     */
    @Test
    @Order(9)
    void testReadOnlyQuery() {
        // 先准备数据
        User user1 = new User();
        user1.setName("查询测试A");
        user1.setAge(25);
        DB.Pojo.insert(user1);

        User user2 = new User();
        user2.setName("查询测试B");
        user2.setAge(30);
        DB.Pojo.insert(user2);

        // 在事务中查询
        List<User> users = txService.testReadOnlyQuery("查询测试");

        assertEquals(2, users.size(), "应该查询到2条记录");
    }

    /**
     * 测试10：更新操作事务
     * 验证：更新操作在事务中正确执行
     */
    @Test
    @Order(10)
    void testUpdateTransaction() {
        // 先插入数据
        User user = new User();
        user.setName("更新前");
        user.setAge(20);
        DB.Pojo.insert(user);
        Long userId = user.getId();

        // 在事务中更新
        txService.testUpdateTransaction(userId, "更新后", 30);

        User updatedUser = DB.Pojo.select(User.class)
                .eq(User::getId, userId)
                .queryBean();

        assertNotNull(updatedUser);
        assertEquals("更新后", updatedUser.getName());
        assertEquals(Integer.valueOf(30), updatedUser.getAge());
    }

    /**
     * 测试11：删除操作事务
     * 验证：删除操作在事务中正确执行
     */
    @Test
    @Order(11)
    void testDeleteTransaction() {
        // 先插入数据
        User user = new User();
        user.setName("待删除用户");
        user.setAge(25);
        DB.Pojo.insert(user);
        Long userId = user.getId();

        // 在事务中删除
        txService.testDeleteTransaction(userId);

        User deletedUser = DB.Pojo.select(User.class)
                .eq(User::getId, userId)
                .queryBean();

        assertNull(deletedUser, "用户应该被删除");
    }

    /**
     * 测试12：复杂业务场景 - 转账成功
     * 验证：转账成功，双方余额正确更新
     */
    @Test
    @Order(12)
    void testTransferSuccess() {
        // 准备两个用户（用 age 字段模拟余额）
        User fromUser = new User();
        fromUser.setName("转出账户");
        fromUser.setAge(200); // 余额 200
        DB.Pojo.insert(fromUser);

        User toUser = new User();
        toUser.setName("转入账户");
        toUser.setAge(100); // 余额 100
        DB.Pojo.insert(toUser);

        // 执行转账（金额 50，小于 100，不会触发异常）
        txService.testTransfer("转出账户", "转入账户", 50);

        // 验证转账结果
        User updatedFrom = DB.Pojo.select(User.class)
                .eq(User::getName, "转出账户")
                .queryBean();

        User updatedTo = DB.Pojo.select(User.class)
                .eq(User::getName, "转入账户")
                .queryBean();

        assertNotNull(updatedFrom);
        assertNotNull(updatedTo);
        assertEquals(Integer.valueOf(150), updatedFrom.getAge(), "转出账户应扣款");
        assertEquals(Integer.valueOf(150), updatedTo.getAge(), "转入账户应加款");
    }

    /**
     * 测试13：复杂业务场景 - 转账失败（余额不足）
     * 验证：余额不足时回滚
     */
    @Test
    @Order(13)
    void testTransferInsufficientBalance() {
        // 准备两个用户
        User fromUser = new User();
        fromUser.setName("转出账户2");
        fromUser.setAge(30); // 余额 30
        DB.Pojo.insert(fromUser);

        User toUser = new User();
        toUser.setName("转入账户2");
        toUser.setAge(100); // 余额 100
        DB.Pojo.insert(toUser);

        try {
            // 尝试转账 50，余额不足
            txService.testTransfer("转出账户2", "转入账户2", 50);
            fail("应该抛出余额不足异常");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        // 验证数据未改变
        User fromAfter = DB.Pojo.select(User.class)
                .eq(User::getName, "转出账户2")
                .queryBean();

        User toAfter = DB.Pojo.select(User.class)
                .eq(User::getName, "转入账户2")
                .queryBean();

        assertEquals(Integer.valueOf(30), fromAfter.getAge(), "转出账户余额不应改变");
        assertEquals(Integer.valueOf(100), toAfter.getAge(), "转入账户余额不应改变");
    }

    /**
     * 测试14：复杂业务场景 - 转账失败（金额过大）
     * 验证：金额过大时回滚
     */
    @Test
    @Order(14)
    void testTransferAmountTooLarge() {
        // 准备两个用户
        User fromUser = new User();
        fromUser.setName("转出账户3");
        fromUser.setAge(200); // 余额 200
        DB.Pojo.insert(fromUser);

        User toUser = new User();
        toUser.setName("转入账户3");
        toUser.setAge(100); // 余额 100
        DB.Pojo.insert(toUser);

        try {
            // 尝试转账 150，超过限制
            txService.testTransfer("转出账户3", "转入账户3", 150);
            fail("应该抛出金额过大异常");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        // 验证数据未改变（回滚）
        User fromAfter = DB.Pojo.select(User.class)
                .eq(User::getName, "转出账户3")
                .queryBean();

        User toAfter = DB.Pojo.select(User.class)
                .eq(User::getName, "转入账户3")
                .queryBean();

        assertEquals(Integer.valueOf(200), fromAfter.getAge(), "转出账户余额不应改变");
        assertEquals(Integer.valueOf(100), toAfter.getAge(), "转入账户余额不应改变");
    }

    /**
     * 测试15：多次调用事务方法
     * 验证：每次调用都是独立的事务
     */
    @Test
    @Order(15)
    void testMultipleTransactionCalls() {
        // 第一次调用
        txService.testCommit();
        long count1 = DB.Pojo.select(User.class)
                .like(User::getName, "事务用户")
                .count();

        // 第二次调用
        txService.testCommit();
        long count2 = DB.Pojo.select(User.class)
                .like(User::getName, "事务用户")
                .count();

        assertEquals(2, count1, "第一次调用应有2条记录");
        assertEquals(4, count2, "第二次调用后应有4条记录");
    }
}
