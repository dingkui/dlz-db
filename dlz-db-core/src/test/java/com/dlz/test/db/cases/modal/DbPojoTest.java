package com.dlz.test.db.cases.modal;


import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.wrapper.PojoDelete;
import com.dlz.db.modal.wrapper.PojoQuery;
import com.dlz.db.modal.wrapper.PojoUpdate;
import com.dlz.db.support.helper.HelperScan;
import com.dlz.db.support.helper.SqlHelper;
import com.dlz.kit.exception.SystemException;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.AutoIdEntity;
import com.dlz.test.db.entity.Orders;
import com.dlz.test.db.entity.SysSql;
import com.dlz.test.db.entity.TestUser;
import com.dlz.test.db.entity.Yc1Record;
import com.dlz.test.db.entity.YcRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DbPojo POJO 执行操作测试（含 Wrapper API 入口验证 + 实际执行）
 */
@DisplayName("DbPojo POJO 操作测试")
class DbPojoTest extends BaseDBTest {

    // ========== Wrapper API 入口验证（不执行） ==========

    @Test
    @DisplayName("测试 select - Class 参数")
    void testSelectWrapperWithClass() {
        PojoQuery<TestUser> query = DB.pojo.selectWrapper(TestUser.class);
        assertNotNull(query);
        assertTrue(query instanceof PojoQuery);
    }

    @Test
    @DisplayName("测试 select - null 条件 Bean")
    void testSelectWrapperWithNullCondition() {
        assertThrows(Exception.class, () -> DB.pojo.selectWrapper(null));
    }

    @Test
    @DisplayName("测试 delete - Class 参数")
    void testDeleteWrapperWithClass() {
        PojoDelete<TestUser> delete = DB.pojo.deleteWrapper(TestUser.class);
        assertNotNull(delete);
        assertTrue(delete instanceof PojoDelete);
    }

    @Test
    @DisplayName("测试  UPDATE - Class 参数")
    void testUpdateWrapperWithClass() {
        PojoUpdate<TestUser> update = DB.pojo.updateWrapper(TestUser.class);
        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试  UPDATE - Bean 参数")
    void testUpdateWrapperWithBean() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("新名字");
        PojoUpdate<TestUser> update = DB.pojo.updateWrapper(TestUser.class);
        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试  UPDATE - Bean 参数带忽略规则")
    void testUpdateWrapperWithBeanAndIgnore() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("新名字");
        PojoUpdate<TestUser> update = DB.pojo.updateWrapper(TestUser.class).ignore((name, val) -> "password".equals(name));
        assertNotNull(update);
        assertTrue(update instanceof PojoUpdate);
    }

    @Test
    @DisplayName("测试  UPDATE - null 忽略规则")
    void testUpdateWrapperWithNullIgnore() {
        TestUser user = new TestUser();
        user.setId(1L);
        PojoUpdate<TestUser> update = DB.pojo.updateWrapper(TestUser.class);
        assertNotNull(update);
    }

    // ========== INSERT 执行测试 ==========

    @Test
    @DisplayName("测试 insert 方法返回类型")
    void testAddReturnType() {
        TestUser user = new TestUser();
        user.setName("张三");
        DB.pojo.insert(user);
    }

    @Test
    @DisplayName("insert - SysSql 手动 id 为空应抛异常")
    void testAddSysSqlWithoutId() {
        SysSql dict = new SysSql();
        dict.setName("xx");
        try {
            DB.pojo.insert(dict);
            fail("应该抛出 SystemException");
        } catch (SystemException e) {
            assertTrue(e.getMessage().contains("SysSql.id为手动输入"));
        }
    }

    @Test
    public void addYc1RecordTest() {
        YcRecord dict = new Yc1Record();
        dict.setRe("xx");
        dict.setPcid("xx");
        dict.setSta(1);
        DB.pojo.insert(dict);
    }

    @Test
    public void addYc1RecordMinimalTest() {
        Yc1Record yc1Record = new Yc1Record();
        yc1Record.setSta(1);
        DB.pojo.insert(yc1Record);
    }

