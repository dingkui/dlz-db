# DLZ-DB 8.0 功能与 API 清单

**对齐版本**：8.0.0
**核对日期**：2026-08-28
**事实来源**：`dlz-db-core`、Spring Boot Starter、Solon Plugin 的当前源码

> 本页是公共用法索引，不罗列 `com.dlz.db.internal.*` 中的实现细节。

## 1. 统一入口

| 入口 | 用途 |
|---|---|
| `DB.pojo` | 基于实体类和 Lambda 的 CRUD，默认首选 |
| `DB.table` | 基于表名和 `JSONMap`/`ResultMap` 的动态表操作 |
| `DB.jdbc` | 使用 `?` 占位符的一次性 SQL |
| `DB.sql` | 使用 `#{key}` 参数的预设 SQL/Key-SQL |
| `DB.batch` | Pojo、表数据或原生 SQL 批处理 |
| `DB.ds` | 数据源注册、切换、查询和移除 |
| `DB.tx` | 当前或指定数据源上的编程式事务 |
| `DB.config` | 插件、方言、预设 SQL 和底层执行器配置 |

## 2. Pojo API

### 2.1 直接 CRUD

```java
User inserted = DB.pojo.insert(user);
User saved = DB.pojo.insertOrUpdateById(user);
User one = DB.pojo.selectById(User.class, id);
List<User> list = DB.pojo.selectByIds(User.class, ids);
int updated = DB.pojo.updateById(user);
int deleted = DB.pojo.deleteById(User.class, id);
boolean exists = DB.pojo.existsById(User.class, id);
```

- `selectByIds` 和 `deleteByIds` 支持 `Collection<?>` 或 CSV 字符串。
- `add(entity)` 和 `save(entity)` 为已废弃别名；新代码使用 `insert` 和 `insertOrUpdateById`。
- 写入可传 `DbOption...`，如 `InsertOption.INCLUDE_NULL`、`UpdateOption.INCLUDE_NULL`、`DeleteOption.PHYSICAL`。

### 2.2 Pojo Wrapper

```java
PojoQuery<User> query = DB.pojo.selectWrapper(User.class);
PojoInsert<User> insert = DB.pojo.insertWrapper(user);
PojoUpdate<User> update = DB.pojo.updateWrapper(User.class);
PojoDelete<User> delete = DB.pojo.deleteWrapper(User.class);
```

常用查询：

```java
User one = DB.pojo.selectWrapper(User.class)
    .select(User::getId, User::getName)
    .eq(User::getStatus, 1)
    .queryBean();

Page<User> page = DB.pojo.selectWrapper(User.class)
    .orderByDesc(User::getCreateTime)
    .page(1, 20)
    .queryBeanPage();
```

常用写操作：

```java
int updated = DB.pojo.updateWrapper(User.class)
    .set(User::getStatus, 2)
    .eq(User::getId, id)
    .execute();

int deleted = DB.pojo.deleteWrapper(User.class)
    .eq(User::getId, id)
    .execute();

int physicalDeleted = DB.pojo.deleteWrapper(User.class)
    .eq(User::getId, id)
    .physical();
```

Wrapper 写操作应始终带明确业务条件。当最终 WHERE 为空时，当前构建器会生成 `WHERE false`；但逻辑删除插件注入的 `deleted = 0` 也会被视为 WHERE，所以这不是“无业务条件一定拒绝”的强安全保证。

## 3. Table API

### 3.1 直接 CRUD

| 方法 | 返回值 |
|---|---|
| `insert(table, values, options...)` | 影响行数 `int` |
| `insertWithAutoKey(table, values, options...)` | 自增主键 `Long` |
| `insertOrUpdate(table, values, options...)` | 影响行数 `int` |
| `selectById(table, id, options...)` | `ResultMap` |
| `selectByIds(table, ids)` | `List<ResultMap>` |
| `updateById(table, values, options...)` | 影响行数 `int` |
| `deleteById/deleteByIds` | 影响行数 `int` |

### 3.2 Table Wrapper

