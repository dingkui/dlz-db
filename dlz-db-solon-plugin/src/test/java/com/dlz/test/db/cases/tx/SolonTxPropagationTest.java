package com.dlz.test.db.cases.tx;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import com.dlz.test.db.service.SolonPropagationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.noear.solon.Solon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Solon 事务传播性测试
 *
 * <p>核心目标：验证 Solon 原生 <b>@Tran</b> 与 DLZ-DB <b>DB.Tx.run()</b> 之间的事务传播行为。</p>
 *
 * <p>测试场景覆盖：</p>
 * <ul>
 *   <li>{@code @Tran} 外层 + {@code DB.Tx.run()} 内层：内层加入外层事务（同一物理连接）</li>
 *   <li>{@code DB.Tx.run()} 外层 + {@code @Tran} 内层：内层感知外层 dlz-db 事务</li>
 *   <li>多层嵌套：{@code @Tran} → {@code DB.Tx.run()} → {@code @Tran}</li>
 *   <li>异常回滚传播：任一层失败，整体数据回滚</li>
 * </ul>
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SolonTxPropagationTest extends BaseDBTest {

    private SolonPropagationService propagationService;

    @BeforeAll
    public static void setupTable() {
        DB.Jdbc.execute("DELETE FROM USER");
    }

    @BeforeEach
    public void setUp() {
        propagationService = Solon.context().getBean(SolonPropagationService.class);
        DB.Jdbc.execute("DELETE FROM USER");
    }

    // ==================== @Tran 外层 + DB.Tx.run() 内层 ====================

    /**
     * 测试1：@Tran 外层包含 DB.Tx.run() 内层，全部成功
     * 验证：外层和内层数据应同时提交
     */
    @Test
    @Order(1)
    void testTranOuterDlzInnerCommit() {
        propagationService.tranOuterDlzInner();

        assertEquals(1, countByName("Tran外层用户"), "@Tran 外层数据应提交");
        assertEquals(1, countByName("DLZ内层用户"), "DB.Tx.run() 内层数据也应提交（同一事务）");
        log.info("✅ @Tran 外层 + DB.Tx.run() 内层：提交成功，事务传播正常");
    }

    /**
     * 测试2：@Tran 外层包含 DB.Tx.run() 内层，内层抛异常
     * 验证：内层异常导致整体回滚，外层数据也不应存在
     */
    @Test
    @Order(2)
    void testTranOuterDlzInnerRollback() {
        assertThrows(RuntimeException.class, () ->
                propagationService.tranOuterDlzInnerRollback()
        );

        assertEquals(0, countByName("Tran外层-回滚"), "@Tran 外层数据应回滚");
        assertEquals(0, countByName("DLZ内层-回滚"), "DB.Tx.run() 内层数据也应回滚");
        log.info("✅ @Tran 外层 + DB.Tx.run() 内层异常：整体回滚成功");
    }

    // ==================== DB.Tx.run() 外层 + @Tran 内层 ====================

    /**
     * 测试3：DB.Tx.run() 外层调用 @Tran 内层，全部成功
     * 验证：外层和内层数据应同时提交
     */
    @Test
    @Order(3)
    void testDlzOuterTranInnerCommit() {
        propagationService.dlzOuterTranInner();

        assertEquals(1, countByName("DLZ外层用户"), "DB.Tx.run() 外层数据应提交");
        assertEquals(1, countByName("@Tran内层用户"), "@Tran 内层数据也应提交（加入同一事务）");
        log.info("✅ DB.Tx.run() 外层 + @Tran 内层：提交成功，事务传播正常");
    }

    /**
     * 测试4：DB.Tx.run() 外层调用 @Tran 内层，内层抛异常
     * 验证：内层异常导致整体回滚
     */
    @Test
    @Order(4)
    void testDlzOuterTranInnerRollback() {
        assertThrows(RuntimeException.class, () ->
                propagationService.dlzOuterTranInnerRollback()
        );

        assertEquals(0, countByName("DLZ外层-回滚"), "DB.Tx.run() 外层数据应回滚");
        assertEquals(0, countByName("@Tran内层-回滚"), "@Tran 内层数据也应回滚");
        log.info("✅ DB.Tx.run() 外层 + @Tran 内层异常：整体回滚成功");
    }

    // ==================== 多层嵌套传播 ====================

    /**
     * 测试5：@Tran → DB.Tx.run() → @Tran 三层嵌套，全部成功
     * 验证：三层数据同时提交
     */
    @Test
    @Order(5)
    void testMultiLayerPropagationCommit() {
        propagationService.tranOuterWithNestedDlzAndTran();

        assertEquals(1, countByName("最外层-Tran"), "最外层数据应提交");
        assertEquals(1, countByName("中间层-DLZ"), "中间层数据应提交");
        assertEquals(1, countByName("最内层-Tran"), "最内层数据应提交");
        log.info("✅ 三层嵌套事务传播：全部提交成功");
    }

    // ==================== 外层失败回滚内层 ====================

    /**
     * 测试6：@Tran 外层失败，验证内层 DLZ 操作也被回滚
     * 验证：同一物理连接下，外层异常导致整体回滚
     */
    @Test
    @Order(6)
    void testTranOuterFailRollbackDlzInner() {
        assertThrows(RuntimeException.class, () ->
                propagationService.tranOuterFailRollbackDlzInner()
        );

        assertEquals(0, countByName("Tran外层-失败"), "@Tran 外层数据应回滚");
        assertEquals(0, countByName("DLZ内层-被回滚"), "DB.Tx.run() 内层数据也应回滚");
        log.info("✅ @Tran 外层异常：内层 DLZ 操作也被回滚，事务传播正确");
    }

    // ==================== 混合场景验证 ====================

    /**
     * 测试7：两种事务机制可以交替使用，互不影响
     * 先执行 @Tran 外层+DLZ 内层，再执行 DLZ 外层+@Tran 内层
     */
    @Test
    @Order(7)
    void testAlternatingPropagation() {
        propagationService.tranOuterDlzInner();
        propagationService.dlzOuterTranInner();

        assertEquals(1, countByName("Tran外层用户"));
        assertEquals(1, countByName("DLZ内层用户"));
        assertEquals(1, countByName("DLZ外层用户"));
        assertEquals(1, countByName("@Tran内层用户"));
        log.info("✅ 交替使用两种事务机制，各自独立提交，无干扰");
    }

    @AfterAll
    public static void cleanup() {
        log.info("========================================");
        log.info("Solon 事务传播性测试完成！");
        log.info("结论：DLZ-DB 与 Solon @Tran 事务传播正常");
        log.info("- @Tran 外层可正确传播到 DB.Tx.run() 内层");
        log.info("- DB.Tx.run() 外层可正确传播到 @Tran 内层");
        log.info("- 多层嵌套事务统一提交/回滚");
        log.info("========================================");
    }

    private long countByName(String name) {
        return DB.Pojo.select(User.class)
                .eq(User::getName, name)
                .count();
    }
}
