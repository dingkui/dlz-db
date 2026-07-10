# DLZ-DB

> **一个不到 7000 行代码的 Java 数据库框架，让你写 SQL 像写本地代码一样直接。**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-8+-green.svg)](https://www.oracle.com/java/)
[![Build Status](https://github.com/dingkui/dlz-db/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/dingkui/dlz-db/actions/workflows/build-and-test.yml)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-7.1.0-orange.svg)](https://central.sonatype.com/artifact/top.dlzio/dlz-db-core)
[![codecov](https://codecov.io/gh/dingkui/dlz-db/graph/badge.svg?token=UDX6ZH1R0Q)](https://codecov.io/gh/dingkui/dlz-db)

```java
List<User> users = DB.Pojo.select(User.class)
        .eq(User::getStatus, 1)
        .like(User::getName, "张")
        .orderByDesc(User::getCreateTime)
        .queryBeanList();
```

没有 Mapper 接口，没有 Service 层，没有 XML。

---

## 版本说明

当前版本 v7.1.0（开源初始版 v6.6.4）。

这个项目不是从零开始的。它大约在 **2009 年**开始积累，**2014 年**左右成型，作为公司内部的数据库操作工具包投入使用。此后十年间，累计被数十个内部项目采用，适配过各种老旧系统、各种开源框架组合、各种奇奇怪怪的版本混搭。

**2024 年**，我们决定将它开源。为此做了大量重构和删减——剥离内部依赖、清理历史包袱、提炼通用能力，最终发布了第一个公开版本 **6.6.4**。

所以版本号不是从 1.0 开始的——因为你看到的是一个已经跑了十几年的工具，被数十个项目验证过，而不是一个刚起步的新项目。

---

## 为什么又一个数据库框架？

如果你写过一段时间 Java，大概率经历过这些：

- 为了一次简单的 CRUD，创建 6 个文件、200 行代码。
- 线上日志一条 SQL，全局搜半小时找不到是谁执行的。
- `@DS("slave")` 写在注解里，新租户动态接入？对不起，字符串硬编码。
- MyBatis 出异常，栈里 15 层代理，看不出错在哪一行业务代码。
- 查询返回一个 JSON 字段，还得自己 `JSON.parseObject(...)` 一层层剥。

DLZ-DB 想解决的不是"没有框架"，而是**"框架长在了用户不想要它长的地方"**。

---

## 四个你会立刻感受到的不同

### 1. SQL 日志能直接跳到写它的那一行

```
caller:(UserController.java:42) getList 15ms sql:SELECT * FROM user WHERE id = 1
        ↑                                    ↑                 ↑
   IDE 点这里直接跳转              真实耗时           参数已填充，复制即可执行
```

MyBatis 日志告诉你"有一条 SQL 跑了"，DLZ-DB 告诉你**"是你的 UserController 第 42 行跑的"**。生产排查时，这一条就值回票价。

---

### 2. 多数据源：运行时完全动态

`@DS("slave")` 在编译期就把数据源 key 写死了。想按租户、按 Header、按灰度规则路由？SpEL 补丁、手动 push/pop、AOP 顺序……每一步都在和框架较劲。

DLZ-DB 把两端都做成字符串：

```java
// 运行时注册一个新数据源
DB.Dynamic.setDataSource(prop);

// 运行时用任意逻辑决定走哪个库
String dsName = routeByTenant(tenantId);
User user = DB.Dynamic.use(dsName, () ->
    DB.Pojo.select(User.class).eq(User::getId, id).queryBean()
);
```

**SaaS 多租户、动态数据源管理、ETL 工具、灰度迁移**——这些场景里注解派要绕一大圈，这里两行解决。

---

### 3. 核心代码不到 7000 行，2天能通读

这不是"功能少"，是**"不做你不需要的事"**：

- 不做 Mapper 接口和 XML 双向映射 → 省掉解析引擎。
- 不做 SqlSession / Executor 分层 → 调用栈直通 JDBC。
- 不做一二级缓存 → 交给 Redis / Caffeine，各司其职。

你因此得到的实际好处：

- **可通读**：整个框架没有黑盒，出 bug 能自己跟进源码。
- **可定制**：想改一个行为？fork 下来一眼能看到改哪里。
- **异常栈短**：查询异常直接告诉你 SQL 在哪，不需要穿越 10 层代理。
- **部署轻**：jar 体积小、启动快、常驻内存低，适合微服务和工具类项目。

> 运行时单次查询性能与 MyBatis 相近——数据库才是瓶颈，框架层差距可以忽略。我们不在这个维度卷。

---

### 4. 查询结果自带深度取值

```java
ResultMap result = DB.Table.select("user").eq("id", 1).queryOne();

result.getInt("age", 0);
result.getStr("profile.address.city", "未知");  // profile 是 JSON 字段
result.getList("orders", Order.class);          // orders 是 JSON 数组
```

`ResultMap` 继承自 `JSONMap`，`a.b.c` 路径取值是原生能力，不用自己 `JSON.parseObject` 再一层层 `.get`。

---

## API 风格：显式 > 魔法

DLZ-DB 整个框架的审美是一致的：**用显式的 lambda 和链式，对抗隐式的注解和代理。**

```java
// 条件判断：三参形式，不用写 if
.eq(name != null, "name", name)

// 嵌套逻辑：lambda 就地表达
.or(o -> o.like(User::getName, "关键词").like(User::getAddress, "关键词"))

// 数据源作用域：lambda 包起来
DB.Dynamic.use("other_db", () -> { ... });

// 空值自动忽略：SQL 里用方括号
[AND status = #{status}]
```

**代码里能看见的控制流，才是真正可靠的控制流。**

---

## 30 秒上手

DLZ-DB v7 采用多模块架构，可根据运行环境选择依赖：

| 模块 | 说明 | 适用场景 |
|------|------|---------|
| `dlz-db-core` | 核心模块，零 Spring 依赖 | 手动集成、非 Spring 项目 |
| `dlz-db-spring-boot-starter` | Spring Boot 自动配置 | Spring Boot 项目（推荐） |
| `dlz-db-solon-plugin` | Solon 插件 | Solon 项目 |

### Spring Boot 快速开始

#### 1. 引入依赖

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>7.1.0</version>
</dependency>
```

#### 2. 配置数据源

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test
    username: root
    password: 123456

# DLZ-DB 配置（可选）
dlz:
  db:
    logic-delete-field: deleted
    log:
      show-run-sql: true
      show-caller: true
```

#### 3. 启用 DLZ-DB

```java
@Configuration
@EnableConfigurationProperties(SpringDlzDbProperties.class)
public class DlzDbConfigs extends SpringDlzDbConfig {}
```

> 包路径：`com.dlz.db.spring.config.SpringDlzDbConfig`、`com.dlz.db.spring.config.SpringDlzDbProperties`

#### 4. 开始使用

```java
@Data
public class User {
    private Long id;
    private String name;
    private Integer age;
    private Integer deleted;      // 可选：存在即启用逻辑删除
    private Date createTime;
}

@RestController
public class UserController {
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return DB.Pojo.select(User.class).eq(User::getId, id).queryBean();
    }
}
```

**没了。** 不需要 Mapper，不需要 Service，不需要 XML。

---

### Solon 快速开始

#### 1. 引入依赖

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-solon-plugin</artifactId>
    <version>7.1.0</version>
</dependency>
```

#### 2. 配置

```yaml
dlz:
  db:
    logic-delete-field: deleted
    log:
      show-run-sql: true
      show-caller: true
```

Solon 数据源配置（以 HikariCP 为例）：

```yaml
datasource:
  default:
    jdbcUrl: jdbc:mysql://localhost:3306/test
    username: root
    password: 123456
    driverClassName: com.mysql.cj.jdbc.Driver
```

#### 3. 使用

Solon 下 API 完全一致，`DB.Pojo`/`DB.Table`/`DB.Jdbc`/`DB.Sql` 接口不变：

```java
@Component
public class UserService {
    public User getUser(Long id) {
        return DB.Pojo.select(User.class).eq(User::getId, id).queryBean();
    }
}
```

Solon 事务使用 `@Tran` 注解：

```java
@Tran
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    DB.Pojo.update(Account.class)
        .setSql("balance = balance - #{amount}", Params.of("amount", amount))
        .eq(Account::getId, fromId)
        .execute();
    DB.Pojo.update(Account.class)
        .setSql("balance = balance + #{amount}", Params.of("amount", amount))
        .eq(Account::getId, toId)
        .execute();
}
```

---

## 常见操作速览

```java
// 查询
User u     = DB.Pojo.select(User.class).eq(User::getId, 1).queryBean();
List<User> list = DB.Pojo.select(User.class).eq(User::getStatus, 1).queryBeanList();
Page<User> page = DB.Pojo.select(User.class)
        .setPage(Page.build(1, 10, Order.desc("create_time")))
        .queryBeanPage();

// 插入
DB.Pojo.insert(user);
DB.Batch.insert(users, 100);

// 更新
DB.Pojo.update(user).eq(User::getId, id).execute();
DB.Pojo.update(User.class).set(User::getName, "新名字").eq(User::getId, id).execute();

// 删除（有 deleted 字段自动走逻辑删除）
DB.Pojo.delete(User.class).eq(User::getId, id).execute();

// 预设 SQL（xml / db 中定义，key 以 "key." 开头）
List<User> users = DB.Sql.select("key.user.find")
        .addPara("status", 1)
        .queryList(User.class);
```

---

## 六个入口，职责分工清晰

```
主操作入口（按 SQL 风格选一个）
├─ DB.Pojo   ← 有 Bean 时首选，链式 + Lambda，类型安全
├─ DB.Table  ← 动态表名场景，不需要 Bean
├─ DB.Jdbc   ← 一行搞定的简单 SQL，? 占位符，秒迁 JdbcTemplate
└─ DB.Sql    ← 复杂 / 动态 / 可复用 SQL，#{} 占位符 + 预设 SQL

正交能力（任何时候可叠加）
├─ DB.Batch    ← 批量写入
└─ DB.Dynamic  ← 数据源切换作用域
```

---

## 对 AI 也友好

- 入口收敛到 `DB.`，决策树很浅。
- 条件方法统一 `(condition, field, value)` 三参形式，特例少。
- 返回值有机械规则：**带 `Bean` → Bean，不带 → Map，带 `(Class)` → 指定类型**。
- 整个使用规范可以压进 **1000 token** 以内塞给 AI（见 [docs/第05章-AI辅助/5.1-AI速读.md](./docs/第05章-AI辅助/5.1-AI速读.md)）。

---

## 常见问题

**Q：复杂 SQL 怎么写？**

```java
// 原生 SQL
DB.Jdbc.select("复杂的SQL语句 where id=?", id).queryList();

// 预设 SQL
DB.Sql.select("key.复杂查询").addPara("x", 1).queryList();

// 条件构造器 + 自定义片段
DB.Pojo.select(User.class)
        .eq(User::getStatus, 1)
        .sql("EXISTS (SELECT 1 FROM vip WHERE user_id=t.id AND level>=#{lv})",
             new JSONMap("lv", 3))
        .queryBeanList();
```

**Q：如何调试 SQL？**

打开 `dlz.db.log.show-run-sql=true`，日志会直接显示：
1. 完整的可执行 SQL（参数已填充，可直接复制执行）
2. 执行耗时
3. 调用代码位置（IDE 中可点击跳转）

**Q：性能如何？**

底层直通 JDBC，无额外反射/代理损耗。与 MyBatis 运行时接近，不是我们的主要卖点——**我们卖的是简单和可控，不是性能。**

**Q：能和现有 MyBatis / MP 项目共存吗？**

可以。DLZ-DB 不依赖 MyBatis 体系，两者走各自的数据源和连接即可。迁移可以渐进——新模块用 DLZ-DB，老模块保持不动。

**Q：v7 和 v6 的 API 兼容吗？**

`DB.Pojo`/`DB.Table`/`DB.Jdbc`/`DB.Sql` 等核心 API 完全兼容。但 Maven 坐标、配置类包路径有变更，详见 [6.2-v6升级到v7](./docs/第06章-迁移与升级/6.2-v6升级到v7.md)。

---

## 版本历史

### v7.1.0 — 2026-07-01

架构级重构：插件化逻辑删除、API 命名统一、DbTable 一步式操作、测试覆盖大幅提升。

#### ✨ 新增

- **插件化架构**：新增 `SqlBuildInterceptor` 接口 + `DbPlugin` 注册中心，`LogicDeleteInterceptor` 从核心代码抽取为可插拔插件，后续可扩展租户隔离、数据权限等。
- **`DbTable` 一步式操作**：新增 `insert(table, map)` / `insertWithAutoKey()` / `insertOrUpdate()` / `selectById()` / `selectByIds()` / `deleteById()` / `deleteByIds()` 直接执行 API，无需 Wrapper 链式调用。
- **`DBDynamic.testConnection(DataSourceProperty)`**：测试数据源连接，不注册到配置池，返回成功/异常。
- **`SqlHelper.listTables()`**：跨数据库通用表列表查询，走 JDBC `DatabaseMetaData.getTables()`，统一 MySQL/PostgreSQL/H2 行为。
- **`PojoCache.getIdName(String tableName)`**：按表名查询主键字段名（支持 `DB.Table` 一步式操作）。

#### 🔧 变更（Breaking）

| 变更 | 旧 API (7.0.x) | 新 API (7.1.0) | 说明 |
|------|----------------|----------------|------|
| **DbPojo 方法重命名** | `DB.Pojo.select(User.class)` | `DB.Pojo.select(User.class)` | `select` / `delete` / `update` → `select` / `delete` / `updateW` |
| | `DB.Pojo.select(conditionBean)` | 移除 | 请改用 `select(Class)` + 条件链 |
| | `DB.Pojo.delete(conditionBean)` | 移除 | 同上 |
| | `DB.Pojo.update(Class)` | `DB.Pojo.update(Class)` | 同上 |
| **DbTable 方法重命名** | `DB.Table.select("user")` | `DB.Table.select("user")` | `select` / `insert` / `delete` / `update` → `select` / `insertW` / `delete` / `updateW` |
| **DbBatch 方法重命名** | `DB.Batch.insert(list)` | `DB.Batch.pojoInsert(list)` | 区分 Pojo / Table / Jdbc 三种模式 |
| | `DB.Batch.update(list)` | `DB.Batch.pojoUpdate(list)` | 同上 |
| | — | `DB.Batch.tableInsert/tableUpdate/jdbcExecute` | 新增 Table/Jdbc 批量 API |
| **DbJdbc 精简** | `DB.Jdbc.insert/update/delete` | 移除 | 统一用 `DB.Jdbc.execute()` |
| **DbSql 精简** | `DB.Sql.insert/update/delete` | 移除 | 统一用 `DB.Sql.execute()` |
| **逻辑删除** | 硬编码在 `IExecutorDelete` / `WrapperBuildUtil` | 抽取为 `LogicDeleteInterceptor` 插件 | 需通过 `DbPlugin.registerInterceptor()` 注册 |
| **ISqlExecutor.doDb(Supplier)** | 公开默认方法 | 注释掉 | 改用 `doDb(SqlAction, msg)` 重载 |

#### 🐛 修复

- **`IExecutorDelete.execute()` 插件链**：逻辑删除从硬编码改为插件调用，修复 `ignoreLogicDelete` 作用域残留问题（`finally` 块确保清理）。
- **PojoCache.getLogicDeleteInfo**：不再从 `WrapperBuildUtil.logicDeleteField` 静态变量获取，改为由 `LogicDeleteInterceptor` 构造参数注入。
- **`NativeJdbcSupport`**：整文件注释掉（已废弃，替换为 `NativeSqlUtil`）。

#### 🗑 移除

- `NativeJdbcSupport.java`（已废弃两年）
- `DbJdbc.insert/update/delete`（统一为 `execute`）
- `DbSql.insert/update/delete`（统一为 `execute`）
- `DbPojo.select(conditionBean)` / `delete(conditionBean)`（重命名为 `select/delete`）
- `SqlHelper.getTableIndexs()`（未使用，替代方案为 JDBC `DatabaseMetaData.getIndexInfo()`）
- 测试用废弃实体类：`Department` / `GoodsPrice` / `Room` / `Smoke` / `Vip`

#### 📦 依赖

- 版本号：`7.0.1-4` → `7.1.0`
- `dlz-kit` → `6.6.5`
- `dlz-spring` → `6.6.5`
- `solon` → `3.0.6`

#### ✅ 测试

- 123 个文件变更，+4130 / −2410 行
- 新增：`DbTableTest`（42 测试）、`ISqlExecutorTest`（15 测试）、batch 测试（5 文件）、wrapper 测试重构
- `com.dlz.db.util` 包覆盖从 ~50% 提升至 ~90%
- `IExecutorQuery` 默认方法覆盖达 100%

---

## 文档导航

### 快速上手
- [1.1 安装配置 - Spring Boot](./docs/第01章-快速入门/1.1-安装配置-SpringBoot.md)
- [1.2 安装配置 - Solon](./docs/第01章-快速入门/1.2-安装配置-Solon.md)
- [1.3 五分钟上手](./docs/第01章-快速入门/1.3-五分钟上手.md)
- [1.4 核心概念](./docs/第01章-快速入门/1.4-核心概念.md)

### 基础操作
- [2.1 查询操作](./docs/第02章-基础操作/2.1-查询操作.md)
- [2.2 插入更新删除](./docs/第02章-基础操作/2.2-插入更新删除.md)
- [2.3 条件构造器](./docs/第02章-基础操作/2.3-条件构造器.md)
- [2.4 分页排序](./docs/第02章-基础操作/2.4-分页排序.md)
- [2.5 结果映射](./docs/第02章-基础操作/2.5-结果映射.md)

### 高级特性
- [3.1 预设 SQL](./docs/第03章-高级特性/3.1-预设SQL.md)
- [3.2 多数据源](./docs/第03章-高级特性/3.2-多数据源.md)
- [3.3 事务管理 - Spring Boot](./docs/第03章-高级特性/3.3-事务管理-SpringBoot.md)
- [3.4 事务管理 - Solon](./docs/第03章-高级特性/3.4-事务管理-Solon.md)
- [3.5 逻辑删除与批量操作](./docs/第03章-高级特性/3.5-逻辑删除与批量操作.md)
- [3.6 日志调试](./docs/第03章-高级特性/3.6-日志调试.md)

### 框架集成
- [4.1 Spring Boot 完整配置](./docs/第04章-框架集成/4.1-SpringBoot完整配置.md)
- [4.2 Solon 完整集成](./docs/第04章-框架集成/4.2-Solon完整集成.md)
- [4.3 框架对比](./docs/第04章-框架集成/4.3-框架对比.md)
- [4.4 FAQ](./docs/第04章-框架集成/4.4-FAQ.md)

### 迁移与升级
- [6.1 从 MyBatis / MP 迁移](./docs/第06章-迁移与升级/6.1-从MyBatis-MP迁移.md)
- [6.2 v6 升级到 v7](./docs/第06章-迁移与升级/6.2-v6升级到v7.md)
- [6.3 框架源码指南](./docs/第06章-迁移与升级/6.3-框架源码指南.md)

### 其他
- [5.1 AI 速读](./docs/第05章-AI辅助/5.1-AI速读.md)（AI 代码生成规范）
- [5.1 AI Quick Reference (English)](./docs/Chapter05-AI/5.1-AI-Quick-Reference.md)（English AI Quick Reference）
- [7.1 最佳实践](./docs/第07章-最佳实践/7.1-最佳实践.md)

### English Documentation
- [README (English)](./README_EN.md)

---

## License

[Apache License 2.0](LICENSE) © DLZ KIT

---

<div>
**简单的事情简单做，复杂的事情也能简单做。**
如果觉得有帮助，请点个 ⭐ Star 支持一下！
</div>
