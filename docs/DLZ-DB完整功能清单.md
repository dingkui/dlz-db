# DLZ-DB 完整功能清单

**版本：** v7.0.0  
**生成日期：** 2026-05-06  
**入口类：** `com.dlz.db.modal.DB`

---

## 一、DB 入口概览

DLZ-DB 提供 7 个静态入口，覆盖所有数据库操作场景：

| 入口 | 职责 | 适用场景 |
|------|------|----------|
| **DB.Pojo** | 基于实体类的类型安全操作 | 推荐首选，IDE 自动补全，重构安全 |
| **DB.Table** | 基于表名的动态操作 | 无实体类或动态表名场景 |
| **DB.Jdbc** | 原生 SQL 操作（? 占位符） | 简单 SQL，快速原型开发 |
| **DB.Sql** | 预设 SQL 操作（#{key} 占位符） | 复杂 SQL，SQL 集中管理 |
| **DB.Batch** | 批量操作 | 批量插入、更新、删除 |
| **DB.Dynamic** | 动态数据源切换 | 读写分离、多数据源 |
| **DB.Tx** | 事务管理 | 编程式事务 |

---

## 二、DB.Pojo - 实体类操作（推荐）

### 2.1 查询操作

#### 基础查询（→ 构建器，需调用 queryBean/List/Page 执行）
```java
// 按 Class 查询
PojoQuery<T> select(Class<T> clazz)

// 按条件 Bean 查询
PojoQuery<T> select(T conditionBean)
```

#### 快捷查询方法（直接执行）
```java
// 根据 ID 查询单条
T selectById(Class<T> clazz, Object id)

// 根据 IDs 查询多条
List<T> selectByIds(Class<T> clazz, Object ids)  // ids: "1,2,3" 或 List
```

### 2.2 插入操作（直接执行）

```java
// 插入单条（自动回填主键）
int insert(T bean)

// 批量插入（默认批次 1000）
boolean insertBatch(List<T> beans)
boolean insertBatch(List<T> beans, int batchSize)

// 插入或更新（根据 ID 判断）
int insertOrUpdate(T obj)

// 根据 ID 更新（只更新非 ID 字段）
int updateById(T obj)
```

### 2.3 更新操作（→ 构建器，需调用 .execute()）

```java
// 按 Class 创建更新器
PojoUpdate<T> update(Class<T> clazz)

// 按对象创建更新器（自动设置所有非 null 且非 ID 字段）
PojoUpdate<T> update(T value)

// 按对象创建更新器（自定义忽略规则）
PojoUpdate<T> update(T value, Function<String, Boolean> ignore)
```

### 2.4 删除操作

#### 链式删除（→ 构建器，需调用 .execute()）
```java
// 按 Class 创建删除器
PojoDelete<T> delete(Class<T> clazz)

// 按条件对象创建删除器
PojoDelete<T> delete(T condition)
```

#### 快捷删除（直接执行）
```java
// 根据 ID 删除单条
int deleteById(Class<T> clazz, Object id)

// 根据 IDs 删除多条
int deleteByIds(Class<T> clazz, String ids)     // CSV 字符串
int deleteByIds(Class<T> clazz, List<?> ids)    // List
```

### 2.5 查询结果方法（PojoQuery）

#### Bean 结果
```java
T queryBean()                    // 单条 Bean
List<T> queryBeanList()          // Bean 列表
Page<T> queryBeanPage()          // Bean 分页
```

#### ResultMap 结果
```java
ResultMap queryOne()             // 单条 Map
List<ResultMap> queryList()      // Map 列表
Page<ResultMap> queryPage()      // Map 分页
```

#### 指定类型结果
```java
T queryOne(Class<T> clazz)       // 指定类型单条
List<T> queryList(Class<T> clazz)  // 指定类型列表
Page<T> queryPage(Class<T> clazz)  // 指定类型分页
```

#### 标量结果
```java
String queryStr()                // 单值字符串
List<String> queryStrList()      // 字符串列表

Long queryLong()                 // 单值 Long
List<Long> queryLongList()       // Long 列表

Integer queryInt()               // 单值 Integer
List<Integer> queryIntList()     // Integer 列表

Double queryDouble()             // 单值 Double
List<Double> queryDoubleList()   // Double 列表
```

#### 统计
```java
int count()                      // 记录数
```

### 2.6 条件构造方法

#### 比较操作
```java
eq(field, value)        // = 
ne(field, value)        // <>
gt(field, value)        // >
ge(field, value)        // >=
lt(field, value)        // <
le(field, value)        // <=
```

#### NULL 判断
```java
isNull(field)              // IS NULL
isNotNull(field)             // IS NOT NULL
```

#### 范围操作
```java
in(field, values)       // IN (支持 List/CSV/子查询)
notIn(field, values)       // NOT IN
between(field, min, max)     // BETWEEN
notBetween(field, min, max)     // NOT BETWEEN
```

