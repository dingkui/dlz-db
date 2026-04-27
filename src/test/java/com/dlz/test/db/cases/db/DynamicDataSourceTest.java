package com.dlz.test.db.cases.db;

import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.dto.ResultMap;
import com.dlz.test.db.config.SpingDbBaseTest;
import com.dlz.test.db.entity.SysSql;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * DB.Dynamic.use() 动态数据源测试
 * SQLite 支持事务（BEGIN/COMMIT/ROLLBACK）
 */
@Slf4j
public class DynamicDataSourceTest extends SpingDbBaseTest {

    private static final String TEST_DS_NAME = "test_sqlite";

    @Before
    public void setup() {
        // 清理已有数据源
        try {
            DB.Dynamic.removeDataSource(TEST_DS_NAME);
        } catch (Exception e) {
            // 忽略不存在的情况
        }

        // 初始化动态数据源（使用 SQLite 内存数据库）
        DataSourceProperty properties = new DataSourceProperty();
        properties.setName(TEST_DS_NAME);
        properties.setDriverClassName("org.sqlite.JDBC");
        properties.setUrl("jdbc:sqlite::memory:");
        properties.setUsername("");
        properties.setPassword("");
        DB.Dynamic.setDataSource(properties);

        // 在动态数据源中创建测试表
        DB.Dynamic.use(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("CREATE TABLE IF NOT EXISTS user (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT, " +
                    "age INTEGER, " +
                    "create_time TEXT)");
            return null;
        });
    }

    @Test
    public void testUseSwitchDataSource() {
        // 向动态数据源插入数据
        DB.Dynamic.use(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("INSERT INTO user (id, name, age) VALUES (?, ?, ?)", 1, "test_user", 20);
            return null;
        });

        // 从动态数据源查询数据
        List<ResultMap> list = DB.Dynamic.use(TEST_DS_NAME, () ->
                DB.Jdbc.select("SELECT * FROM user WHERE id = ?", 1).queryList()
        );

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("test_user", list.get(0).getStr("name"));
        assertEquals(Integer.valueOf(20), list.get(0).getInt("age"));
    }

    @Test
    public void testUseDefaultDataSource() {
        // 使用默认数据源（无需 use）
        List<ResultMap> defaultList = DB.Jdbc.select("SELECT 1 as num").queryList();
        assertNotNull(defaultList);
        assertEquals(1, defaultList.size());

        // 使用 default 显式切换
        List<ResultMap> list = DB.Dynamic.use("default", () ->
                DB.Jdbc.select("SELECT 1 as num").queryList()
        );
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    public void testUseDataSourceNotExist() {
        // 访问不存在的数据源应抛异常
        try {
            DB.Dynamic.use("not_exist", () -> DB.Jdbc.select("SELECT 1").queryList());
            fail("应抛出异常：数据源不存在");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("数据源不存在") || e.getCause().getMessage().contains("数据源不存在"));
        }
    }

    @Test
    public void testManualTransactionCommit() {
        // 手动事务：成功提交
        DB.Dynamic.use(TEST_DS_NAME, () -> {
            DB.Jdbc.execute("INSERT INTO user (id, name, age) VALUES (?, ?, ?)", 100, "tx_user", 25);
            return null;
        });

        // 验证数据已提交
        List<ResultMap> list = DB.Dynamic.use(TEST_DS_NAME, () ->
                DB.Jdbc.select("SELECT * FROM user WHERE id = ?", 100).queryList()
        );
        assertEquals(1, list.size());
        assertEquals("tx_user", list.get(0).getStr("name"));
    }

    @Test
    @Transactional
    public void testSpringTransactionalWithDefaultDs() {
        // Spring @Transactional 只在默认数据源生效
        // 此测试验证默认数据源下事务正常工作
        SysSql dict = new SysSql();
        dict.setId(99999L);
        dict.setSqlKey("tx_test");
        dict.setIsDeleted(0);
        DB.Pojo.insert(dict);

        // 查询验证
        SysSql result = DB.Pojo.select(SysSql.class).eq(SysSql::getId, 99999L).queryBean();
        assertNotNull(result);
        assertEquals("tx_test", result.getSqlKey());
    }

    @Test
    public void testManualTransactionRollback() {
        // 验证：SQLite 手动控制事务回滚
        // 由于当前 DB.Dynamic.use() 不自动管理事务，
        // 这里演示通过异常模拟回滚场景（实际未提交的数据在单个 Connection 中不会持久化）

        String uuid = "rollback_test_" + System.currentTimeMillis();

        try {
            DB.Dynamic.use(TEST_DS_NAME,true, () -> {
                DB.Jdbc.execute("INSERT INTO user (id, name, age) VALUES (?, ?, ?)", 200, uuid, 30);
                // 模拟业务异常，数据应未提交（但 SQLite 默认自动提交，此处仅演示）
                throw new RuntimeException("模拟业务异常");
            });
            fail("应抛出异常");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("模拟业务异常"));
        }

        // 验证：由于当前没有 useWithTransaction，数据可能已提交（取决于实现）
        // 此断言用于说明当前行为
        int size = DB.Dynamic.use(TEST_DS_NAME, () ->
                DB.Jdbc.select("SELECT * FROM user WHERE name = ?", uuid).count()
        );
        assertEquals(0, size);


        try {
            DB.Dynamic.use(TEST_DS_NAME,false, () -> {
                DB.Jdbc.execute("INSERT INTO user (id, name, age) VALUES (?, ?, ?)", 200, uuid, 30);
                // 模拟业务异常，数据应未提交（但 SQLite 默认自动提交，此处仅演示）
                throw new RuntimeException("模拟业务异常");
            });
            fail("应抛出异常");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("模拟业务异常"));
        }

        // 验证：由于当前没有 useWithTransaction，数据可能已提交（取决于实现）
        // 此断言用于说明当前行为
        size = DB.Dynamic.use(TEST_DS_NAME, () ->
                DB.Jdbc.select("SELECT * FROM user WHERE name = ?", uuid).count()
        );
        assertEquals(1, size);
    }

    @Test
    public void testRemoveDataSource() {
        // 添加临时数据源
        String tempName = "temp_ds";
        DataSourceProperty tempProp = new DataSourceProperty();
        tempProp.setName(tempName);
        tempProp.setDriverClassName("org.sqlite.JDBC");
        tempProp.setUrl("jdbc:sqlite::memory:");
        DB.Dynamic.setDataSource(tempProp);

        // 验证存在
        assertTrue(DB.Dynamic.getAllDataSourceNames().contains(tempName));

        // 删除
        boolean removed = DB.Dynamic.removeDataSource(tempName);
        assertTrue(removed);

        // 验证不存在
        assertFalse(DB.Dynamic.getAllDataSourceNames().contains(tempName));
    }
}
