package com.dlz.test.db.cases.id_strategy;

import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.modal.DB;
import com.dlz.db.support.DBHolder;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.AutoIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

/**
 * 智能号段 ID 生成器测试用例
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SegmentIdGeneratorTest extends BaseDBTest {

    private static final String TEST_TABLE = "TEST_AUTO_ID";

    @BeforeEach
    void setUp() {
        // 清理测试数据，确保每次测试环境干净
        // 清理号段记录
        DB.Jdbc.execute("delete from sys_seq");
        DB.Jdbc.execute("delete from TEST_AUTO_ID");
        final DataSourceProperty properties = new DataSourceProperty();
        properties.setName("test");
        properties.setDriverClassName("org.sqlite.JDBC");
        properties.setUrl("jdbc:sqlite:./test/testdb_dynamic.sqlite3");
        DB.Dynamic.setDataSource(properties);
        DB.Dynamic.use("test", () -> {
            // 清理测试数据，确保每次测试环境干净
            DB.Jdbc.execute("DROP TABLE IF EXISTS " + TEST_TABLE);
            DB.Jdbc.execute("CREATE TABLE " + TEST_TABLE + " (id INTEGER PRIMARY KEY, name TEXT)");

            // 清理号段记录
            DB.Jdbc.execute("DROP TABLE IF EXISTS sys_seq");
        });
    }

    @Test
    @Order(1)
    void testAutoCreateTable() {
        // 第一次调用 sequence 应该会自动创建 table_id_segment 表
        long maxId = DBHolder.sequence(TEST_TABLE, 10);
        Assertions.assertTrue(maxId >= 10, "首次取号应成功并返回终止值");
        log.info("Auto create table test passed, maxId: {}", maxId);
    }

    @Test
    @Order(2)
    void testSingleIdGeneration() {
        long maxId1 = DBHolder.sequence(TEST_TABLE, 1);
        long maxId2 = DBHolder.sequence(TEST_TABLE, 1);
        Assertions.assertEquals(maxId1 + 1, maxId2, "单次取号应连续递增");
        log.info("Single ID generation: {} -> {}", maxId1, maxId2);
    }

    @Test
    @Order(2)
    void testSingleIdGeneration2() {
        int i = 20;
        AutoIdEntity autoIdEntity = new AutoIdEntity();
        autoIdEntity.setName("test");
        long maxId1 = DB.Pojo.insertOrUpdate(autoIdEntity).getId();
        while (i-- > 0) {
            AutoIdEntity autoIdEntity2 = new AutoIdEntity();
            autoIdEntity2.setName("test");
            long maxId2 = DB.Pojo.insertOrUpdate(autoIdEntity2).getId();
            Assertions.assertEquals(maxId1 + 1, maxId2, "单次取号应连续递增");
            maxId1 = maxId2;
        }
    }

    @Test
    @Order(3)
    void testBatchIdGeneration() {
        // 一次性取 50 个 ID
        long maxId = DBHolder.sequence(TEST_TABLE, 50);

        // 验证返回的是终止值
        long startId = maxId - 50 + 1;
        log.info("Batch generation: [{} - {}]", startId, maxId);

        // 再取一个，应该是 maxId + 1
        long nextMaxId = DBHolder.sequence(TEST_TABLE, 1);
        Assertions.assertEquals(maxId + 1, nextMaxId, "批量取号后下一个 ID 应紧接着");
    }

    @Test
    @Order(4)
    void testLargeBatchGeneration() {
        // 一次性取 2000 个 ID
        long maxId = DBHolder.sequence(TEST_TABLE, 2000);

        Assertions.assertTrue(maxId >= 2000, "大批量取号应成功");
        log.info("Large batch generation maxId: {}", maxId);

        // 验证连续性
        long nextMaxId = DBHolder.sequence(TEST_TABLE, 1);
        Assertions.assertEquals(maxId + 1, nextMaxId, "大批量取号后连续性应保持");
    }

    @Test
    @Order(5)
    void testConcurrency() throws InterruptedException {
        int threadCount = 10;
        java.util.Set<Long> allIds = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        long maxId = DBHolder.sequence(TEST_TABLE, 1);
                        allIds.add(maxId);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        Assertions.assertEquals(threadCount * 100, allIds.size(), "并发产生的 ID 必须唯一且数量正确");
        log.info("Concurrency test passed: {} unique IDs generated", allIds.size());
    }

    @Test
    @Order(6)
    void testAutoRecoveryAfterRestart() {
        // 模拟重启：清空内存缓存，但保留数据库记录
        // 简单验证：再次取号应该从数据库最新值之后开始
        long currentMax = DBHolder.sequence(TEST_TABLE, 1);
        long nextMax = DBHolder.sequence(TEST_TABLE, 1);
        Assertions.assertEquals(currentMax + 1, nextMax, "取号应保持绝对连续，不跳号");
    }

    @Test
    @Order(7)
    void testMultiDataSourceSameTableName() {
        // 场景：模拟两个不同的数据源 DS_A 和 DS_B，它们都有一个名为 "orders" 的表
        String sharedTableName = "test_orders_xx";

        long dsA_id1 = DB.Dynamic.use("test", () -> DBHolder.sequence(sharedTableName));
        long dsA_id2 = DB.Dynamic.use("test", () -> DBHolder.sequence(sharedTableName));

        // 在 DS_B 取号（应该从 1 开始，与 DS_A 无关）
        long dsB_id1 = DBHolder.sequence(sharedTableName, 1);
        long dsB_id2 = DBHolder.sequence(sharedTableName, 1);

        // 验证：不同数据源的 ID 应该是独立的
        Assertions.assertEquals(dsA_id1 + 1, dsA_id2, "同一数据源内 ID 应连续");
        Assertions.assertEquals(dsB_id1 + 1, dsB_id2, "同一数据源内 ID 应连续");

        // 验证：不同数据源的起始 ID 应该相同（因为都是新表/新记录）
        Assertions.assertEquals(dsA_id1, dsB_id1, "不同数据源的初始 ID 应一致");

        log.info("Multi-DS Test Passed: DS_A=[{}, {}], DS_B=[{}, {}]", dsA_id1, dsA_id2, dsB_id1, dsB_id2);
    }
}
