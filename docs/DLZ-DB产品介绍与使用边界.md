# DLZ-DB 产品介绍、能力边界与使用指南

> 本文面向第一次接触 DLZ-DB 的开发者、架构师和 AI 助手。
> 基线版本：v8.0.0；当前 Java 源码优先，升级计划中的内容不是当前 API。

## 1. 一句话认识 DLZ-DB

DLZ-DB 是一个面向同步 JDBC 应用的轻量数据库访问框架，提供单表实体 CRUD、动态表 CRUD、条件构造、分页排序、批量操作、原生 SQL、预设 SQL、逻辑删除、本地事务和数据源上下文切换。

它不是试图覆盖所有数据库能力的重型 ORM，而是：

```text
轻量 JDBC 执行层
    + 单表 CRUD Wrapper
    + 类型安全的实体操作
    + 原生 SQL / 预设 SQL
    + 本地事务和数据源上下文
```

## 2. 产品定位

### 2.1 适合解决的问题

- 常规单表 CRUD 代码重复；
- 简单查询不希望创建 Mapper、DAO、XML 和额外 Service 层；
- 希望使用 Lambda 条件和类型安全的实体操作；
- 复杂 SQL 仍然需要手写，但希望统一参数绑定、结果映射、日志和事务；
- 需要在运行时切换或注册数据源；
- 希望在 Spring Boot、Solon 和非 Spring 环境复用相同核心 API；
- 希望 SQL 日志能够定位到业务代码调用位置。

### 2.2 不追求解决的问题

DLZ-DB 不以以下能力为核心目标：

- JPA/Hibernate 风格的完整对象关系映射；
- 自动生成复杂 JOIN、CTE、窗口函数的查询 Builder；
- 自动维护数据库迁移版本；
- 分布式事务；
- 响应式或异步数据库访问；
- 自动读写路由和数据库故障转移；
- 完整存储过程、OUT 参数和厂商特殊 JDBC 类型抽象；
- 业务级缓存、权限和租户模型。

这些场景可以通过 `DB.jdbc`、`DB.sql` 或外部组件完成，但不属于 DLZ-DB 的核心承诺。

## 3. 产品特色与核心设计思路

### 3.1 一套门面，八大能力入口

DLZ-DB 没有把所有操作塞进一种抽象，而是按场景提供不同入口：

| 入口 | 特色 | 典型使用方式 |
|---|---|---|
| `DB.pojo` | 实体类型 + Lambda 字段引用 | 类型安全的单表 CRUD |
| `DB.table` | 字符串表名和字段名 | 动态表、无实体场景、通用后台功能 |
| `DB.jdbc` | 原生 JDBC + `?` 参数 | 一次性 SQL、复杂 SQL、数据库方言 SQL |
| `DB.sql` | 命名参数 + 预设 SQL | 可复用 SQL、集中管理 SQL、复杂查询 |
| `DB.batch` | 批量插入、更新、执行 | 批量写入、同步、ETL |
| `DB.ds` | 数据源注册和线程内切换 | 多数据源、租户路由、报表库 |
| `DB.tx` | 编程式本地事务 | 显式事务边界的业务流程 |
| `DB.config` | 数据库配置和方言配置 | 高级配置 |

简单操作保持短小，复杂操作仍可完整表达，不需要为了迁就 Wrapper 而牺牲 SQL 可读性。其中 `DB.pojo` / `DB.table` / `DB.jdbc` / `DB.sql` 负责数据访问读写，`DB.batch` / `DB.ds` / `DB.tx` / `DB.config` 提供批量、数据源、事务和配置等横切能力。

### 3.2 类型安全与 SQL 控制权并存

实体操作使用 `User::getName` 这样的 Lambda 字段引用，减少字段重命名造成的运行时错误；复杂查询保留手写 SQL 的控制权：

