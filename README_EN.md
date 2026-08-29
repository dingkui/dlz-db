# DLZ-DB

> **A lightweight Java database framework that makes SQL as direct as writing local code.**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Runtime](https://img.shields.io/badge/Runtime-Java%208+-green.svg)](https://www.oracle.com/java/)
[![Build JDK](https://img.shields.io/badge/Build%20JDK-17+-blue.svg)](https://www.oracle.com/java/)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-8.0.0-orange.svg)](https://central.sonatype.com/artifact/top.dlzio/dlz-db-core)

```java
List<User> users = DB.pojo.selectWrapper(User.class)
        .eq(User::getStatus, 1)
        .like(User::getName, "Zhang")
        .orderByDesc(User::getCreateTime)
        .queryBeanList();
```

No required Mapper interfaces or XML, and no mandatory Service layer. Use a Service when transactions, reuse, or business orchestration require one.

---

## Version Notes

Current version v8.0.0 (initial open-source version v6.6.4).

This project didn't start from scratch. It began accumulating around **2009**, took shape around **2014**, and has been used as an internal database operation toolkit in the company for over a decade. During this period, it has been adopted by dozens of internal projects, adapted to various legacy systems, different open-source framework combinations, and various strange version mixes.

In **2024**, we decided to open-source it. We did extensive refactoring and cleanup—stripping internal dependencies, cleaning up historical baggage, refining general capabilities, and finally released the first public version **6.6.4**.

That's why the version number doesn't start from 1.0—what you see is a tool that has been running for over a decade, validated by dozens of projects, not a brand-new project just starting out.

---

## Why Another Database Framework?

If you've written Java for a while, you've likely experienced these:

- Creating 6 files and 200 lines of code for a simple CRUD operation
- Spending half an hour globally searching for a SQL log in production without finding who executed it
- Writing `@DS("slave")` in annotations—new tenant dynamic access? Sorry, string hardcoding
- MyBatis exceptions with 15 layers of proxies in the stack, unable to see which line of business code failed
- Query returns a JSON field, having to manually `JSON.parseObject(...)` layer by layer

DLZ-DB aims to solve not "the lack of a framework," but **"the framework growing where users don't want it to grow."**

---

## Four Differences You'll Immediately Feel

### 1. SQL Logs Can Carry the Business Call Site

```
getList 15ms sql:SELECT * FROM user WHERE id = 1
```

Enable `show-run-sql` to log SQL with parameters expanded. With `show-caller`, DLZ-DB adds caller information to MDC. Whether that location is displayed or clickable in an IDE depends on the application's logging pattern and console support.

---

### 2. Multi-Datasource: Fully Dynamic at Runtime

`@DS("slave")` hardcodes the datasource key at compile time. Want to route by tenant, by header, by grayscale rules? SpEL patches, manual push/pop, AOP order... every step fights the framework.

DLZ-DB makes both ends strings:

```java
// Register a new datasource at runtime
DB.ds.setDataSource(prop);

// Decide which database to use at runtime with any logic
String dsName = routeByTenant(tenantId);
User user = DB.ds.use(dsName, () ->
    DB.pojo.selectWrapper(User.class).eq(User::getId, id).queryBean()
);
```

**SaaS multi-tenant, dynamic datasource management, ETL tools, grayscale migration**—in these scenarios, the annotation-based approach requires detours, here solved in two lines.

---

### 3. Lightweight Core, Designed to Be Readable

This isn't "few features," it's **"not doing what you don't need":**

- No Mapper interface and XML bidirectional mapping → saves parsing engine
- No SqlSession / Executor layering → call stack goes straight to JDBC
- No level 1/2 cache → leave it to Redis / Caffeine, each doing its job

Actual benefits you get:

- **Readable**: The entire framework has no black boxes, you can follow the source code when bugs occur
- **Customizable**: Want to change a behavior? Fork it and see where to change at a glance
- **Short exception stack**: Query exceptions directly tell you where the SQL is, no need to traverse 10 layers of proxies
- **Light deployment**: Small jar size, fast startup, low memory footprint, suitable for microservices and tool projects

> The design keeps the JDBC path small, but this repository currently provides no reproducible cross-framework benchmark. Measure performance with your SQL, driver, database, and workload.

---

### 4. Query Results Come with Deep Value Access

```java
ResultMap result = DB.table.selectWrapper("user").eq("id", 1).queryOne();

result.getInt("age", 0);
result.getStr("profile.address.city", "Unknown");  // profile is a JSON field
result.getList("orders", Order.class);              // orders is a JSON array
```

`ResultMap` inherits from `JSONMap`, `a.b.c` path value retrieval is a native capability, no need to manually `JSON.parseObject` then layer by layer `.get`.

---

## API Style: Explicit > Magic

DLZ-DB's entire framework aesthetic is consistent: **use explicit lambda and chain calls to fight implicit annotations and proxies.**

```java
// Condition judgment: three-parameter form, no need to write if
.eq(name != null, "name", name)

// Nested logic: lambda expression in place
.ors(o -> o.like(User::getName, "keyword").like(User::getAddress, "keyword"))

// Datascope scope: lambda wrapped
DB.ds.use("other_db", () -> { ... });

// Auto-ignore null values: use square brackets in SQL
[AND status = #{status}]
```

**Control flow visible in code is truly reliable control flow.**

---

## Quick Start in 30 Seconds

DLZ-DB v8 adopts a multi-module architecture; choose dependencies based on the runtime environment:

| Module | Description | Use Case |
|--------|-------------|----------|
| `dlz-db-core` | Core module, zero Spring dependency | Manual integration, non-Spring projects |
| `dlz-db-spring-boot-starter` | Spring Boot auto-configuration | Spring Boot projects (recommended) |
| `dlz-db-solon-plugin` | Solon plugin | Solon projects |

### Spring Boot Quick Start

#### 1. Add Dependency

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>8.0.0</version>
</dependency>
```

#### 2. Configure Datasource

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test
    username: root
    password: 123456

# DLZ-DB Configuration (optional)
dlz:
  db:
    logic-delete-field: deleted
    log:
      show-run-sql: true
      show-caller: true
```

#### 3. Auto-configuration

After adding the starter and configuring `spring.datasource`, DLZ-DB is ready to use. The starter binds `dlz.db.*` automatically; no configuration subclass or `@EnableConfigurationProperties` is required.

#### 4. Start Using

```java
@Data
public class User {
    private Long id;
    private String name;
    private Integer age;
    private Integer deleted;      // Optional: presence enables logical delete
    private Date createTime;
}

@RestController
public class UserController {
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return DB.pojo.selectWrapper(User.class).eq(User::getId, id).queryBean();
    }
}
```

**That's it.** No Mapper, no Service, no XML.

---

### Solon Quick Start

#### 1. Add Dependency

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-solon-plugin</artifactId>
    <version>8.0.0</version>
</dependency>
```

#### 2. Configure

```yaml
dlz:
  db:
    logic-delete-field: deleted
    log:
      show-run-sql: true
      show-caller: true
```

Solon datasource configuration (HikariCP example):

```yaml
datasource:
  default:
    jdbcUrl: jdbc:mysql://localhost:3306/test
    username: root
    password: 123456
    driverClassName: com.mysql.cj.jdbc.Driver
```

#### 3. Use

API is completely consistent under Solon, `DB.pojo`/`DB.table`/`DB.jdbc`/`DB.sql` interfaces unchanged:

```java
@Component
public class UserService {
    public User getUser(Long id) {
        return DB.pojo.selectWrapper(User.class).eq(User::getId, id).queryBean();
    }
}
```

Solon transactions use `@Tran` annotation:

```java
@Tran
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    DB.jdbc.execute(
        "UPDATE account SET balance = balance - ? WHERE id = ?", amount, fromId);
    DB.jdbc.execute(
        "UPDATE account SET balance = balance + ? WHERE id = ?", amount, toId);
}
```

---

## Common Operations Overview

```java
// Query
User u     = DB.pojo.selectWrapper(User.class).eq(User::getId, 1).queryBean();
List<User> list = DB.pojo.selectWrapper(User.class).eq(User::getStatus, 1).queryBeanList();
Page<User> page = DB.pojo.selectWrapper(User.class)
        .page(Page.build(1, 10, Order.desc("create_time")))
        .queryBeanPage();

// Insert
DB.pojo.insert(user);
DB.batch.insert(users, 100);

// Update
DB.pojo.updateWrapper(user).eq(User::getId, id).execute();
DB.pojo.updateWrapper(User.class).set(User::getName, "New Name").eq(User::getId, id).execute();

// Delete (with deleted field automatically uses logical delete)
DB.pojo.deleteWrapper(User.class).eq(User::getId, id).execute();

// Preset SQL (defined in xml/db, key starts with "key.")
List<User> users = DB.sql.selectWrapper("key.user.find")
        .addPara("status", 1)
        .queryList(User.class);
```

---

## Six Entry Points, Clear Responsibility Division

```
Main Operation Entry Points (choose one by SQL style)
├─ DB.pojo   ← First choice when you have Bean, chain + Lambda, type-safe
├─ DB.table  ← Dynamic table name scenarios, no Bean needed
├─ DB.jdbc   ← One-line simple SQL, ? placeholder, second to migrate JdbcTemplate
└─ DB.sql    ← Complex / dynamic / reusable SQL, #{} placeholder + preset SQL

Orthogonal Capabilities (can be superimposed anytime)
├─ DB.batch  ← Batch write
├─ DB.ds     ← Datasource switching scope
└─ DB.tx     ← Programmatic transactions
```

---

## AI-Friendly Design

- Entry points converge to `DB.`, decision tree is shallow
- Condition methods uniformly use three-parameter form `(condition, field, value)`, few exceptions
- Return values have mechanical rules: **with `Bean` → Bean, without → Map, with `(Class)` → specified type**
- Entire usage specification can be compressed into **<1000 tokens** to feed AI (see [docs/第05章-AI辅助/5.1-AI速读.md](./docs/第05章-AI辅助/5.1-AI速读.md))

---

## Common Questions

**Q: How to write complex SQL?**

```java
// Native SQL
DB.jdbc.selectWrapper("complex SQL statement where id=?", id).queryList();

// Preset SQL
DB.sql.list("key.complexQuery", new JSONMap("x", 1));

// Condition builder + custom fragment
DB.pojo.selectWrapper(User.class)
        .eq(User::getStatus, 1)
        .sql("EXISTS (SELECT 1 FROM vip WHERE user_id=t.id AND level>=#{lv})",
             new JSONMap("lv", 3))
        .queryBeanList();
```

**Q: How to debug SQL?**

Turn on `dlz.db.log.show-run-sql=true`, logs will directly show:
1. Complete executable SQL (parameters filled, can be copied to execute directly)
2. Execution time
3. Calling code location (clickable to jump in IDE)

**Q: How about performance?**

DLZ-DB is JDBC-based, but wrapper construction, entity mapping, and logging still have costs. End-to-end performance depends on the SQL, data volume, database, connection pool, and logging configuration; benchmark with the real workload. **The primary goal is simplicity and control, not an unverified performance multiplier.**

**Q: Can it coexist with existing MyBatis / MP projects?**

Yes. DLZ-DB does not depend on the MyBatis ecosystem, so migration can be gradual. If both must participate in the same Spring transaction, verify that they use the same `DataSource` and transaction-bound connection with integration tests.

**Q: Is v7 API compatible with v6?**

Do not assume full compatibility. The overall static-facade style remains, but Maven coordinates, auto-configuration, packages, and some wrapper signatures changed. Migrate with compiler feedback and regression tests; see [6.2-v6升级到v7](./docs/第06章-迁移与升级/6.2-v6升级到v7.md).

---

## Verification

On 2026-08-28, the complete reactor passed on JDK 17 and 21 with JaCoCo enabled: 1,281 tests, 0 failures, 0 errors, and all coverage gates met. The three published library modules also passed the same 1,281 tests on JDK 8. See [TESTING.md](./TESTING.md) for the exact commands and module boundaries.

---

## Documentation Navigation

### Quick Start
- [1.1 Installation - Spring Boot](./docs/第01章-快速入门/1.1-安装配置-SpringBoot.md) (Chinese)
- [1.2 Installation - Solon](./docs/第01章-快速入门/1.2-安装配置-Solon.md) (Chinese)
- [1.3 Five Minutes Quick Start](./docs/第01章-快速入门/1.3-五分钟上手.md) (Chinese)
- [1.4 Core Concepts](./docs/第01章-快速入门/1.4-核心概念.md) (Chinese)

### Basic Operations
- [2.1 Query Operations](./docs/第02章-基础操作/2.1-查询操作.md) (Chinese)
- [2.2 Insert Update Delete](./docs/第02章-基础操作/2.2-插入更新删除.md) (Chinese)
- [2.3 Condition Builder](./docs/第02章-基础操作/2.3-条件构造器.md) (Chinese)
- [2.4 Pagination Sorting](./docs/第02章-基础操作/2.4-分页排序.md) (Chinese)
- [2.5 Result Mapping](./docs/第02章-基础操作/2.5-结果映射.md) (Chinese)

### Advanced Features
- [3.1 Preset SQL](./docs/第03章-高级特性/3.1-预设SQL.md) (Chinese)
- [3.2 Multi-Datasource](./docs/第03章-高级特性/3.2-多数据源.md) (Chinese)
- [3.3 Transaction Management - Spring Boot](./docs/第03章-高级特性/3.3-事务管理-SpringBoot.md) (Chinese)
- [3.4 Transaction Management - Solon](./docs/第03章-高级特性/3.4-事务管理-Solon.md) (Chinese)
- [3.5 Logical Delete and Batch Operations](./docs/第03章-高级特性/3.5-逻辑删除与批量操作.md) (Chinese)
- [3.6 Log Debugging](./docs/第03章-高级特性/3.6-日志调试.md) (Chinese)

### Framework Integration
- [4.1 Spring Boot Complete Configuration](./docs/第04章-框架集成/4.1-SpringBoot完整配置.md) (Chinese)
- [4.2 Solon Complete Integration](./docs/第04章-框架集成/4.2-Solon完整集成.md) (Chinese)
- [4.3 Framework Comparison](./docs/第04章-框架集成/4.3-框架对比.md) (Chinese)
- [4.4 FAQ](./docs/第04章-框架集成/4.4-FAQ.md) (Chinese)

### Migration and Upgrade
- [6.1 Migrate from MyBatis / MP](./docs/第06章-迁移与升级/6.1-从MyBatis-MP迁移.md) (Chinese)
- [6.2 v6 Upgrade to v7](./docs/第06章-迁移与升级/6.2-v6升级到v7.md) (Chinese)
- [6.3 Framework Source Code Guide](./docs/第06章-迁移与升级/6.3-框架源码指南.md) (Chinese)

### Other
- [5.1 AI Quick Reference](./docs/第05章-AI辅助/5.1-AI速读.md) (Chinese, AI code generation specification)
- [5.2 AI Tool Configuration Guide](./docs/第05章-AI辅助/5.2-AI工具配置指南.md) (Chinese)
- [7.1 Best Practices](./docs/第07章-最佳实践/7.1-最佳实践.md) (Chinese)

---

## License

[Apache License 2.0](LICENSE) © DLZ KIT

---

<div>
**Make simple things simple, complex things also simple.**
If you find it helpful, please give it a ⭐ Star to support!
</div>
