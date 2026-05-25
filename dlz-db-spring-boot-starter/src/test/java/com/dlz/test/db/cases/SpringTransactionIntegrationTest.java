package com.dlz.test.db.cases;

import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.service.TransactionTestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Spring 事务与 dlz-db 事务集成测试套件
 * <p>
 * 该测试套件验证 Spring 框架的声明式事务（@Transactional）与 dlz-db 编程式事务（DB.Tx.run()）
 * 在嵌套场景下的正确行为，特别关注异常回滚机制的正确性。
 * </p>
 * <p>
 * 测试覆盖以下场景：
 * <ul>
 *   <li>Spring 事务包含 dlz-db 事务（内层异常、外层异常、无异常）</li>
 *   <li>dlz-db 事务包含 Spring 事务（内层异常、外层异常、无异常）</li>
 *   <li>不同异常类型的回滚行为（RuntimeException、Checked Exception）</li>
 *   <li>异常后的连接状态验证</li>
 *   <li>测试用例之间的数据隔离</li>
 * </ul>
 * </p>
 *
 * @author dlz-db-test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpringTransactionIntegrationTest extends BaseDBTest {

    @Autowired
    private TransactionTestService transactionTestService;

    /**
     * 测试前置方法 - 初始化测试环境
     * <p>
     * 在每个测试方法执行前，删除旧的 tx_test_user 表（如果存在），
     * 并创建新的 tx_test_user 表，确保每个测试用例都从干净的数据库状态开始。
     * </p>
     * <p>
     * 表结构：
     * <ul>
     *   <li>id: INTEGER PRIMARY KEY - 用户ID（主键）</li>
     *   <li>name: TEXT - 用户名称</li>
     *   <li>age: INTEGER - 用户年龄</li>
     * </ul>
     * </p>
     */
    @Before
    public void setup() {
        log.info("=== 测试前置：初始化测试表 ===");
        
        // 删除旧表（如果存在）
        DB.Jdbc.execute("DROP TABLE IF EXISTS tx_test_user");
        log.info("已删除旧的 tx_test_user 表");
        
        // 创建新表
        DB.Jdbc.execute("CREATE TABLE tx_test_user (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)");
        log.info("已创建新的 tx_test_user 表");
        
        // 验证表为空
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("测试开始时表应该为空", 0, count);
        log.info("验证通过：表为空，记录数 = 0");
    }

    /**
     * 测试后置方法 - 清理测试数据
     * <p>
     * 在每个测试方法执行后，清理 tx_test_user 表中的所有数据，
     * 确保测试用例之间的数据隔离，避免相互影响。
     * </p>
     */
    @After
    public void teardown() {
        log.info("=== 测试后置：清理测试数据 ===");
        
        try {
            // 清理测试数据
            DB.Jdbc.execute("DELETE FROM tx_test_user");
            log.info("已清理 tx_test_user 表中的所有数据");
        } catch (Exception e) {
            log.warn("清理测试数据时发生异常（可能表已被删除）: {}", e.getMessage());
        }
    }

    /**
     * 测试：验证测试环境配置和生命周期管理
     * <p>
     * 该测试验证：
     * <ul>
     *   <li>测试表能够正确创建</li>
     *   <li>数据能够正确插入和查询</li>
     *   <li>测试前后的数据隔离机制正常工作</li>
     * </ul>
     * </p>
     */
    @Test
    public void testSetupAndTeardown() {
        log.info("=== 测试：验证测试环境配置 ===");
        
        // 验证表为空
        long countBefore = DB.Jdbc.select("SELECT * FROM tx_test_user").count();
        assertEquals("测试开始时应该没有数据", 0, countBefore);
        log.info("验证通过：测试开始时记录数 = 0");
        
        // 插入测试数据
        DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 1, "test_user", 25);
        log.info("已插入测试数据：id=1, name=test_user, age=25");
        
        // 验证数据已插入
        long countAfter = DB.Jdbc.select("SELECT * FROM tx_test_user").count();
        assertEquals("数据应该已插入", 1, countAfter);
        log.info("验证通过：插入后记录数 = 1");
        
        // 验证数据内容
        ResultMap user = DB.Jdbc.select("SELECT * FROM tx_test_user WHERE id = ?", 1).queryOne();
        assertNotNull("应该能查询到用户数据", user);
        assertEquals("用户名称应该正确", "test_user", user.getStr("name"));
        assertEquals("用户年龄应该正确", Integer.valueOf(25), user.getInt("age"));
        log.info("验证通过：数据内容正确");
        
        log.info("=== 测试完成：环境配置正常 ===");
    }

    // ========================================
    // Spring 包含 DLZ 事务测试场景
    // ========================================

    /**
     * 测试：Spring 事务包含 DLZ 事务 - 内层异常导致两层事务都回滚
     * <p>
     * 测试场景：
     * <ul>
     *   <li>外层：Spring 事务（@Transactional）插入数据</li>
     *   <li>内层：DLZ 事务（DB.Tx.run()）插入数据并抛出异常</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>内层 DLZ 事务抛出 RuntimeException</li>
     *   <li>异常向外传播到 Spring 事务</li>
     *   <li>两层事务的所有数据都应该被回滚</li>
     *   <li>数据库中不应该有任何记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 2.1, 2.4, 6.1, 6.2
     */
    @Test
    public void testSpringContainsDlz_InnerException_BothRollback() {
        log.info("=== 测试：Spring 包含 DLZ - 内层异常 - 两层回滚 ===");

        try {
            transactionTestService.springContainsDlzInnerExceptionBothRollback();
            throw new AssertionError("应该抛出异常");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
            assertEquals("异常消息应该正确", "1003:[事务执行失败：DLZ inner exception]", e.getMessage());
        }

        // 验证两层的数据都已回滚
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("两层事务的数据都应该被回滚", 0, count);
        log.info("验证通过：记录数 = 0，两层事务都已回滚");

        log.info("=== 测试完成 ===");
    }

    /**
     * 测试：Spring 事务包含 DLZ 事务 - 外层异常导致两层事务都回滚
     * <p>
     * 测试场景：
     * <ul>
     *   <li>外层：Spring 事务（@Transactional）插入数据</li>
     *   <li>内层：DLZ 事务（DB.Tx.run()）插入数据（不抛出异常）</li>
     *   <li>外层：在 DLZ 事务之后抛出异常</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>内层 DLZ 事务正常提交</li>
     *   <li>外层 Spring 事务抛出 RuntimeException</li>
     *   <li>两层事务的所有数据都应该被回滚</li>
     *   <li>数据库中不应该有任何记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 2.2, 2.4, 6.1, 6.2
     */
    @Test
    public void testSpringContainsDlz_OuterException_BothRollback() {
        log.info("=== 测试：Spring 包含 DLZ - 外层异常 - 两层回滚 ===");

        try {
            transactionTestService.springContainsDlzOuterExceptionBothRollback();
            throw new AssertionError("应该抛出异常");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
            assertEquals("异常消息应该正确", "Spring outer exception", e.getMessage());
        }

        // 验证两层的数据都已回滚
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("两层事务的数据都应该被回滚", 0, count);
        log.info("验证通过：记录数 = 0，两层事务都已回滚");

        log.info("=== 测试完成 ===");
    }

    /**
     * 测试：Spring 事务包含 DLZ 事务 - 无异常时两层事务都提交
     * <p>
     * 测试场景：
     * <ul>
     *   <li>外层：Spring 事务（@Transactional）插入数据</li>
     *   <li>内层：DLZ 事务（DB.Tx.run()）插入数据</li>
     *   <li>不抛出任何异常</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>内层 DLZ 事务正常提交</li>
     *   <li>外层 Spring 事务正常提交</li>
     *   <li>两层事务的所有数据都应该被持久化</li>
     *   <li>数据库中应该有 2 条记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 2.3, 2.4, 6.1, 6.2
     */
    @Test
    public void testSpringContainsDlz_NoException_BothCommit() {
        log.info("=== 测试：Spring 包含 DLZ - 无异常 - 两层提交 ===");

        transactionTestService.springContainsDlzNoExceptionBothCommit();

        // 验证两层的数据都已提交
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("两层事务的数据都应该被提交", 2, count);
        log.info("验证通过：记录数 = 2，两层事务都已提交");

        // 验证数据内容
        ResultMap user1 = DB.Jdbc.select("SELECT * FROM tx_test_user WHERE id = ?", 1).queryOne();
        assertNotNull("应该能查询到外层数据", user1);
        assertEquals("外层数据名称应该正确", "spring_outer", user1.getStr("name"));

        ResultMap user2 = DB.Jdbc.select("SELECT * FROM tx_test_user WHERE id = ?", 2).queryOne();
        assertNotNull("应该能查询到内层数据", user2);
        assertEquals("内层数据名称应该正确", "dlz_inner", user2.getStr("name"));
        log.info("验证通过：数据内容正确");

        log.info("=== 测试完成 ===");
    }

    // ========================================
    // DLZ 包含 Spring 事务测试场景
    // ========================================

    /**
     * 测试：DLZ 事务包含 Spring 事务 - 内层异常导致两层事务都回滚
     * <p>
     * 测试场景：
     * <ul>
     *   <li>外层：DLZ 事务（DB.Tx.run()）插入数据</li>
     *   <li>内层：Spring 事务（@Transactional）插入数据并抛出异常</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>内层 Spring 事务抛出 RuntimeException</li>
     *   <li>异常向外传播到 DLZ 事务</li>
     *   <li>两层事务的所有数据都应该被回滚</li>
     *   <li>数据库中不应该有任何记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 3.1, 3.4, 3.5, 6.1, 6.2
     */
    @Test
    public void testDlzContainsSpring_InnerException_BothRollback() {
        log.info("=== 测试：DLZ 包含 Spring - 内层异常 - 两层回滚 ===");
        
        try {
            // 外层 DLZ 事务
            DB.Tx.run(() -> {
                // 外层插入数据
                DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 
                                1, "dlz_outer", 30);
                log.info("外层 DLZ 事务：已插入数据 id=1");
                
                // 内层 Spring 事务：插入数据并抛出异常
                transactionTestService.insertUserInSpringTxWithException(2, "spring_inner", 25);
            });
            
            // 不应该执行到这里
            throw new AssertionError("应该抛出异常");
            
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
            assertEquals("异常消息应该正确", "1003:[事务执行失败：Spring transaction exception]", e.getMessage());
        }
        
        // 验证两层的数据都已回滚
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("两层事务的数据都应该被回滚", 0, count);
        log.info("验证通过：记录数 = 0，两层事务都已回滚");
        
        log.info("=== 测试完成 ===");
    }

    /**
     * 测试：DLZ 事务包含 Spring 事务 - 外层异常导致两层事务都回滚
     * <p>
     * 测试场景：
     * <ul>
     *   <li>外层：DLZ 事务（DB.Tx.run()）插入数据</li>
     *   <li>内层：Spring 事务（@Transactional）插入数据（不抛出异常）</li>
     *   <li>外层：在 Spring 事务之后抛出异常</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>内层 Spring 事务正常提交</li>
     *   <li>外层 DLZ 事务抛出 RuntimeException</li>
     *   <li>两层事务的所有数据都应该被回滚</li>
     *   <li>数据库中不应该有任何记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 3.2, 3.4, 6.1, 6.2
     */
    @Test
    public void testDlzContainsSpring_OuterException_BothRollback() {
        log.info("=== 测试：DLZ 包含 Spring - 外层异常 - 两层回滚 ===");
        
        try {
            // 外层 DLZ 事务
            DB.Tx.run(() -> {
                // 外层插入数据
                DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 
                                1, "dlz_outer", 30);
                log.info("外层 DLZ 事务：已插入数据 id=1");
                
                // 内层 Spring 事务：插入数据（不抛出异常）
                transactionTestService.insertUserInSpringTx(2, "spring_inner", 25);
                log.info("内层 Spring 事务：已插入数据 id=2");
                
                // 外层抛出异常触发回滚
                throw new RuntimeException("DLZ outer exception");
            });
            
            // 不应该执行到这里
            throw new AssertionError("应该抛出异常");
            
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
            assertEquals("异常消息应该正确", "1003:[事务执行失败：DLZ outer exception]", e.getMessage());
        }
        
        // 验证两层的数据都已回滚
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("两层事务的数据都应该被回滚", 0, count);
        log.info("验证通过：记录数 = 0，两层事务都已回滚");
        
        log.info("=== 测试完成 ===");
    }

    /**
     * 测试：DLZ 事务包含 Spring 事务 - 无异常时两层事务都提交
     * <p>
     * 测试场景：
     * <ul>
     *   <li>外层：DLZ 事务（DB.Tx.run()）插入数据</li>
     *   <li>内层：Spring 事务（@Transactional）插入数据</li>
     *   <li>不抛出任何异常</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>内层 Spring 事务正常提交</li>
     *   <li>外层 DLZ 事务正常提交</li>
     *   <li>两层事务的所有数据都应该被持久化</li>
     *   <li>数据库中应该有 2 条记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 3.3, 3.4, 6.1, 6.2
     */
    @Test
    public void testDlzContainsSpring_NoException_BothCommit() {
        log.info("=== 测试：DLZ 包含 Spring - 无异常 - 两层提交 ===");
        
        // 外层 DLZ 事务
        DB.Tx.run(() -> {
            // 外层插入数据
            DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 
                            1, "dlz_outer", 30);
            log.info("外层 DLZ 事务：已插入数据 id=1");
            
            // 内层 Spring 事务：插入数据
            transactionTestService.insertUserInSpringTx(2, "spring_inner", 25);
            log.info("内层 Spring 事务：已插入数据 id=2");
        });
        log.info("外层 DLZ 事务：正常完成");
        
        // 验证两层的数据都已提交
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("两层事务的数据都应该被提交", 2, count);
        log.info("验证通过：记录数 = 2，两层事务都已提交");
        
        // 验证数据内容
        ResultMap user1 = DB.Jdbc.select("SELECT * FROM tx_test_user WHERE id = ?", 1).queryOne();
        assertNotNull("应该能查询到外层数据", user1);
        assertEquals("外层数据名称应该正确", "dlz_outer", user1.getStr("name"));
        
        ResultMap user2 = DB.Jdbc.select("SELECT * FROM tx_test_user WHERE id = ?", 2).queryOne();
        assertNotNull("应该能查询到内层数据", user2);
        assertEquals("内层数据名称应该正确", "spring_inner", user2.getStr("name"));
        
        log.info("=== 测试完成 ===");
    }

    // ========================================
    // 异常类型测试
    // ========================================

    /**
     * 测试：RuntimeException 触发事务回滚
     * <p>
     * 测试场景：
     * <ul>
     *   <li>在 DLZ 事务中插入数据</li>
     *   <li>抛出 RuntimeException</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>RuntimeException 被抛出</li>
     *   <li>事务应该被回滚</li>
     *   <li>数据库中不应该有任何记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 5.1, 5.2, 5.3
     */
    @Test
    public void testRollback_RuntimeException() {
        log.info("=== 测试：RuntimeException 触发回滚 ===");
        
        try {
            DB.Tx.run(() -> {
                // 插入数据
                DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 
                                1, "test_user", 20);
                log.info("已插入数据 id=1");
                
                // 抛出 RuntimeException
                throw new RuntimeException("业务异常");
            });
            
            // 不应该执行到这里
            throw new AssertionError("应该抛出异常");
            
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
            assertEquals("异常消息应该正确", "1003:[事务执行失败：业务异常]", e.getMessage());
        }
        
        // 验证数据已回滚
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("数据应该被回滚", 0, count);
        log.info("验证通过：记录数 = 0，事务已回滚");
        
        log.info("=== 测试完成 ===");
    }

    /**
     * 测试：Checked Exception 触发事务回滚
     * <p>
     * 测试场景：
     * <ul>
     *   <li>调用带有 @Transactional(rollbackFor = Exception.class) 的方法</li>
     *   <li>方法插入数据后抛出 Checked Exception</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>Checked Exception 被抛出</li>
     *   <li>由于配置了 rollbackFor = Exception.class，事务应该被回滚</li>
     *   <li>数据库中不应该有任何记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 5.4, 5.2, 5.3
     */
    @Test
    public void testRollback_CheckedException() {
        log.info("=== 测试：Checked Exception 触发回滚 ===");
        
        try {
            // 调用会抛出 Checked Exception 的方法
            transactionTestService.insertUserWithCheckedException(1, "test_user", 20);
            
            // 不应该执行到这里
            throw new AssertionError("应该抛出异常");
            
        } catch (Exception e) {
            log.info("捕获到预期异常: {}", e.getMessage());
            assertEquals("异常消息应该正确", "Checked exception", e.getMessage());
        }
        
        // 验证数据已回滚
        long count = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("数据应该被回滚", 0, count);
        log.info("验证通过：记录数 = 0，事务已回滚");
        
        log.info("=== 测试完成 ===");
    }

    // ========================================
    // 连接状态和数据隔离测试
    // ========================================

    /**
     * 测试：异常后数据库连接状态仍然可用
     * <p>
     * 测试场景：
     * <ul>
     *   <li>第一次操作：在事务中插入数据并抛出异常</li>
     *   <li>第二次操作：在新事务中插入数据（不抛出异常）</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>第一次操作的数据应该被回滚</li>
     *   <li>第二次操作应该能够正常执行</li>
     *   <li>数据库连接状态不应该被破坏</li>
     *   <li>数据库中应该只有第二次操作的 1 条记录</li>
     * </ul>
     * </p>
     *
     * @Requirements 5.5, 8.3
     */
    @Test
    public void testConnectionState_AfterException() {
        log.info("=== 测试：异常后连接状态验证 ===");
        
        // 第一次操作：抛出异常
        try {
            DB.Tx.run(() -> {
                DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 
                                1, "test1", 20);
                log.info("第一次操作：已插入数据 id=1");
                
                throw new RuntimeException("异常");
            });
            
            throw new AssertionError("应该抛出异常");
            
        } catch (RuntimeException e) {
            log.info("第一次操作：捕获到预期异常: {}", e.getMessage());
        }
        
        // 验证第一次操作的数据已回滚
        long countAfterFirst = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("第一次操作的数据应该被回滚", 0, countAfterFirst);
        log.info("验证通过：第一次操作已回滚");
        
        // 第二次操作：正常执行
        DB.Tx.run(() -> {
            DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 
                            2, "test2", 30);
            log.info("第二次操作：已插入数据 id=2");
        });
        
        // 验证第二次操作成功
        long countAfterSecond = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("第二次操作应该成功", 1, countAfterSecond);
        log.info("验证通过：第二次操作成功，记录数 = 1");
        
        // 验证数据内容
        ResultMap user = DB.Jdbc.select("SELECT * FROM tx_test_user WHERE id = ?", 2).queryOne();
        assertNotNull("应该能查询到第二次操作的数据", user);
        assertEquals("数据名称应该正确", "test2", user.getStr("name"));
        
        log.info("=== 测试完成：连接状态正常 ===");
    }

    /**
     * 测试：测试用例之间的数据隔离
     * <p>
     * 测试场景：
     * <ul>
     *   <li>验证测试开始时数据库为空</li>
     *   <li>插入测试数据</li>
     *   <li>验证数据已插入</li>
     *   <li>依赖 @After teardown() 方法清理数据</li>
     * </ul>
     * </p>
     * <p>
     * 预期行为：
     * <ul>
     *   <li>测试开始时数据库应该为空（记录数为 0）</li>
     *   <li>插入数据后应该能够查询到（记录数为 1）</li>
     *   <li>测试结束后数据应该被清理（由 @After 方法保证）</li>
     * </ul>
     * </p>
     *
     * @Requirements 4.2, 4.3, 4.4
     */
    @Test
    public void testDataIsolation_BetweenTests() {
        log.info("=== 测试：数据隔离验证 ===");
        
        // 验证测试开始时数据库为空
        long countBefore = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("测试开始时应该没有数据", 0, countBefore);
        log.info("验证通过：测试开始时记录数 = 0");
        
        // 插入测试数据
        DB.Jdbc.execute("INSERT INTO tx_test_user (id, name, age) VALUES (?, ?, ?)", 
                        1, "isolation_test", 20);
        log.info("已插入测试数据 id=1");
        
        // 验证数据已插入
        long countAfter = DB.Jdbc.select("SELECT COUNT(*) FROM tx_test_user").count();
        assertEquals("数据应该已插入", 1, countAfter);
        log.info("验证通过：插入后记录数 = 1");
        
        // 验证数据内容
        ResultMap user = DB.Jdbc.select("SELECT * FROM tx_test_user WHERE id = ?", 1).queryOne();
        assertNotNull("应该能查询到数据", user);
        assertEquals("数据名称应该正确", "isolation_test", user.getStr("name"));
        
        log.info("=== 测试完成：数据隔离正常（@After 方法将清理数据）===");
    }
}