```java
DB.pojo.selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .queryBeanList();

DB.jdbc.list(
    "SELECT u.id, u.name, d.name AS dept_name "
        + "FROM user u LEFT JOIN department d ON d.id = u.dept_id "
        + "WHERE u.status = ?",
    1);
```

两者共用参数绑定、结果映射、日志和事务能力。

### 3.3 严格查询与"取第一条"语义明确

- `queryOne` / `queryBean`：最多允许一条，多条抛 `NonUniqueResultException`；
- `queryFirst` / `queryFirstBean`：允许多条，只返回第一条。

使用 `queryFirst` 时应显式指定排序，以保证"第一条"有稳定含义。

### 3.4 Wrapper 构建与快捷执行分层

`selectWrapper` / `insertWrapper` / `updateWrapper` / `deleteWrapper` 返回可继续构建的 Wrapper；`insert` / `updateById` / `selectById` 等是快捷执行方法。

```text
构建复杂操作 -> xxxWrapper(...).条件(...).排序(...).execute/query...
直接执行     -> DB.pojo.insert(entity)
```

### 3.5 条件 API 原生支持动态查询

条件方法统一提供带开关的重载，当开关为 `false` 时条件不会进入 SQL 条件树：

```java
DB.pojo.selectWrapper(User.class)
    .eq(name != null, User::getName, name)
    .eq(status != null, User::getStatus, status)
    .queryBeanList();
```

该能力覆盖比较、模糊匹配、范围、集合和空值条件。

### 3.6 选项与 SQL 构建拦截 SPI

8.0 已接线的 Option Point 用于控制 INSERT/UPDATE 的 null 字段策略、补入 INSERT 字段、合并 WHERE 条件、删除模式、已删数据查询和查询锁。当前典型能力包括：

- `InsertOption.INCLUDE_NULL` / `UpdateOption.INCLUDE_NULL`：写入空字段；
- `DeleteOption.PHYSICAL`：强制物理删除；
- `LogicDeleteInterceptor`：通过 `WherePoint` / `InsertFieldPoint` / `LogicDeleteValuePoint` 处理逻辑删除条件、插入默认值和 DELETE 改写；
- `SqlBuildInterceptor`：按操作和表名供给默认 `DbOption`，调用点的同 key Option 优先。

`WherePoint` 可用于实现租户或数据权限条件，`InsertFieldPoint` 可补入插入字段。当前没有对外接线数据源路由、UPDATE 字段填充、执行前/后/失败回调等 Option Point，不应将开发路线图写成已有能力。框架也不会自动替业务决定租户模型和权限规则。

### 3.7 方言采用注册机制

应用只需注册实现 `DbDialect` 的自定义方言，框架会根据数据源 JDBC URL 自动匹配，无需修改核心枚举：

```java
DB.config.registerDialect(new OceanBaseDialect());
```

`DB.config` 在完成框架初始化后会变为不可变，因此自定义方言或插件必须在初始化前注册。在自动装配环境中，需把这个时序纳入集成设计，不要在应用已启动后再调用。

内置方言包括：

```text
MySQL / MariaDB
PostgreSQL
Oracle
达梦 DM8
SQLite
H2 / HSQLDB
SQL Server (mssql)
```

方言能力按职责拆分：

```text
DbDialect
├── SqlDialect              SQL 语法、标识符和分页
├── SchemaDialect           建表、字段和表元数据
├── ResultMappingDialect    结果集映射
└── GeneratedKeyDialect     主键回填兜底
```

### 3.8 SQL、参数、结果与调用位置形成完整链路

DLZ-DB 统一处理 `?` 位置参数与 `#{name}` 命名参数、Map/标量/实体/分页结果映射、执行耗时与慢 SQL 日志、调用方信息和批量失败位置。启用 `show-caller` 时调用方信息写入 MDC，是否出现在日志中取决于应用的日志 pattern。复杂 SQL 由开发者掌握，重复的参数绑定、结果转换和排障信息由框架统一提供。

### 3.9 核心 API 与运行框架解耦