```java
List<ResultMap> rows = DB.table.selectWrapper("user")
    .select("id", "name")
    .eq("status", 1)
    .queryList();

int updated = DB.table.updateWrapper("user")
    .set("status", 2)
    .eq("id", id)
    .execute();
```

可用 Wrapper：`TableQuery`、`TableInsert`、`TableUpdate`、`TableDelete`。`insertWrapper/updateWrapper` 还提供 `.batch(...)` 批处理。

## 4. JDBC 与预设 SQL

### 4.1 JDBC

```java
ResultMap one = DB.jdbc.one("SELECT * FROM user WHERE id = ?", id);
List<User> list = DB.jdbc.list(
    "SELECT * FROM user WHERE status = ?", User.class, 1);
long count = DB.jdbc.count("SELECT * FROM user WHERE status = ?", 1);
Page<User> page = DB.jdbc.page(
    "SELECT * FROM user WHERE status = ?",
    PageRequest.of(1, 20), User.class, 1);
int affected = DB.jdbc.execute("UPDATE user SET status = ? WHERE id = ?", 2, id);
```

- `one` 是严格单条，`first` 是取第一条。
- 链式入口是 `selectWrapper(...)` 和 `executeWrapper(...)`，对应类是 `JdbcSelect` 和 `JdbcExecute`。
- `JdbcExecute.executeAndReturnId()` 返回自增主键 `Long`。
- `count()` 和 `page()` 会自动从 SELECT 改写 count SQL；当前原生 SQL 改写要求存在大写 ` FROM `，复杂 `GROUP BY` / `DISTINCT` / `UNION` 应使用经过验证的显式 SQL。

### 4.2 预设 SQL

```java
List<User> users = DB.sql.selectWrapper("key.user.findActive")
    .addPara("status", 1)
    .queryList(User.class);

int affected = DB.sql.execute(
    "key.user.disable", new JSONMap("id", id));
```

- 链式入口是 `SqlQuery` 和 `SqlExecute`。
- 直接入口提供 `one`、`first`、`list`、`count`、`execute`。
- `DB.sql` 目前没有与 `DB.jdbc.page(...)` 对称的直接分页方法；分页时使用 `selectWrapper(...).page(...).queryPage(...)`。
- 类路径预设 SQL 从 `classpath*:sql/<sqllist>.sql` 读取，默认 `sqllist` 为 `app/*`。文件扩展名是 `.sql`，文件内容使用 `<sqlList>` XML 结构。

## 5. 条件、排序与分页

### 5.1 条件

常用条件包括：

- `eq/ne/gt/ge/lt/le`
- `isNull/isNotNull`
- `in/notIn`
- `between/notBetween`
- `like/likeLeft/likeRight/notLike`
- `sql(sql, JSONMap)` 自定义命名参数片段
- `ands(consumer)` 和 `ors(consumer)` 复合条件

```java
List<User> rows = DB.pojo.selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .ors(o -> o.like(User::getName, keyword)
               .like(User::getMobile, keyword))
    .queryBeanList();
```

`ors(...)` 表示 lambda 内部用 OR 连接，整组与外层仍按 AND 连接；它不等同于 MyBatis-Plus 的同名语义。

### 5.2 排序与分页

```java
Page<User> page = DB.pojo.selectWrapper(User.class)
    .orderByAsc(User::getName)
    .orderByDesc(User::getCreateTime)
    .page(1, 20)
    .queryBeanPage();
```

- Wrapper 支持 `page(Page)`、`page(current, size, Order...)`、`limit(size)` 和 `sort(...)`。
- `Page<T>` 字段包括 `current`、`size`、`total`、`pages`、`records`，也提供 `pageNo()`、`pageSize()`、`hasNext()` 等简洁方法。
- 当前 `Page.setSize` 会把页大小上限限制为 5000。

## 6. 查询返回类型

| 方法 | 返回类型 |
|---|---|
| `queryOne/queryFirst` | `ResultMap` |
| `queryList/queryPage` | `List<ResultMap>` / `Page<ResultMap>` |
| `queryOne(Class)/queryFirst(Class)` | 指定 Bean |
| `queryList(Class)/queryPage(Class)` | 指定 Bean 列表/分页 |
| `queryBean/queryFirstBean` | Pojo Wrapper 绑定的实体类 |
| `queryBeanList/queryBeanPage` | Pojo 列表/分页 |
| `queryStr/Long/Int/Double` | 单列单值 |
| `queryStrList/queryLongList/queryIntList/queryDoubleList` | 单列列表 |
| `count` | `long` |

