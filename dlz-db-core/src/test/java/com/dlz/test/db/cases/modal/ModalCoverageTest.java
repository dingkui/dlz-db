package com.dlz.test.db.cases.modal;

import com.dlz.db.dialect.DbDialect;
import com.dlz.db.exception.DbParameterException;
import com.dlz.db.interceptor.SqlBuildInterceptor;
import com.dlz.db.modal.DB;
import com.dlz.db.modal.DbConfig;
import com.dlz.db.modal.dto.PageRequest;
import com.dlz.db.modal.dto.BatchResult;
import com.dlz.db.modal.RequireUtil;
import com.dlz.db.modal.options.DeleteOption;
import com.dlz.db.modal.options.InsertOption;
import com.dlz.db.modal.options.UpdateOption;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.entity.TestUser;
import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ModalCoverageTest extends BaseDBTest {
    @Test
    void jdbcConvenienceApiAndValidation() {
        String name = "coverage-" + System.nanoTime();
        DB.jdbc.execute("INSERT INTO user(name, age, status, deleted) VALUES (?, ?, ?, ?)", name, 20, "1", 0);
        assertNotNull(DB.jdbc.executeWrapper("DELETE FROM user WHERE name = ?", "does-not-exist"));
        assertNotNull(DB.jdbc.one("SELECT * FROM user WHERE name = ?", name));
        assertNotNull(DB.jdbc.first("SELECT * FROM user WHERE name = ?", name));
        assertNotNull(DB.jdbc.one("SELECT * FROM user WHERE name = ?", com.dlz.test.db.entity.User.class, name));
        assertNotNull(DB.jdbc.first("SELECT * FROM user WHERE name = ?", com.dlz.test.db.entity.User.class, name));
        assertFalse(DB.jdbc.list("SELECT * FROM user WHERE name = ?", name).isEmpty());
        assertFalse(DB.jdbc.list("SELECT * FROM user WHERE name = ?", com.dlz.test.db.entity.User.class, name).isEmpty());
        assertEquals(1, DB.jdbc.count("SELECT * FROM user WHERE name = ?", name));
        assertFalse(DB.jdbc.page("SELECT * FROM user", PageRequest.of(1, 10)).records().isEmpty());
        assertFalse(DB.jdbc.page("SELECT * FROM user", PageRequest.of(1, 10), com.dlz.test.db.entity.User.class).records().isEmpty());
        assertThrows(DbParameterException.class, () -> DB.jdbc.one(null));
        assertThrows(DbParameterException.class, () -> DB.jdbc.first(" "));
        assertThrows(DbParameterException.class, () -> DB.jdbc.one("SELECT 1", (Class<?>) null));
        assertThrows(DbParameterException.class, () -> DB.jdbc.first("SELECT 1", (Class<?>) null));
        assertThrows(DbParameterException.class, () -> DB.jdbc.list("SELECT 1", (Class<?>) null));
        assertThrows(DbParameterException.class, () -> DB.jdbc.page("SELECT 1", null));
    }

    @Test
    void sqlConvenienceApiAndValidation() {
        Map<String, Object> params = new HashMap<>();
        String name = "coverage-sql-" + System.nanoTime();
        params.put("name", name);
        DB.jdbc.execute("INSERT INTO user(name, age, status, deleted) VALUES (?, ?, ?, ?)", name, 21, "1", 0);
        assertNotNull(DB.sql.selectWrapper("SELECT * FROM user WHERE name = #{name}", params, null, Collections.emptyMap()));
        assertNotNull(DB.sql.executeWrapper("DELETE FROM user WHERE name = #{name}", params));
        assertNotNull(DB.sql.executeWrapper("DELETE FROM user WHERE name = #{name}", null, Collections.emptyMap()));
        assertNotNull(DB.sql.one("SELECT * FROM user WHERE name = #{name}", params));
        assertNotNull(DB.sql.first("SELECT * FROM user WHERE name = #{name}", params));
        assertNotNull(DB.sql.one("SELECT * FROM user WHERE name = #{name}", com.dlz.test.db.entity.User.class, params));
        assertNotNull(DB.sql.first("SELECT * FROM user WHERE name = #{name}", com.dlz.test.db.entity.User.class, params));
        assertFalse(DB.sql.list("SELECT * FROM user WHERE name = #{name}", params).isEmpty());
        assertFalse(DB.sql.list("SELECT * FROM user WHERE name = #{name}", com.dlz.test.db.entity.User.class, params).isEmpty());
        assertEquals(1, DB.sql.count("SELECT * FROM user WHERE name = #{name}", params));
        assertThrows(DbParameterException.class, () -> DB.sql.one(null));
        assertThrows(DbParameterException.class, () -> DB.sql.first(""));
        assertThrows(DbParameterException.class, () -> DB.sql.one("SELECT 1", (Class<?>) null));
        assertThrows(DbParameterException.class, () -> DB.sql.first("SELECT 1", (Class<?>) null));
        assertThrows(DbParameterException.class, () -> DB.sql.list("SELECT 1", (Class<?>) null));
    }

    @Test
    void configValidationAndInitialization() {
        DbConfig config = new DbConfig();
        SqlBuildInterceptor interceptor = () -> true;
        assertSame(config, config.plugin(interceptor));
        assertSame(config, config.registerDialect((DbDialect) () -> "coverage"));
        assertSame(config, config.sql("coverage", "SELECT 1"));
        assertSame(config, config.sql("key.coverage2", "SELECT 2"));
        assertSame(config, config.logicDeleteField("deleted"));
        assertSame(config, config.columnNameConvertor(String::toUpperCase));
        assertThrows(DbParameterException.class, () -> config.plugin(null));
        assertThrows(DbParameterException.class, () -> config.registerDialect(null));
        assertThrows(DbParameterException.class, () -> config.sql("", "SELECT 1"));
        assertThrows(DbParameterException.class, () -> config.sql("key", ""));
        assertThrows(DbParameterException.class, () -> config.logicDeleteField(" "));
        assertThrows(DbParameterException.class, () -> config.columnNameConvertor(null));
        assertThrows(DbParameterException.class, () -> config.init(null, null, null, null));
        assertThrows(DbParameterException.class, config::init);
        DataSource dataSource = (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class<?>[]{DataSource.class}, (proxy, method, args) -> null);
        assertSame(config, config.dataSource(dataSource));
        assertSame(config, config.sqlExecutor(com.dlz.db.support.DBHolder.getSqlExecutor()));
        assertSame(config, config.txExecutor(source -> com.dlz.db.support.DBHolder.getTxExecutor(null)));
        assertSame(config, config.init());
        assertSame(config, config.init());
        assertThrows(DbParameterException.class, () -> config.logicDeleteField("deleted2"));
    }

    @Test
    void requireUtilitiesAndEmptyBatchBoundaries() {
        assertSame("id", RequireUtil.requireId("id"));
        assertSame("entity", RequireUtil.requireEntity("entity"));
        JSONMap values = new JSONMap();
        assertSame(values, RequireUtil.requireValues(values));
        assertSame("SELECT 1", RequireUtil.requireJdbcSql("SELECT 1"));
        assertSame("key.demo", RequireUtil.requireSqlKey("key.demo"));
        assertSame(TestUser.class, RequireUtil.requireType(TestUser.class));
        assertEquals("id", RequireUtil.requireIdInfo(TestUser.class).getDbName());
        assertEquals("id", RequireUtil.requireIdColumn("user"));
        assertEquals("user", RequireUtil.requireTableName("user"));
        assertSame(Collections.emptyList(), RequireUtil.requireList(Collections.emptyList()));
        assertTrue(RequireUtil.requireMaps(Arrays.asList(Collections.singletonMap("id", 1))).get(0).containsKey("id"));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireId(null));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireEntity(null));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireValues(null));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireIds((String) null));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireIds((java.util.Collection<?>) null));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireJdbcSql(" "));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireSqlKey(" "));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireType(null));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireList(null));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireBatchSize(0));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireMaps(Collections.singletonList("bad")));
        assertThrows(RuntimeException.class, () -> RequireUtil.requireIdColumn("test_no_id"));
        assertThrows(RuntimeException.class, () -> RequireUtil.requireTableName("user;drop"));
        assertThrows(DbParameterException.class, () -> RequireUtil.requireJdbcSql(null));
        assertDoesNotThrow(() -> {
            Constructor<RequireUtil> constructor = RequireUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
        assertEquals(0, DB.batch.insert(Collections.emptyList()).totalItems());
        assertEquals(0, DB.batch.update(Collections.emptyList()).totalItems());
        assertEquals(0, DB.batch.insert("user", Collections.emptyList()).totalItems());
        assertEquals(0, DB.batch.update("user", Collections.emptyList()).totalItems());
        BatchResult sqlResult = DB.batch.execute("SELECT 1", Collections.emptyList());
        assertEquals(0, sqlResult.totalItems());
        assertEquals(0, DB.batch.execute("DELETE FROM user WHERE id = ?", Collections.singletonList(new Object[]{-1}), 1).knownAffectedRows());
    }

    @Test
    void tableAndPojoOptionAndIdBoundaries() {
        long id = 1_000_000_000L + Math.abs(System.nanoTime() % 100_000L);
        JSONMap row = new JSONMap();
        row.put("id", id);
        row.put("name", "coverage-option");
        row.put("deleted", 0);
        assertDoesNotThrow(() -> DB.table.insert("user", row, InsertOption.INCLUDE_NULL));
        JSONMap autoRow = new JSONMap();
        autoRow.put("name", "coverage-auto-" + id);
        autoRow.put("deleted", 0);
        assertDoesNotThrow(() -> DB.table.insertWithAutoKey("user", autoRow, InsertOption.INCLUDE_NULL));
        assertDoesNotThrow(() -> DB.table.updateById("user", row, UpdateOption.INCLUDE_NULL));
        assertDoesNotThrow(() -> DB.table.deleteById("user", id, DeleteOption.PHYSICAL));
        assertNotNull(DB.table.selectByIds("user", Collections.singletonList(id)));
        assertNotNull(DB.table.deleteByIds("user", Collections.singletonList(id)));
        assertThrows(RuntimeException.class, () -> DB.table.selectByIds("user", Collections.emptyList()));
        assertThrows(DbParameterException.class, () -> DB.table.insert("user", null));
        assertThrows(DbParameterException.class, () -> DB.table.updateById("user", new JSONMap()));

        TestUser user = new TestUser();
        user.setId(id);
        user.setName("coverage-pojo");
        assertNotNull(DB.pojo.insertWrapper(TestUser.class));
        assertNotNull(DB.pojo.updateWrapper(user));
        assertNotNull(DB.pojo.selectWrapper(TestUser.class, (com.dlz.kit.fn.DlzFn[]) null));
        assertDoesNotThrow(() -> DB.pojo.insert(user, InsertOption.INCLUDE_NULL));
        TestUser added = new TestUser();
        added.setName("coverage-add-" + id);
        assertDoesNotThrow(() -> DB.pojo.add(added));
        assertDoesNotThrow(() -> DB.pojo.updateById(user, UpdateOption.INCLUDE_NULL));
        assertNotNull(DB.pojo.selectByIds(TestUser.class, Collections.singletonList(1L)));
        assertNotNull(DB.pojo.selectByIds(TestUser.class, "1"));
        assertDoesNotThrow(() -> DB.pojo.existsById(TestUser.class, 1));
        assertThrows(RuntimeException.class, () -> DB.pojo.selectByIds(TestUser.class, Collections.emptyList()));
        assertThrows(DbParameterException.class, () -> DB.pojo.insert(null));
        assertThrows(DbParameterException.class, () -> DB.pojo.updateById(new TestUser()));
    }
}