核心模块不绑定 Spring。Spring Boot、Solon 通过各自适配层提供数据源、事务和配置接入，而 `DB.pojo`、`DB.table`、`DB.jdbc`、`DB.sql`、`DB.batch`、`DB.ds`、`DB.tx`、`DB.config` 等门面入口保持一致。

### 3.10 数据源切换与事务边界分开表达

`DB.ds.use(...)` 只表示当前线程的数据源上下文切换，`DB.tx.run(...)` 才表示事务边界，两者不隐式绑定：

```java
DB.ds.use("report", () ->
    DB.tx.run(() -> DB.jdbc.list("SELECT * FROM daily_report"))
);
```

### 3.11 显式优先

DLZ-DB 更偏好显式代码而非大量隐式代理。调用代码可以直接看出入口、实体、条件和返回类型。

## 4. 能力地图

| 入口 | 主要职责 | 推荐场景 |
|---|---|---|
| `DB.pojo` | 实体类、Lambda、类型安全 CRUD | 有明确实体类的业务表 |
| `DB.table` | 字符串表名、动态表 CRUD | 动态表名、无实体场景 |
| `DB.jdbc` | `?` 位置参数的原生 JDBC SQL | 一次性 SQL、快速迁移 JdbcTemplate |
| `DB.sql` | `#{name}` 命名参数的预设 SQL | 复杂 SQL、可复用 SQL |
| `DB.batch` | 批量插入、更新、执行 | 批量写入、同步、ETL |
| `DB.ds` | 数据源注册和线程内切换 | 多数据源、租户路由、报表库 |
| `DB.tx` | 编程式本地事务 | 显式事务边界的业务流程 |
| `DB.config` | 数据库配置和方言配置 | 高级配置 |

门面字段名称区分大小写，当前入口是小写 `DB.pojo` / `DB.table` / `DB.jdbc` / `DB.sql` / `DB.batch` / `DB.ds` / `DB.tx` / `DB.config`。

## 5. API 选择规则

**查询 / 写操作入口选择：**

```text
是否是实体类单表操作？
├─ 是 -> DB.pojo
└─ 否
   是否只是动态表名的单表操作？
   ├─ 是 -> DB.table
   └─ 否
      是否是复杂、可复用或需要集中管理的 SQL？
      ├─ 是 -> DB.sql
      └─ 否 -> DB.jdbc
```

**横切能力入口选择：**

```text
需要批量写入？        -> DB.batch
需要在运行时切换数据源？  -> DB.ds.use(...)
需要显式事务边界？      -> DB.tx.run(...)
需要注册方言或全局配置？ -> DB.config
```

| 需求 | 推荐 API |
|---|---|
| 按实体 ID 查询 | `DB.pojo.selectById(...)` |
| 按 Lambda 条件查询 | `DB.pojo.selectWrapper(...)` |
| 动态表名查询 | `DB.table.selectWrapper(...)` |
| 一次性 SELECT | `DB.jdbc.list(...)` 或 `DB.jdbc.one(...)` |
| 复杂 JOIN | `DB.jdbc` 或 `DB.sql` |
| 预设 SQL | `DB.sql` |
| 批量写入 | `DB.batch` 或 Wrapper 的 `batch(...)` |
| 切换数据源 | `DB.ds.use(...)` |
| 本地事务 | `DB.tx.run(...)` |
| 注册自定义方言 | `DB.config.registerDialect(...)` |

## 6. 实体模型

### 6.1 基本实体

```java
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Integer age;
    private Integer status;
}
```

常用注解：

```java
@TableName("user")
@TableId(value = "id", type = IdType.AUTO)
@TableField(value = "user_name")
@TableField(exist = false)
@TableField(select = false)
```

### 6.2 主键策略

| 类型 | 含义 |
|---|---|
| `AUTO` | 数据库自增主键 |
| `SEQ` | 号段/序列策略，具体行为取决于配置和数据库 |
| `INPUT` | 由业务自行提供主键 |
| `ASSIGN_ID` | 框架生成分布式 ID |
| `ASSIGN_UUID` | 框架生成 UUID |

