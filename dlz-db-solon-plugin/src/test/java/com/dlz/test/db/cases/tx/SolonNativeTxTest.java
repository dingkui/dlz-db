package com.dlz.test.db.cases.tx;

import com.dlz.db.modal.DB;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.service.SolonNativeTxService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.noear.solon.Solon;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Solon 原生事务测试
 * 不使用 dlz-db API，只验证 Solon @Tran 本身是否正常工作
 */
@Slf4j
public class SolonNativeTxTest extends BaseDBTest {

    private SolonNativeTxService nativeTxService;

    @BeforeAll
    public static void setupTable() {
        // 创建测试表
        DB.Jdbc.execute("DELETE FROM USER");
    }

    @BeforeEach
    public void setUp() {
        // 从 Solon 容器获取 Service Bean
        nativeTxService = Solon.context().getBean(SolonNativeTxService.class);

        // 清理数据
        DB.Jdbc.execute("DELETE FROM USER");
    }

    /**
     * 测试1：Solon @Tran + dlz-db API 正常提交
     */
    @Test
    void testSolonTranWithDlzDbCommit() throws Exception {
        nativeTxService.insertWithDlzDbAndCommit();

        int count = nativeTxService.countByName("Solon事务包含DB提交用户");
        assertEquals(1, count, "应该插入1条记录");
        log.info("✅ Solon @Tran 能正确提交 dlz-db 操作");
    }

    /**
     * 测试2：Solon @Tran + dlz-db API 异常回滚
     */
    @Test
    void testSolonTranWithDlzDbRollback() throws Exception {
        int before = nativeTxService.countByName("Solon事务包含DB回滚用户");

        try {
            nativeTxService.insertWithDlzDbAndRollback();
            fail("应该抛出 RuntimeException");
        } catch (RuntimeException e) {
            log.info("捕获到预期异常: {}", e.getMessage());
        }

        int after = nativeTxService.countByName("Solon事务包含DB回滚用户");
        assertEquals(before, after, "异常应该触发回滚，dlz-db 操作应该被回滚");
        log.info("✅ Solon @Tran 能正确回滚 dlz-db 操作");
    }

    @AfterAll
    public static void cleanup() {
        log.info("========================================");
        log.info("Solon 原生事务测试完成！");
        log.info("结论：Solon @Tran 本身工作正常");
        log.info("========================================");
    }
}
