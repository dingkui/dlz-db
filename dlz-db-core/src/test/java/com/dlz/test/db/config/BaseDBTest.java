package com.dlz.test.db.config;

import com.dlz.db.core.DlzDbProperties;
import com.dlz.db.core.jdbc.JdbcDbAdapter;
import com.dlz.db.core.jdbc.JdbcSqlExecutor;
import com.dlz.db.core.jdbc.JdbcTxExecutor;
import com.dlz.db.support.helper.HelperScan;
import com.dlz.db.util.DbLogUtil;
import com.dlz.kit.util.id.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;

import javax.sql.DataSource;

/**
 * 测试基类 - 自动初始化 MockDbProvider
 * 所有需要数据库功能的测试类继承此类即可
 */
@Slf4j
public abstract class BaseDBTest {

    /**
     * JUnit 5 初始化入口
     */
    @BeforeAll
    public static void bootstrapJunit5() {
        initMockDb();
    }

    /**
     * JUnit 4 初始化入口
     */
    @BeforeClass
    public static void bootstrapJunit4() {
        initMockDb();
    }


    static void initMockDb() {
        TraceUtil.setTraceId("initMockDb");
        final DlzDbProperties testConfig = SqliteDbProviderUtil.createTestConfig();
        final DataSource dataSource = SqliteDbProviderUtil.createSqliteDataSource();
        testConfig.getHelper().setPackageName("com.dlz.test.db.entity");
        testConfig.getHelper().setAutoUpdate(true);
        DbLogUtil.init(testConfig);
        // 设置 MockDbProvider
        JdbcDbAdapter.init(testConfig, dataSource, JdbcSqlExecutor.class, JdbcTxExecutor.class);
        // 自动更新数据库结构
        if (testConfig.getHelper().isAutoUpdate()) {
            log.info("dlzHelper autoUpdate ...");
            HelperScan.scan(testConfig.getHelper().getPackageName());
        }
        log.info("---------------------------------------------------------");

        TraceUtil.clearTraceId();
    }
    
    /**
     * 根据 Java 类型获取简化的 JDBC 类型索引
     * @param type Java 类型
     * @return JDBC 类型索引
     */
//    private static Integer getJdbcTypeIndex(Class<?> type) {
//        if (type == null) {
//            return null;
//        }
//
//        // String -> VARCHAR (12)
//        if (String.class.isAssignableFrom(type)) {
//            return 12;
//        }
//
//        // Integer, int -> INTEGER (4)
//        if (type == Integer.class || type == int.class) {
//            return 4;
//        }
//
//        // Long, long -> BIGINT (-5)
//        if (type == Long.class || type == long.class) {
//            return -5;
//        }
//
//        // Double, double -> DOUBLE (8)
//        if (type == Double.class || type == double.class) {
//            return 8;
//        }
//
//        // Float, float -> FLOAT (6)
//        if (type == Float.class || type == float.class) {
//            return 6;
//        }
//
//        // Boolean, boolean -> BOOLEAN (16)
//        if (type == Boolean.class || type == boolean.class) {
//            return 16;
//        }
//
//        // Date, java.sql.Date, java.sql.Timestamp -> TIMESTAMP (93)
//        if (java.util.Date.class.isAssignableFrom(type) ||
//            java.sql.Date.class.isAssignableFrom(type) ||
//            java.sql.Timestamp.class.isAssignableFrom(type)) {
//            return 93;
//        }
//
//        // BigDecimal -> DECIMAL (3)
//        if (java.math.BigDecimal.class.isAssignableFrom(type)) {
//            return 3;
//        }
//
//        // Byte, byte -> TINYINT (-6)
//        if (type == Byte.class || type == byte.class) {
//            return -6;
//        }
//
//        // Short, short -> SMALLINT (5)
//        if (type == Short.class || type == short.class) {
//            return 5;
//        }
//
//        // 默认返回 null，让调用方使用递增索引
//        return null;
//    }
}