当前直接 CRUD 主要按单列主键设计。复合主键请使用 Wrapper 条件或原生 SQL。

## 7. 查询使用方法

### 7.1 实体查询

```java
User user = DB.pojo
    .selectWrapper(User.class)
    .eq(User::getId, 1L)
    .queryBean();

List<User> users = DB.pojo
    .selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .like(User::getName, "张")
    .orderByDesc(User::getId)
    .queryBeanList();
```

### 7.2 直接按 ID 查询

```java
User user = DB.pojo.selectById(User.class, id);
List<User> users = DB.pojo.selectByIds(User.class, ids);
boolean exists = DB.pojo.existsById(User.class, id);
```

### 7.3 Table 查询

```java
ResultMap user = DB.table
    .selectWrapper("user")
    .eq("id", 1)
    .queryOne();

List<ResultMap> users = DB.table
    .selectWrapper("user")
    .eq("status", 1)
    .queryList();
```

### 7.4 严格单条与非严格第一条

严格模式（0 条返回 `null`，1 条返回该记录，多条抛 `NonUniqueResultException`）：

```java
ResultMap result = DB.table
    .selectWrapper("user")
    .eq("name", "alice")
    .queryOne();
```

非严格模式（0 条返回 `null`，多条按查询顺序返回第一条）：

```java
ResultMap result = DB.table
    .selectWrapper("user")
    .eq("name", "alice")
    .orderByAsc("id")
    .queryFirst();
```

使用非严格模式时建议显式指定排序。

### 7.5 条件构造

```java
DB.pojo.selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .ne(User::getType, "disabled")
    .gt(User::getAge, 18)
    .between(User::getAge, 18, 60)
    .like(User::getName, "张")
    .isNotNull(User::getPhone)
    .in(User::getId, Arrays.asList(1L, 2L, 3L))
    .queryBeanList();
```

常用条件：

```text
eq / ne / gt / ge / lt / le
like / likeLeft / likeRight / notLike
isNull / isNotNull
between / notBetween
in / notIn
ands / ors
sql
```

### 7.6 动态条件

```java
DB.pojo.selectWrapper(User.class)
    .eq(name != null, User::getName, name)
    .eq(status != null, User::getStatus, status)
    .queryBeanList();
```

### 7.7 AND/OR 分组

```java
DB.pojo.selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .ors(or -> or
        .like(User::getName, keyword)
        .like(User::getPhone, keyword))
    .queryBeanList();
```

`ors(...)` 表示"组内条件使用 OR，整个组通常与外层使用 AND"。它不是 MyBatis-Plus 中某些版本的同名语义，迁移时需要重新确认括号逻辑。

### 7.8 自定义 SQL 条件

```java
DB.pojo.selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .sql(
        "EXISTS (SELECT 1 FROM vip WHERE user_id = t.id AND level >= #{level})",
        new JSONMap("level", 3))
    .queryBeanList();
```

SQL 值应使用参数绑定；表名、列名和 SQL 结构必须来自可信代码。

## 8. 查询 API 对照

| API | 模式 | 0 条 | 多条 |
|---|---|---|---|
| `queryOne()` | 严格 Map | `null` | 抛 `NonUniqueResultException` |
| `queryOne(Class)` | 严格类型 | `null` | 抛 `NonUniqueResultException` |
| `queryBean()` | 严格实体 | `null` | 抛 `NonUniqueResultException` |
| `queryFirst()` | 非严格 Map | `null` | 第一条 |
| `queryFirst(Class)` | 非严格类型 | `null` | 第一条 |
| `queryFirstBean()` | 非严格实体 | `null` | 第一条 |
| `queryList()` | 列表 | 空列表 | 全部 |
| `queryBeanList()` | 实体列表 | 空列表 | 全部 |

原生 SQL 也提供对应快捷方法：

