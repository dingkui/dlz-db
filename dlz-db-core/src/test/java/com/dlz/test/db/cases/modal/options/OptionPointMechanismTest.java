package com.dlz.test.db.cases.modal.options;

import com.dlz.db.DB;
import com.dlz.db.interceptor.DbPlugin;
import com.dlz.db.interceptor.LogicDeleteInterceptor;
import com.dlz.db.interceptor.SqlBuildInterceptor;
import com.dlz.db.internal.inf.ISqlPara;
import com.dlz.db.internal.items.JdbcItem;
import com.dlz.db.option.DbOption;
import com.dlz.db.option.DbOperation;
import com.dlz.db.option.DbOptions;
import com.dlz.db.option.point.InsertFieldPoint;
import com.dlz.db.option.point.WherePoint;
import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.option.point.context.FieldContribution;
import com.dlz.db.option.point.context.FieldContext;
import com.dlz.db.option.point.context.FieldValue;
import com.dlz.db.sql.SqlFragment;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.json.JSONMap;
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.test.db.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 拦截器供给默认 Option 机制验证：
 * 1. 拦截器通过 supplyOptions 供给的 Option 经桩点（WherePoint/InsertFieldPoint）自动生效
 * 2. 调用点同 key Option 覆盖拦截器默认
 * 3. isEnabled=false 的拦截器不参与；supplyOptions 可按操作/表动态过滤
 * 4. effectiveOptions 合并语义（零开销短路、同 key 调用点优先、supports 过滤）
 * 5. 逻辑删除 Option 在 DELETE 执行时改写为 UPDATE
 */
public class OptionPointMechanismTest extends BaseDBTest {

    @AfterEach
    public void resetInterceptors() {
        DbPlugin.clearInterceptors();
        DbPlugin.registerInterceptor(new LogicDeleteInterceptor("deleted"));
    }

    private static String sqlOf(ISqlPara wrapper) {
        JdbcItem jdbcSql = wrapper.jdbcSql();
        return SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
    }

    // ==================== 1. 拦截器供给的 Option 经桩点生效 ====================

    @Test
    public void interceptor_supplied_where_option_takes_effect() {
        DbPlugin.registerInterceptor(supply(new StatusFilterOption("1")));

        String sql = sqlOf(DB.table.selectWrapper("user"));
        assertTrue(sql.contains("status = '1'"), sql);
        // 拦截器供给与内置逻辑删除同时生效
        assertTrue(sql.contains("deleted = '0'"), sql);
    }

    @Test
    public void interceptor_supplied_insert_field_option_takes_effect() {
        DbPlugin.registerInterceptor(supply(new InsertStatusOption()));

        String sql = sqlOf(DB.table.insertWrapper("user").value("name", "mech"));
        assertTrue(sql.contains("status"), sql);
    }

    // ==================== 2. 调用点 Option 覆盖拦截器默认 ====================

    @Test
    public void user_option_overrides_interceptor_default() {
        DbPlugin.registerInterceptor(supply(new StatusFilterOption("1")));

        String sql = sqlOf(DB.table.selectWrapper("user")
                .options(DbOptions.resolve(DbOperation.SELECT, new StatusFilterOption("2"))));
        assertTrue(sql.contains("status = '2'"), sql);
        assertFalse(sql.contains("status = '1'"), sql);
    }

    // ==================== 3. 开关与动态过滤 ====================

    @Test
    public void disabled_interceptor_not_consulted() {
        DbPlugin.registerInterceptor(new SqlBuildInterceptor() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public List<DbOption> supplyOptions(DbOperation operation, String tableName) {
                return Collections.singletonList(new StatusFilterOption("1"));
            }
        });

