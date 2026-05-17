package com.dlz.test.db.config;

import com.dlz.db.core.*;
import com.dlz.db.ds.DataSourceConfig;
import com.dlz.db.ds.DataSourceProperty;
import com.dlz.db.service.ICommService;
import com.dlz.db.service.impl.CommServiceImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试用 SQLite 数据库提供者 - 使用真实 SQLite 数据库
 * 用于单元测试，提供真实的数据库连接
 */
public class SqliteDbProvider extends ADbProvider {

    private final SqliteSqlExecutor sqlExecutor;
    private final ICommService commService;
    private final MockCacheExecutor cacheExecutor;
    private final DlzDbProperties sqlConfig;
    private final DataSource dataSource;
    private static final AtomicLong idGenerator = new AtomicLong(1000);

    public SqliteDbProvider() {
        // 创建 SQLite 数据源
        this.dataSource = createSqliteDataSource();
        
        // 初始化 SQL 执行器
        this.sqlExecutor = new SqliteSqlExecutor(dataSource);
        
        // 初始化服务
        this.commService = new CommServiceImpl(sqlExecutor);
        
        // 初始化缓存执行器
        this.cacheExecutor = new MockCacheExecutor();
        
        // 初始化配置
        this.sqlConfig = createDefaultConfig();
        
        // 初始化测试数据
        initTestData();
    }

    private DataSource createSqliteDataSource() {
        try {
            // 创建测试目录
            File dir = new File("./test");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 配置 HikariCP 连接池
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:./test/testdb-core.sqlite3");
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            return new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SQLite data source", e);
        }
    }

    private DlzDbProperties createDefaultConfig() {
        DlzDbProperties config = new DlzDbProperties();
        config.setLogicDeleteField("IS_DELETED");
        config.setTableCacheTime(-1);
        
        DlzDbProperties.Log logConfig = new DlzDbProperties.Log();
        logConfig.setShowResult(false);
        logConfig.setShowRunSql(false);
        logConfig.setShowCaller(false);
        logConfig.setSlowSqlThreshold(0L);
        config.setLog(logConfig);
        
        return config;
    }

    /**
     * 初始化测试数据
     */
    private void initTestData() {
        try {
            // 创建 user 表
//            sqlExecutor.update("DROP TABLE IF EXISTS user");
//            sqlExecutor.update("CREATE TABLE user (" +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "name TEXT NOT NULL, " +
//                    "age INTEGER, " +
//                    "create_time TEXT, " +
//                    "sex TEXT, " +
//                    "flag TEXT, " +
//                    "score TEXT, " +
//                    "dept_id TEXT, " +
//                    "level TEXT, " +
//                    "retry_count INTEGER DEFAULT 0, " +
//                    "address TEXT, " +
//                    "phone TEXT, " +
//                    "vip TEXT, " +
//                    "email TEXT, " +
//                    "remark TEXT, " +
//                    "status TEXT DEFAULT '1', " +
//                    "create_user TEXT, " +
//                    "update_user TEXT, " +
//                    "update_time TEXT, " +
//                    "is_deleted TEXT DEFAULT '0', " +
//                    "delete_user TEXT, " +
//                    "delete_time TEXT, " +
//                    "is_expired TEXT, " +
//                    "code TEXT, " +
//                    "type TEXT, " +
//                    "city TEXT, " +
//                    "description TEXT)");
//
//            // 创建 test_user 表
//            sqlExecutor.update("DROP TABLE IF EXISTS test_user");
//            sqlExecutor.update("CREATE TABLE test_user (" +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "name TEXT NOT NULL, " +
//                    "age INTEGER, " +
//                    "email TEXT, " +
//                    "status INTEGER DEFAULT 1)");
            
            // 插入 user 测试数据
            sqlExecutor.update("INSERT INTO user (name, age, email, status) VALUES (?, ?, ?, ?)", 
                    "张三", 25, "zhangsan@example.com", "1");
            sqlExecutor.update("INSERT INTO user (name, age, email, status) VALUES (?, ?, ?, ?)", 
                    "李四", 30, "lisi@example.com", "1");
            sqlExecutor.update("INSERT INTO user (name, age, email, status) VALUES (?, ?, ?, ?)", 
                    "王五", 28, "wangwu@example.com", "0");
            
            // 插入 test_user 测试数据
            sqlExecutor.update("INSERT INTO test_user (name, age, email, status) VALUES (?, ?, ?, ?)", 
                    "测试用户1", 25, "test1@example.com", 1);
            sqlExecutor.update("INSERT INTO test_user (name, age, email, status) VALUES (?, ?, ?, ?)", 
                    "测试用户2", 30, "test2@example.com", 1);
                    
            System.out.println("SQLite 测试数据初始化完成");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test data", e);
        }
    }

    @Override
    public ITxExecutor createTxExecutor(DataSourceConfig dataSourceConfig) {
        // 对于 SQLite，我们提供一个简单的事务执行器
        return new SimpleTxExecutor(dataSource);
    }

    @Override
    public ISqlExecutor getSqlExecutor() {
        return sqlExecutor;
    }

    @Override
    public ICommService getService() {
        return commService;
    }

    @Override
    public IRedisExecutor getCacheExecutor() {
        return cacheExecutor;
    }

    @Override
    public DlzDbProperties getSqlConfig() {
        return sqlConfig;
    }
    
    /**
     * 获取数据源（供测试使用）
     */
    public DataSource getDataSource() {
        return dataSource;
    }
}