```java
DB.jdbc.one(sql, User.class, args);        // 严格
DB.jdbc.first(sql, User.class, args);      // 非严格
DB.sql.one("key.user.find", User.class, params);   // 严格
DB.sql.first("key.user.find", User.class, params); // 非严格
```

## 9. 插入、更新和删除

### 9.1 插入实体

```java
User user = new User();
user.setName("Alice");
user.setStatus(1);

DB.pojo.insert(user);
// 生成的主键会回填到 user
```

### 9.2 Wrapper 插入和 Table 插入

```java
DB.pojo.insertWrapper(User.class)
    .value(user)
    .execute();

int rows = DB.table.insert(
    "user",
    new JSONMap().put("name", "Alice").put("status", 1));

Long id = DB.table.insertWithAutoKey(
    "user",
    new JSONMap().put("name", "Alice"));
```

### 9.3 更新

```java
user.setName("新名称");
DB.pojo.updateById(user);

DB.pojo.updateWrapper(User.class)
    .set(User::getName, "新名称")
    .eq(User::getId, id)
    .execute();
```

默认情况下，实体更新通常忽略 `null` 字段；如确实需要写入 `null`，使用对应的 `DbOption`。

原生字段表达式：

```java
DB.pojo.updateWrapper(User.class)
    .setSql("retry_count = retry_count + 1")
    .eq(User::getId, id)
    .execute();
```

`setSql` 是原生 SQL 表达式入口，不要拼接外部输入。

### 9.4 删除和逻辑删除

```java
DB.pojo.deleteById(User.class, id);

DB.pojo.deleteWrapper(User.class)
    .eq(User::getId, id)
    .execute();
```

如果启用了逻辑删除插件并且实体存在逻辑删除字段，删除操作会被改写为逻辑删除。强制物理删除：

```java
DB.pojo.deleteById(User.class, id, DeleteOption.PHYSICAL);
```

### 9.5 `insertOrUpdateById` 的准确含义

```java
DB.pojo.insertOrUpdateById(user);
DB.table.insertOrUpdate("user", values);
```

含义是：主键为空则 INSERT，主键非空则按主键 UPDATE。这不是数据库原子 Upsert。如需 MySQL `ON DUPLICATE KEY UPDATE`、PostgreSQL `ON CONFLICT` 或 Oracle/DM/SQL Server `MERGE`，请使用 `DB.jdbc` 或 `DB.sql` 编写方言 SQL。

## 10. 批量操作

```java
BatchResult inserted = DB.batch.insert(users, 500);
BatchResult updated = DB.batch.update(users, 500);
```

Table 批量操作：

```java
BatchResult result = DB.batch.insert("user", values, 500);
BatchResult updateResult = DB.batch.update("user", values, 500);
```

JDBC 批量执行：

```java
List<Object[]> params = Arrays.asList(
    new Object[]{"Alice", 1},
    new Object[]{"Bob", 1}
);

BatchResult result = DB.batch.execute(
    "INSERT INTO user(name, status) VALUES(?, ?)",
    params,
    500);
```

`DB.batch` 提供 Bean/Table 的 `insert`、`update` 和原生 SQL 的 `execute`。当前没有 `DB.batch.delete(...)`，批量删除使用 `DB.pojo.deleteByIds(...)`、`DB.table.deleteByIds(...)` 或 Wrapper 的 `in(...)` 条件。

批量 API 返回 `BatchResult`，可判断总数、批次数、已完成批次和失败位置。批量操作不是天然的全有或全无事务，需要整体回滚时应显式放入 `DB.tx.run(...)`。

## 11. 原生 SQL 和预设 SQL

### 11.1 原生 JDBC SQL

```java
List<ResultMap> rows = DB.jdbc.list(
    "SELECT * FROM user WHERE status = ?",
    1);

User user = DB.jdbc.one(
    "SELECT * FROM user WHERE id = ?",
    User.class,
    id);

User first = DB.jdbc.first(
    "SELECT * FROM user WHERE status = ? ORDER BY id",
    User.class,
    1);
```

