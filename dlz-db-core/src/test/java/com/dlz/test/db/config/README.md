# MockDbProvider 测试框架使用指南

## 概述

MockDbProvider 是一个基于内存的数据库提供者实现，用于单元测试环境。它无需真实数据库连接即可模拟数据库操作，大幅提高测试执行速度和便利性。

## 核心组件

### 1. MockDbProvider
测试用数据库提供者，继承自 `ADbProvider`，提供以下功能：
- 初始化内存测试数据
- 配置默认数据库属性
- 管理 Mock 组件生命周期

### 2. MockSqlExecutor
测试用 SQL 执行器，实现 `ISqlExecutor` 接口：
- 基于内存 Map 存储表数据
- 支持基本的 SELECT、INSERT、UPDATE 操作
- 自动提取 SQL 中的表名
- 预置 user 表测试数据（3条记录）

### 3. MockCommService
测试用公共服务，委托给 MockSqlExecutor 执行。

### 4. MockCacheExecutor
测试用缓存执行器，基于 ConcurrentHashMap 实现：
- set/get/del 基本缓存操作
- incrBy 原子递增
- exists 键存在检查

### 5. MockTxExecutor
测试用事务执行器，空实现（测试环境不开启真实事务）。

### 6. BaseDBTest
测试基类，所有需要数据库功能的测试类应继承此类：
```java
public class MyTest extends BaseDBTest {
    @Test
    void testSomething() {
        // 自动初始化 MockDbProvider
    }
}
```

## 快速开始

### 1. 创建测试类

```java
import com.dlz.test.db.config.BaseDBTest;
import com.dlz.db.modal.wrapper.JdbcSelect;
import org.junit.jupiter.api.Test;

public class MyDatabaseTest extends BaseDBTest {

    @Test
    void testQuery() {
        JdbcSelect query = new JdbcSelect("SELECT * FROM user");
        List<ResultMap> list = query.queryList();

        assertNotNull(list);
        assertEquals(3, list.size());
    }
}
```

### 2. 使用预置数据

MockSqlExecutor 已预置 user 表数据：

| id | name | age | email | status | delete_time |
|----|------|-----|-------|--------|-------------|
| 1  | 张三 | 25  | zhangsan@example.com | 1 | null |
| 2  | 李四 | 30  | lisi@example.com | 1 | null |
| 3  | 王五 | 28  | wangwu@example.com | 0 | 2023-01-01 |

### 3. 添加自定义测试数据

```java
@Test
void testCustomData() {
    // 获取 MockSqlExecutor 实例
    MockDbProvider provider = (MockDbProvider) DBHolder.dbProvider;
    MockSqlExecutor executor = (MockSqlExecutor) provider.getSqlExecutor();
    
    // 添加测试数据
    JSONMap newData = new JSONMap();
    newData.put("id", 4L);
    newData.put("name", "赵六");
    executor.addTestData("user", newData);
    
    // 验证
    JdbcQuery query = new JdbcQuery("SELECT * FROM user");
    List<ResultMap> list = query.queryList();
    assertEquals(4, list.size());
}
```

## 支持的 SQL 操作

### SELECT 查询
```java
JdbcQuery query = new JdbcQuery("SELECT * FROM user");
List<ResultMap> list = query.queryList();
```

### 单条查询
```java
JdbcQuery query = new JdbcQuery("SELECT * FROM user WHERE id = 1");
ResultMap user = query.queryOne();
```

### 转换器
```java
query.convertNative();  // 使用原生列名
query.convertUpper();   // 转换为大写
query.convertLower();   // 转换为小写
```

## 限制说明

### 当前不支持的功能

1. **条件构造器过滤**
   - `eq()`, `gt()`, `like()` 等方法不会真正过滤数据
   - Mock 实现返回表中所有数据

2. **分页功能**
   - `limit()`, `page()` 不会真正分页
   - 返回所有数据

3. **COUNT 查询**
   - `count()` 方法可能抛出异常
   - 建议直接查询列表后获取 size

4. **Bean 映射**
   - `queryList(User.class)` 返回空列表
   - 建议使用 `queryList()` 获取 ResultMap 后手动转换

5. **真实事务**
   - MockTxExecutor 不开启真实事务
   - 直接执行任务，不回滚

## 扩展 Mock 数据

### 添加新表

```java
@BeforeEach
void setUp() {
    MockDbProvider provider = (MockDbProvider) DBHolder.dbProvider;
    MockSqlExecutor executor = (MockSqlExecutor) provider.getSqlExecutor();
    
    // 添加 order 表数据
    List<JSONMap> orders = new ArrayList<>();
    orders.add(createOrder(1L, "ORDER001", 100.0));
    executor.getTableData().put("order", orders);
}
```

### 清空数据

```java
executor.clearTestData("user");  // 清空 user 表
```

## 最佳实践

1. **继承 BaseDBTest**
   - 所有数据库测试类都应继承此基类
   - 自动初始化 Mock 环境

2. **使用预置数据**
   - 优先使用预置的 user 表数据
   - 避免重复创建测试数据

3. **简化测试场景**
   - Mock 环境适合测试业务逻辑
   - 复杂 SQL 功能应在集成测试中验证

4. **隔离测试**
   - 每个测试方法独立
   - 不依赖其他测试的数据状态

## 示例代码

完整示例请参考：
- [MockDbProviderExampleTest.java](src/test/java/com/dlz/test/db/mock/MockDbProviderExampleTest.java)

## 覆盖率贡献

MockDbProvider 帮助提升了 dlz-db-core 模块的代码覆盖率：
- 指令覆盖率：19.88% (3460 / 17403)
- 测试用例数：277 个
- 全部通过 ✅