#### 模糊查询
```java
like(field, value)        // LIKE '%value%'
likeLeft(field, value)        // LIKE '%value'
likeRight(field, value)        // LIKE 'value%'
notLike(field, value)        // NOT LIKE '%value%'
```

#### 逻辑组合
```java
or(Consumer<Condition> consumer)   // OR 嵌套
and(Consumer<Condition> consumer)  // AND 嵌套
```

#### 排序
```java
orderByAsc(field)       // 升序
orderByDesc(field)      // 降序
```

#### 分页
```java
page(Page page)         // 设置分页
```

#### 字段选择
```java
columns(fields...)      // 指定查询字段
```

#### 自定义 SQL
```java
sql(sqlFragment, params)  // 自定义 SQL 片段
```

#### 条件判断
```java
eq(condition, field, value)  // 条件为 true 时才添加
```

---

## 三、DB.Table - 表名操作

### 3.1 基础操作（→ 构建器，需调用 .execute() 或查询方法）

```java
// 查询 → TableQuery，需调用 queryOne/List/Page
TableQuery select(String tableName)

// 插入 → TableInsert，需调用 .execute()
TableInsert insert(String tableName)

// 更新 → TableUpdate，需调用 .execute()
TableUpdate update(String tableName)

// 删除 → TableDelete，需调用 .execute()
TableDelete delete(String tableName)
```

### 3.2 查询结果方法

与 DB.Pojo 相同，支持：
- `queryOne()` / `queryList()` / `queryPage()`
- `queryOne(Class)` / `queryList(Class)` / `queryPage(Class)`
- `queryStr()` / `queryLong()` / `queryInt()` / `queryDouble()` 及其 List 版本
- `count()`

### 3.3 条件构造方法

与 DB.Pojo 相同，但使用字符串字段名而非 Lambda。

---

## 四、DB.Jdbc - 原生 SQL 操作

### 4.1 查询操作（→ 构建器，需调用 queryOne/List/Page）

```java
// 查询（? 占位符）
JdbcQuery select(String sql, Object... para)
```

### 4.2 写操作（直接执行）

```java
// 插入
int insert(String sql, Object... para)

// 更新
int update(String sql, Object... para)

// 删除
int delete(String sql, Object... para)

// 通用执行
int execute(String sql, Object... para)
```

### 4.3 查询结果方法

与 DB.Pojo 相同，支持所有查询方法和标量方法。

### 4.4 分页支持

```java
select(sql, params).page(Page.build(1, 10)).queryList()
```

---

## 五、DB.Sql - 预设 SQL 操作

### 5.1 基础操作

```java
// 查询 → SqlQuery，需调用 queryOne/List/Page（#{key} 占位符）
SqlQuery select(String sqlKey)

// 以下返回 SqlExecute，需调用 .execute() 执行
SqlExecute insert(String sqlKey)

SqlExecute update(String sqlKey)

SqlExecute delete(String sqlKey)

SqlExecute executer(String sqlKey)

// 直接执行（无需 .execute()）
int execute(String sqlKey)
```

### 5.2 参数设置

```java
select("key.xxx")
    .addPara("name", "张三")
    .addPara("age", 25)
    .queryList()
```

### 5.3 分页支持

```java
select("key.xxx")
    .setPage(Page.build(1, 10))
    .page(User.class)
```

### 5.4 查询结果方法

与 DB.Pojo 相同，支持所有查询方法和标量方法。

---

## 六、DB.Batch - 批量操作

### 6.1 批量插入

```java
// 批量插入（默认批次大小 1000）
boolean insert(List<T> beans)

// 批量插入（指定批次大小）
boolean insert(List<T> beans, int batchSize)
```

### 6.2 批量更新（Pojo）

```java
// 批量更新（默认批次大小 1000）
boolean update(List<T> beans)

// 批量更新（指定批次大小）
boolean update(List<T> beans, int batchSize)
```

### 6.3 批量更新（SQL）

```java
// 批量更新（默认批次大小 1000）
boolean update(String sql, List<Object[]> params)

// 批量更新（指定批次大小）
boolean update(String sql, List<Object[]> params, int batchSize)
```

---

## 七、DB.Dynamic - 动态数据源

### 7.1 数据源管理

```java
// 注册数据源
boolean setDataSource(DataSourceProperty properties)

// 设置默认数据源
boolean setDefaultDataSource(DataSource dataSource)

// 删除数据源
boolean removeDataSource(String name)
```

### 7.2 数据源切换

```java
// 切换数据源执行（带返回值）
T use(String name, Supplier<T> action)

// 切换数据源执行（无返回值）
void use(String name, Runnable action)
```

### 7.3 使用示例

```java
// 读写分离
List<User> users = DB.Dynamic.use("slave", () ->
    DB.Pojo.select(User.class)
        .eq(User::getStatus, 1)
        .queryBeanList()
);

// 切换到报表库
DB.Dynamic.use("report", () -> {
    DB.Pojo.insert(reportData);
});
```

---

## 八、DB.Tx - 事务管理

### 8.1 编程式事务