### 11.2 复杂 JOIN

```java
List<ResultMap> rows = DB.jdbc.list(
    "SELECT u.id, u.name, d.name AS dept_name "
        + "FROM user u LEFT JOIN department d ON d.id = u.dept_id "
        + "WHERE u.status = ? ORDER BY u.id",
    1);
```

JOIN、CTE、UNION、窗口函数、复杂聚合和数据库特有 SQL 都应放在 `DB.jdbc` 或 `DB.sql`。

### 11.3 预设 SQL

```java
List<User> users = DB.sql.list(
    "key.user.findActive",
    User.class,
    new JSONMap("status", 1));
```

配置示例：

```yaml
dlz:
  db:
    sqllist:
      - app/*
    use-db-sql: false
```

`DB.jdbc` 使用 `?` 位置参数；`DB.sql` 使用 `#{name}` 命名参数。不要在两个入口之间混用占位符语法。

## 12. 分页和排序

```java
Page<User> page = DB.pojo
    .selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .orderByDesc(User::getId)
    .page(1, 20)
    .queryBeanPage();
```

原生 SQL 分页：

```java
Page<User> page = DB.jdbc.page(
    "SELECT * FROM user WHERE status = ?",
    PageRequest.of(1, 20),
    User.class,
    1);
```

`DB.sql` 当前未提供快捷 `page` 方法，分页应使用 `selectWrapper(...).page(...).queryPage()`。

分页适合后台列表和中小规模数据集。当前原生 SQL 的自动 count 改写会查找大写的 ` FROM `，因此 `DB.jdbc.count/page` 的 SQL 应使用大写 `FROM` 并保持简单 SELECT 形状。复杂 JOIN、GROUP BY、DISTINCT、UNION 的 count SQL 需要单独确认；深分页和大表导出应考虑基于主键或游标的业务 SQL。

## 13. 数据源管理

```java
DataSourceProperty property = new DataSourceProperty();
property.setName("report");
property.setUrl("jdbc:mysql://localhost:3306/report");
property.setUsername("root");
property.setPassword("password");

DB.ds.setDataSource(property);
```

切换数据源：

```java
List<ResultMap> rows = DB.ds.use("report", () ->
    DB.jdbc.list("SELECT * FROM daily_report")
);
```

`DB.ds` 只负责线程内数据源上下文切换，不负责读写路由、主从延迟判断、故障转移、分布式事务或跨异步线程传播。

## 14. 事务管理

```java
DB.tx.run(() -> {
    DB.pojo.insert(order);
    DB.pojo.insert(orderItem);
});
```

带返回值：

```java
Long id = DB.tx.run(() -> {
    DB.pojo.insert(order);
    return order.getId();
});
```

当前是单数据源本地事务：

- 同线程、同数据源的嵌套调用复用外层事务；
- 异常回滚，正常提交；
- 切换其他数据源后产生独立事务；
- 不提供跨库原子提交、两阶段提交或 XA。

Spring Boot 可以使用 `@Transactional`，Solon 可以使用 `@Tran`。不要混用两种框架的事务注解。

## 15. Spring Boot 和 Solon

Spring Boot 依赖：

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>8.0.0</version>
</dependency>
```

Spring 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app
    username: root
    password: password

dlz:
  db:
    logic-delete-field: deleted
    sqllist: [app/*]
    use-db-sql: false
    table-cache-time: -1 # 秒，-1 表示不过期
    log:
      show-run-sql: true
      show-caller: true
      show-result: false
      slow-sql-threshold: 1000
```

引入 Starter 并配置数据源后自动装配，无需编写或继承 DLZ-DB 配置类。

Solon 依赖：

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-solon-plugin</artifactId>
    <version>8.0.0</version>