`queryOne/queryBean` 在多条时抛出非唯一结果异常；`queryFirst/queryFirstBean` 只取第一条。

## 7. 批量操作

```java
BatchResult r1 = DB.batch.insert(users);
BatchResult r2 = DB.batch.update(users, 500);
BatchResult r3 = DB.batch.insert("user", values, 500);
BatchResult r4 = DB.batch.execute(sql, params, 500);

if (!r1.isSuccess()) {
    log.warn("batch status={}, failed={}", r1.status(), r1.failedPositions());
}
```

`BatchResult` 对外提供 `totalItems()`、`batchSize()`、`batchCount()`、`completedBatches()`、`knownAffectedRows()`、`unknownAffectedRows()`、`failedPositions()`、`status()`、`cause()` 和 `isSuccess()`。

## 8. 事务与数据源

```java
DB.ds.use("slave", () -> DB.pojo.selectById(User.class, id));

DB.tx.run(() -> {
    DB.pojo.insert(order);
    DB.pojo.insert(orderItem);
});

DB.tx.run("slave", () -> {
    // 在 slave 数据源上开启事务
});
```

- `DB.ds.use(...)` 只切换数据源，不自动开启事务。
- `DB.ds` 还提供 `setDefaultDataSource`、`setDataSource`、`removeDataSource`、`testConnection`、`getAllDataSourceNames` 等方法。
- 多数据源切换不是分布式事务。

## 9. 实体注解与操作选项

注解位于 `com.dlz.db.core.anno`：

- `@TableName(value, comment)`
- `@TableId(value, type)`
- `@TableField(value, exist, select, comment)`
- `IdType.AUTO/SEQ/INPUT/ASSIGN_ID/ASSIGN_UUID`

内置操作选项：

| 操作 | 选项 |
|---|---|
| 插入 | `InsertOption.IGNORE_NULL` / `INCLUDE_NULL` |
| 更新 | `UpdateOption.IGNORE_NULL` / `INCLUDE_NULL` |
| 删除 | `DeleteOption.LOGIC` / `PHYSICAL` |
| 查询 | `SelectOption.INCLUDE_DELETED` / `FOR_UPDATE`（用于接收 `DbOption...` 的直接查询，如 `selectById`） |

## 10. 配置、日志和框架集成

`dlz.db` 核心配置包括：

- `db-support`、`blob-charset`
- `sqllist`、`sql`、`use-db-sql`
- `logic-delete-field`
- `helper.package-name`、`helper.auto-update`
- `log.show-result`、`log.show-run-sql`、`log.show-caller`、`log.slow-sql-threshold`

Spring Boot Starter 自动装配 `SpringDlzDbAutoConfiguration`，同时兼容 Spring Boot 2 与 3 的自动装配发现方式。Solon 通过 `DlzDbSolonPlugin` 加载，初始化时必须能从容器获得 `DataSource` Bean。

`DB.config` 的插件、方言、预设 SQL 和底层执行器设置只能在框架初始化前修改；初始化完成后继续调用这些配置方法会失败。

## 11. JDK 基线

- 库主体以 Java 8 为编译目标。
- Spring Boot 2 和 Solon Demo 目标 Java 8。
- Spring Boot 3 Demo 需要 JDK 17。
- 要一次性聚合验证所有模块和 Demo，建议使用 JDK 17 运行 Maven。

## 12. 公共边界

新的业务代码不应依赖 `com.dlz.db.internal.*`。方言扩展使用 `com.dlz.db.dialect.DbDialect` 和 `DialectRegistry`，SQL 构建拦截使用 `SqlBuildInterceptor`，底层框架适配则以 `ISqlExecutor`、`ITxExecutor` 等 core 抽象为边界。未在公共包中承诺的类名不应出现在面向业务用户的教程中。