    @Test
    @DisplayName("insert - AUTO 主键 execute 后应回填")
    void addExecuteAutoBackfillTest() {
        AutoIdEntity entity = new AutoIdEntity();
        entity.setName("auto_backfill");
        assertNull(entity.getId());

        DB.pojo.insert(entity);
        assertNotNull(entity.getId(), "AUTO 类型 execute 后应回填生成的主键");
        assertTrue(entity.getId() > 0, "回填的主键应大于 0");
    }

    @Test
    @DisplayName("insert - ASSIGN_ID 主键 execute 后应回填")
    void addExecuteAssignIdBackfillTest() {
        Orders orders = new Orders();
        orders.setUserId("u001");
        orders.setAmount(100);
        orders.setUpdateTime(new Date());
        orders.setCreateTime(LocalDateTime.now());
        assertNull(orders.getId());

        DB.pojo.insert(orders);
        assertNotNull(orders.getId(), "ASSIGN_ID 类型 execute 后应预生成并回填主键");
    }

    // ========== INSERT OR UPDATE 测试 ==========

    @Test
    @DisplayName("insertOrUpdate - SysSql 手动 id 为空应抛异常")
    void testAddOrUpdateWrapperSysSqlWithoutId() {
        SysSql dict = new SysSql();
        dict.setName("xx");
        try {
            DB.pojo.save(dict);
            fail("应该抛出 SystemException");
        } catch (SystemException e) {
            assertTrue(e.getMessage().contains("SysSql.id为手动输入"));
        }
    }

    @Test
    @DisplayName("insertOrUpdate - id 为空时执行 insert")
    void testAddOrUpdateWrapperWithNullId() {
        TestUser user = new TestUser();
        user.setName("张三");
        DB.pojo.save(user);
    }