</dependency>
```

Solon 数据源：

```java
@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/app");
        config.setUsername("root");
        config.setPassword("password");
        return new HikariDataSource(config);
    }
}
```

插件通过 Solon SPI 加载，但必须由应用或其他插件提供 `DataSource` Bean。`DB.pojo`、`DB.table`、`DB.jdbc`、`DB.sql`、`DB.batch`、`DB.ds`、`DB.tx`、`DB.config` 等 core 门面 API 在两种框架中保持一致。

## 16. 逻辑删除

逻辑删除通过 `LogicDeleteInterceptor` 插件实现，默认字段为 `deleted`，具体以应用配置为准。

```java
DB.pojo.deleteById(User.class, id, DeleteOption.PHYSICAL);
```

上面的示例强制物理删除。默认删除是否逻辑化，取决于插件是否启用、表中是否存在配置的逻辑删除字段，以及字段值约定。

逻辑删除不等同于完整的数据权限系统。租户隔离、用户权限、组织范围等条件需要通过业务条件或自定义插件实现。

## 17. 乐观锁

当前没有自动识别 `version` 字段和自动抛出乐观锁异常。可以使用条件更新自行实现：

```java
int rows = DB.pojo.updateWrapper(Order.class)
    .set(Order::getStatus, "PAID")
    .set(Order::getVersion, version + 1)
    .eq(Order::getId, orderId)
    .eq(Order::getVersion, version)
    .execute();

if (rows != 1) {
    throw new IllegalStateException("记录已被其他请求修改");
}
```

## 18. 数据库支持边界

内置方言覆盖：

```text
MySQL / MariaDB
PostgreSQL
Oracle
达梦 DM8
SQLite
H2 / HSQLDB
SQL Server (mssql)
```

需要区分三个层级：

| 能力 | 说明 |
|---|---|
| 原生 SQL | 主要由用户 SQL 和 JDBC 驱动决定，可使用数据库特有语法 |
| 基础 CRUD | 主要数据库的常规单表 CRUD、条件、排序和参数绑定 |
| 自动建表/加字段 | 依赖数据库 Helper，不是完整数据库迁移工具 |

生产环境建议：

- 使用 Flyway、Liquibase 或其他正式工具管理 DDL；
- 关闭 `helper.auto-update`；
- 在目标数据库上实际验证方言 SQL；
- 不要仅因为内置方言中存在某个数据库类型，就认为所有自动建表能力完全等价。

## 19. 安全边界

参数值使用绑定参数：

```java
DB.jdbc.list("SELECT * FROM user WHERE name = ?", userName);
DB.sql.list("key.user.find", new JSONMap("name", userName));
```

不要拼接用户输入：

```java
String sql = "SELECT * FROM user WHERE name = '" + userName + "'";
```

以下内容属于 SQL 结构，必须来自可信代码：

- 表名；
- 列名；
- 排序字段；
- `setSql` 表达式；
- `.sql(...)` 片段；
- `in("id", "sql:...")` 子查询。

Wrapper 的 UPDATE/DELETE 必须带明确的业务条件。当最终 WHERE 完全为空时，当前构建器会改成 `WHERE false`；但逻辑删除插件注入的 `deleted = 0` 也是 WHERE 条件，无业务条件的操作仍可能命中所有未删除数据。不应将这个兜底视为防误更新/误删的强保证。

## 20. 性能和资源边界

DLZ-DB 底层使用 JDBC，常规单次 CRUD 的主要耗时通常来自数据库、网络和连接池，而不是 Wrapper 本身。

使用时仍需注意：

- 不要在循环中逐条查询造成 N+1；
- 大批量写入使用 `DB.batch`；
- 大结果集不要一次性加载为 `List`；
- 深分页考虑基于主键的业务分页；
- 复杂分页要确认 count SQL 的正确性和性能；
- 生产环境谨慎开启结果日志；
- `show-caller` 会采集调用栈并写入 MDC，完整 SQL 日志会展开参数；两者都需要结合开销、日志量和 pattern 配置评估。

当前没有流式、Cursor、响应式或异步数据库 API，因此不适合作为大规模导出、ETL 或 R2DBC 访问层的直接替代品。

## 21. 明确不属于当前核心能力的场景

以下场景可以通过原生 SQL 或外部组件完成，但不应期待 Wrapper 自动完成：

- JOIN 后自动组装一对多/多对多对象；
- CTE、UNION、窗口函数的类型安全 Builder；
- 复合主键自动 CRUD；
- 依据唯一键冲突的跨数据库原子 Upsert；
- 分布式事务、XA、两阶段提交；
- 响应式和异步数据库访问；
- 大结果集流式读取；
- 完整存储过程和 OUT 参数抽象；
- 自动读写路由、主从延迟判断、故障转移；
- 自动乐观锁和审计字段；
- 完整数据库迁移管理；
- 业务级缓存、权限和租户隔离模型。

## 22. 常见场景示例

### 22.1 管理后台列表

```java
Page<User> page = DB.pojo
    .selectWrapper(User.class)
    .eq(status != null, User::getStatus, status)
    .like(name != null, User::getName, name)
    .orderByDesc(User::getId)
    .page(pageNo, pageSize)
    .queryBeanPage();