```java
// 默认数据源事务（无返回值）
void run(Runnable action)

// 默认数据源事务（带返回值）
T run(Supplier<T> action)

// 指定数据源事务（无返回值）
void run(String dataSourceName, Runnable action)

// 指定数据源事务（带返回值）
T run(String dataSourceName, Supplier<T> action)
```

### 8.2 使用示例

```java
// 基本事务
DB.Tx.run(() -> {
    DB.Pojo.insert(user);
    DB.Pojo.insert(userLog);
});

// 带返回值
Long userId = DB.Tx.run(() -> {
    DB.Pojo.insert(user);
    return user.getId();
});

// 指定数据源
DB.Tx.run("slave", () -> {
    DB.Pojo.insert(user);
});
```

### 8.3 声明式事务

#### Spring Boot
```java
@Transactional
public void createUser() {
    DB.Pojo.insert(user);
}
```

#### Solon
```java
@Tran
public void createUser() {
    DB.Pojo.insert(user);
}
```

---

## 九、功能对比总结

### 9.1 各入口能力矩阵

| 功能 | Pojo | Table | Jdbc | Sql | Batch | Dynamic | Tx |
|------|------|-------|------|-----|-------|---------|----|
| 查询 | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| 插入 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 更新 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 删除 | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| 分页 | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| 批量 | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| 数据源切换 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 事务 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Lambda 支持 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 类型安全 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

### 9.2 推荐使用场景

| 场景 | 推荐入口 | 原因 |
|------|---------|------|
| 日常 CRUD | DB.Pojo | 类型安全，IDE 提示好 |
| 动态表名 | DB.Table | 无需定义实体类 |
| 简单 SQL | DB.Jdbc | 快速原型开发 |
| 复杂 SQL | DB.Sql | SQL 集中管理，易维护 |
| 批量操作 | DB.Batch | 性能好，自动分批 |
| 读写分离 | DB.Dynamic | 灵活切换数据源 |
| 事务控制 | DB.Tx | 编程式事务，精确控制 |

---

## 十、高级特性

### 10.1 逻辑删除

当实体类包含 `isDeleted` 字段时，自动启用逻辑删除：
- 查询自动添加 `WHERE is_deleted = 0`
- 删除转换为 `UPDATE is_deleted = 1`
- 更新自动添加 `WHERE is_deleted = 0`

### 10.2 预设 SQL

支持三种方式定义预设 SQL：
1. XML 文件：`resources/sql/app/*.xml`
2. 数据库表：`sys_sql` 表
3. 配置文件：`application.yml` 中的 `sqllist`

### 10.3 自动建表

```yaml
dlz:
  db:
    helper:
      auto-update: true
      package-name: com.example.entity
```

启动时自动扫描实体类并创建/更新表结构。

### 10.4 SQL 日志

```yaml
dlz:
  db:
    log:
      show-run-sql: true      # 显示完整 SQL
      show-caller: true       # 显示调用位置
      show-result: false      # 显示查询结果
      slow-sql-threshold: 1000 # 慢 SQL 阈值（毫秒），0 表示不启用
```

日志级别配置：
```xml
<logger name="com.dlz.db.util.DbLogUtil" level="trace" additivity="false"/>
```

- **TRACE**：调用链（框架内部执行路径）
```
TRACE [...] ...DbLogUtil:91 < DbLogUtil:117 < ISqlExecutor:82 < SpringSqlExecutorAdapter:43 < ... < IExecutorQuery:66
```
- **INFO**：`caller:` 行（**核心特色**，IDE 中可点击跳转，直接定位到调用 SQL 的业务代码位置）
```
INFO [...] ...DbLogUtil:128 caller:(UserController.java:15) getUser 12ms sql:SELECT * FROM user WHERE id = 1
```

**慢 SQL 监控：**
- 当 SQL 执行时间超过 `slow-sql-threshold` 时，以 **WARN** 级别输出
- 默认值为 0（不启用）
- 建议生产环境设置为 500-1000ms

---

## 十一、功能完整性

### 11.1 已实现的功能

✅ **全部核心功能已实现**

- 基础 CRUD 操作
- 查询方法（Bean、ResultMap、标量、分页）
- 条件构造器（比较、范围、模糊、逻辑组合）
- 批量操作（插入、更新）
- 多数据源管理
- 事务管理（编程式 + 声明式）
- SQL 日志与慢 SQL 监控 ✅
- 逻辑删除
- 预设 SQL
- 自动建表

---

## 十二、总结

DLZ-DB 提供了完整的数据库操作能力，核心特点：

✅ **7 大入口** - 覆盖所有使用场景  
✅ **链式 API** - 流畅的编码体验  
✅ **类型安全** - Lambda 表达式支持  
✅ **零样板代码** - 无需 Mapper/XML  
✅ **多框架支持** - Spring Boot + Solon  
✅ **轻量级** - 核心模块仅依赖 JDBC  

**推荐使用顺序：** DB.Pojo > DB.Sql > DB.Table > DB.Jdbc

---

**文档版本：** v1.0  
**最后更新：** 2026-05-06