    @Test
    @DisplayName("insertOrUpdate - id 不为空时执行 update")
    void testAddOrUpdateWrapperWithId() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");
        DB.pojo.save(user);
    }

    // ========== INSERT（含前置清理 + 后续查询验证） ==========

    @Test
    @DisplayName("insert - 先删后插再查（实际执行验证）")
    void testAddWithCleanupAndVerify() {
        SysSql dict = new SysSql();
        dict.setId(666L);
        dict.setSqlKey("xxx");
        dict.setDeleted(0);
        DB.pojo.deleteWrapper(SysSql.class).eq(SysSql::getId, 666L).execute();
        DB.pojo.deleteWrapper(SysSql.class).eq(SysSql::getId, 666L).ignoreLogicDelete(true).execute();

        DB.pojo.insert(dict);

        assertNotNull(DB.table.selectWrapper("Sys_Sql").setAllowFullQuery(true).queryList());
    }

    @Test
    @DisplayName("insert - 缺少必填字段应抛异常")
    void testAddSysSqlMissingRequiredField() {
        SysSql dict = new SysSql();
        dict.setSqlKey("xxx");
        try {
            DB.pojo.insert(dict);
            fail("应该抛出 SystemException");
        } catch (SystemException e) {
            assertTrue(e.getMessage().contains("为手动输入,不能为空"));
        }
    }

    // ========== UPDATE BY id 测试 ==========

    @Test
    public void updateWrapperByIdSysSqlTest() {
        SysSql dict = new SysSql();
        dict.setId(1L);
        dict.setName("xx");
        DB.pojo.updateById(dict);
    }

    @Test
    @DisplayName("updateById - id 为空抛出异常")
    void testUpdateWrapperByIdWithNullId() {
        TestUser user = new TestUser();
        user.setName("张三");
        assertThrows(SystemException.class, () -> DB.pojo.updateById(user));
    }

    @Test
    @DisplayName("updateById - id 不为空")
    void testUpdateWrapperByIdWithId() {
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");
        DB.pojo.updateById(user);
    }

    // ========== SELECT BY id 测试 ==========

    @Test
    public void selectWrapperByIdSysSqlTest() {
        DB.pojo.selectById(SysSql.class, "1");
    }

    @Test
    @DisplayName("selectById - id 为空抛出异常")
    void testSelectWrapperByIdWithNullId() {
        assertThrows(SystemException.class, () -> DB.pojo.selectById(TestUser.class, null));
    }

    @Test
    @DisplayName("selectById - id 不为空")
    void testSelectWrapperByIdWithId() {
        DB.pojo.selectById(TestUser.class, 1L);
    }

    @Test
    @DisplayName("selectById - 字符串 id")
    void testSelectWrapperByIdWithStringId() {
        DB.pojo.selectById(TestUser.class, "1");
    }

    // ========== SELECT BY IDS 测试 ==========

    @Test
    @DisplayName("selectByIds - IDs 为空抛出异常")
    void testSelectWrapperByIdsWithNullIds() {
        assertThrows(SystemException.class, () -> DB.pojo.selectByIds(TestUser.class, (String) null));
    }

    @Test
    @DisplayName("selectByIds - IDs 不为空")
    void testSelectWrapperByIdsWithIds() {
        DB.pojo.selectByIds(TestUser.class, "1,2,3");
    }

    // ========== DELETE BY id 测试 ==========

    @Test
    @DisplayName("deleteById - id 为空抛出异常")
    void testDeleteWrapperByIdWithNullId() {
        assertThrows(SystemException.class, () -> DB.pojo.deleteById(TestUser.class, null));
    }

    @Test
    @DisplayName("deleteById - id 不为空")
    void testDeleteWrapperByIdWithId() {
        DB.pojo.deleteById(TestUser.class, 1L);
    }

    // ========== DELETE BY IDS 测试 ==========

    @Test
    public void deleteWrapperByIdsSysSqlTest() {
        DB.pojo.deleteByIds(SysSql.class, "1,2,3");
    }

    @Test
    @DisplayName("deleteByIds - String IDs 为空抛出异常")
    void testDeleteWrapperByIdsStringWithNullIds() {
        assertThrows(SystemException.class, () -> DB.pojo.deleteByIds(TestUser.class, (String) null));
    }

    @Test
    @DisplayName("deleteByIds - String IDs 不为空")
    void testDeleteWrapperByIdsStringWithIds() {
        DB.pojo.deleteByIds(TestUser.class, "1,2,3");
    }

    @Test
    @DisplayName("deleteByIds - List IDs 为空抛出异常")
    void testDeleteWrapperByIdsListWithNullIds() {
        assertThrows(SystemException.class, () -> DB.pojo.deleteByIds(TestUser.class, (List<?>) null));
    }

    @Test
    @DisplayName("deleteByIds - List IDs 不为空")
    void testDeleteWrapperByIdsListWithIds() {
        DB.pojo.deleteByIds(TestUser.class, Arrays.asList(1, 2, 3));
    }

    @Test
    @DisplayName("deleteByIds - 空列表抛出异常")
    void testDeleteWrapperByIdsListWithEmptyList() {
        assertThrows(Exception.class, () -> DB.pojo.deleteByIds(TestUser.class, Collections.emptyList()));
    }

    // ========== DYNAMIC 数据源测试 ==========

    @Test
    public void getUseDbById1() {
        DataSourceProperty properties = new DataSourceProperty();
        properties.setName("test");
        properties.setDriverClassName("org.sqlite.JDBC");
        properties.setUrl("jdbc:sqlite:./test/testdb_dynamic.sqlite3");
        DB.ds.setDataSource(properties);

        DB.ds.use("test", () -> {
            SqlHelper helper = DB.ds.getSqlHelper();
            HelperScan.initTable(SysSql.class, helper);
            DB.pojo.selectById(SysSql.class, "1");
            DB.pojo.selectById(SysSql.class, "2");
            return null;
        });

        DB.pojo.selectById(SysSql.class, "1");
        DB.ds.use("default", () -> {
            DB.pojo.selectById(SysSql.class, "1");
            DB.pojo.selectById(SysSql.class, "2");
            return null;
        });
    }
}