        String sql = sqlOf(DB.table.selectWrapper("user"));
        assertFalse(sql.contains("status = '1'"), sql);
    }

    @Test
    public void supply_options_filtered_by_operation_and_table() {
        DbPlugin.registerInterceptor((operation, tableName) ->
                operation == DbOperation.SELECT && "user".equals(tableName)
                        ? Collections.singletonList(new StatusFilterOption("1"))
                        : Collections.emptyList());

        assertTrue(sqlOf(DB.table.selectWrapper("user")).contains("status = '1'"));
        assertFalse(sqlOf(DB.table.selectWrapper("t_b_dict")).contains("status = '1'"));
        // UPDATE 操作同样供给的表不同、供给为空：仅验证不抛错且 update 正常构建
        assertNotNull(sqlOf(DB.table.updateWrapper("user").set("name", "x").eq("id", 1)));
    }

    // ==================== 4. effectiveOptions 合并语义 ====================

    @Test
    public void effective_options_zero_overhead_without_interceptor() {
        DbPlugin.clearInterceptors();
        DbOptions userOptions = DbOptions.resolve(DbOperation.SELECT);

        assertSame(userOptions, DbPlugin.effectiveOptions(DbOperation.SELECT, "user", userOptions));
    }

    @Test
    public void effective_options_merge_semantics() {
        DbPlugin.clearInterceptors();
        DbPlugin.registerInterceptor(supply(new StatusFilterOption("1"), new NotForSelectOption()));

        // 无调用点 Option：拦截器供给生效
        DbOptions effective = DbPlugin.effectiveOptions(DbOperation.SELECT, "user", null);
        assertEquals("1", effective.get(StatusFilterOption.class).getStatus());
        // supports(SELECT)=false 的供给被过滤
        assertNull(effective.get(NotForSelectOption.class));

        // 同 key 调用点优先
        DbOptions userOptions = DbOptions.resolve(DbOperation.SELECT, new StatusFilterOption("2"));
        DbOptions merged = DbPlugin.effectiveOptions(DbOperation.SELECT, "user", userOptions);
        assertEquals("2", merged.get(StatusFilterOption.class).getStatus());
    }

    // ==================== 5. 逻辑删除 DELETE 改写为 UPDATE ====================

    @Test
    public void logic_delete_rewrites_delete_as_update() {
        String name = "mech-" + System.nanoTime();
        DB.jdbc.execute("INSERT INTO user(name,age,status,deleted) VALUES(?,?,?,?)", name, 20, "1", 0);

        int affected = DB.pojo.deleteWrapper(User.class).eq(User::getName, name).execute();

        assertEquals(1, affected);
        // 行未物理删除，仅标记 deleted=1
        assertEquals(1, DB.jdbc.selectWrapper(
                "SELECT COUNT(*) FROM user WHERE name=? AND deleted=1", name).count());
        assertEquals(0, DB.jdbc.selectWrapper(
                "SELECT COUNT(*) FROM user WHERE name=? AND deleted=0", name).count());
    }

    // ==================== 测试用 Option / 拦截器 ====================

    private static SqlBuildInterceptor supply(DbOption... options) {
        return (operation, tableName) -> {
            List<DbOption> list = new java.util.ArrayList<>();
            Collections.addAll(list, options);
            return list;
        };
    }

    /** 自定义 WherePoint：追加 status = #{statusFilterValue}。 */
    static final class StatusFilterOption implements DbOption, WherePoint {
        private static final long serialVersionUID = 1L;

        private final String status;

        StatusFilterOption(String status) {
            this.status = status;
        }

        String getStatus() {
            return status;
        }

        @Override
        public String key() {
            return "custom.status.filter";
        }

        @Override
        public SqlFragment contributeWhere(CrudContext context) {
            JSONMap paras = new JSONMap();
            paras.put("statusFilterValue", status);
            return SqlFragment.of("status = #{statusFilterValue}", paras);
        }
    }

    /** 自定义 InsertFieldPoint：插入时自动补 status 默认值。 */
    static final class InsertStatusOption implements DbOption, InsertFieldPoint {
        private static final long serialVersionUID = 1L;

        @Override
        public String key() {
            return "custom.insert.status";
        }

        @Override
        public FieldContribution contributeInsertFields(FieldContext context) {
            return new FieldContribution(Collections.<String>emptyList(),
                    Collections.singletonList(new FieldValue("status", "9")));
        }
    }

    /** supports 永远返回 false：验证合并时被过滤且不抛错。 */
    static final class NotForSelectOption implements DbOption {
        private static final long serialVersionUID = 1L;

        @Override
        public String key() {
            return "custom.not.for.select";
        }

        @Override
        public boolean supports(DbOperation operation) {
            return false;
        }
    }
}