```

### 22.2 复杂报表

```java
List<ResultMap> rows = DB.sql.list(
    "key.report.monthlySummary",
    new JSONMap("month", month));
```

### 22.3 租户数据源路由

```java
String dataSource = tenantDataSourceResolver.resolve(tenantId);

return DB.ds.use(dataSource, () ->
    DB.pojo.selectWrapper(User.class)
        .eq(User::getTenantId, tenantId)
        .queryBeanList());
```

数据源切换和租户条件是两件事。切换数据源不会自动增加租户条件。

### 22.4 批量写入事务

```java
DB.tx.run(() -> {
    BatchResult result = DB.batch.insert(users, 500);
    if (!result.isSuccess()) {
        throw new IllegalStateException("批量写入失败");
    }
});
```

## 23. 给 AI 的最小使用规则

1. 有实体类优先使用 `DB.pojo`；
2. 动态表名使用 `DB.table`；
3. 一次性原生 SQL 使用 `DB.jdbc`；
4. 复杂或复用 SQL 使用 `DB.sql`；
5. `xxxWrapper` 表示构建 Wrapper，不是直接执行；
6. `queryOne/queryBean` 是严格模式，多条抛 `NonUniqueResultException`；
7. `queryFirst/queryFirstBean` 是非严格模式，多条取第一条；
8. 使用非严格模式时显式指定排序；
9. `insertOrUpdateById` 不是数据库原子 Upsert；
10. `DB.ds.use` 只切换数据源，不开启事务；
11. `DB.tx.run` 只提供单数据源本地事务；
12. JOIN、CTE、UNION、窗口函数和复杂聚合使用 `DB.jdbc` 或 `DB.sql`；
13. SQL 值使用参数绑定，不拼接用户输入；
14. 表名、字段名和 SQL 片段必须来自可信代码；
15. 复合主键、关系对象自动映射、流式查询和分布式事务不属于当前核心能力。

## 24. 最终定位

DLZ-DB 最适合：

- 单体应用和微服务中的普通业务 CRUD；
- 中后台管理系统；
- 以单表操作为主的业务系统；
- 需要直接写 SQL 的报表和数据查询服务；
- 需要在 Spring、Solon 或非 Spring 环境复用数据库访问代码的项目；
- 需要动态数据源和显式本地事务的应用；
- 需要轻量、可读、容易跟踪源码的数据库访问场景。

DLZ-DB 不适合单独承担：

- 复杂领域对象关系映射；
- 数据库迁移平台；
- 分布式事务协调器；
- 响应式数据库驱动层；
- 完整数据库治理平台。

选择 DLZ-DB 的核心理由不是"它能代替所有数据库工具"，而是：

> 简单的单表操作足够简单，复杂 SQL 可以直接表达，框架不会为了抽象而遮挡数据库本身。
