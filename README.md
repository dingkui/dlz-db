# DLZ-DB

> **一个轻量的 Java 数据库框架，让你写 SQL 像写本地代码一样直接。**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Runtime](https://img.shields.io/badge/Runtime-Java%208+-green.svg)](https://www.oracle.com/java/)
[![Build JDK](https://img.shields.io/badge/Build%20JDK-17+-blue.svg)](https://www.oracle.com/java/)
[![Build Status](https://github.com/dingkui/dlz-db/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/dingkui/dlz-db/actions/workflows/build-and-test.yml)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-8.0.0-orange.svg)](https://central.sonatype.com/artifact/top.dlzio/dlz-db-core)
[![codecov](https://codecov.io/gh/dingkui/dlz-db/graph/badge.svg?token=UDX6ZH1R0Q)](https://codecov.io/gh/dingkui/dlz-db)

```java
List<User> users = DB.pojo.selectWrapper(User.class)
        .eq(User::getStatus, 1)
        .like(User::getName, "张")
        .orderByDesc(User::getCreateTime)
        .queryBeanList();
```

不要求 Mapper 接口或 XML，也不强制设置 Service 层；有事务、复用或业务编排时仍应使用 Service。

---

## 版本说明

当前版本 v8.0.0（开源初始版 v6.6.4）。

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

### 1. SQL 日志可携带业务调用位置

```
getList 15ms sql:SELECT * FROM user WHERE id = 1
```

开启 `show-run-sql` 可输出展开参数的 SQL；开启 `show-caller` 后，框架会将调用方信息注入 MDC。调用位置是否显示、能否在 IDE 中点击，取决于应用日志 pattern 和控制台支持。

---

### 2. 多数据源：运行时完全动态

`@DS("slave")` 在编译期就把数据源 key 写死了。想按租户、按 Header、按灰度规则路由？SpEL 补丁、手动 push/pop、AOP 顺序……每一步都在和框架较劲。

DLZ-DB 把两端都做成字符串：

```java
// 运行时注册一个新数据源
DB.ds.setDataSource(prop);

// 运行时用任意逻辑决定走哪个库
String dsName = routeByTenant(tenantId);
User user = DB.ds.use(dsName, () ->
    DB.pojo.selectWrapper(User.class).eq(User::getId, id).queryBean()
);
```

**SaaS 多租户、动态数据源管理、ETL 工具、灰度迁移**——这些场景里注解派要绕一大圈，这里两行解决。

---

### 3. 核心代码保持轻量，可通读

这不是"功能少"，是**"不做你不需要的事"**：

- 不做 Mapper 接口和 XML 双向映射 → 省掉解析引擎。
- 不做 SqlSession / Executor 分层 → 调用栈直通 JDBC。
- 不做一二级缓存 → 交给 Redis / Caffeine，各司其职。

你因此得到的实际好处：

- **可通读**：整个框架没有黑盒，出 bug 能自己跟进源码。
- **可定制**：想改一个行为？fork 下来一眼能看到改哪里。
- **异常栈短**：查询异常直接告诉你 SQL 在哪，不需要穿越 10 层代理。
- **部署轻**：jar 体积小、启动快、常驻内存低，适合微服务和工具类项目。

> 项目的目标是保持 JDBC 路径简洁，但仓库当前没有提供可复现的跨框架基准数据。性能结论应以你的 SQL、驱动、数据库和工作负载实测为准。

---

### 4. 查询结果自带深度取值

```java
ResultMap result = DB.table.selectWrapper("user").eq("id", 1).queryOne();

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
.ors(o -> o.like(User::getName, "关键词").like(User::getAddress, "关键词"))

// 数据源作用域：lambda 包起来
DB.ds.use("other_db", () -> { ... });

// 空值自动忽略：SQL 里用方括号
[AND status = #{status}]
```

**代码里能看见的控制流，才是真正可靠的控制流。**

---

## 30 秒上手

DLZ-DB v8 采用多模块架构，可根据运行环境选择依赖：

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
    <version>8.0.0</version>
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

#### 3. 自动装配

引入 Starter 并配置 `spring.datasource` 后即可使用。Starter 会自动绑定 `dlz.db.*`，无需继承配置类，也无需额外添加 `@EnableConfigurationProperties`。

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
        return DB.pojo.selectWrapper(User.class).eq(User::getId, id).queryBean();
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
    <version>8.0.0</version>
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

Solon 下核心 `DB.pojo`/`DB.table`/`DB.jdbc`/`DB.sql` 调用形式与 Spring Boot 一致；数据源初始化和事务语义以各自集成章节为准：

```java
@Component
public class UserService {
    public User getUser(Long id) {
        return DB.pojo.selectWrapper(User.class).eq(User::getId, id).queryBean();
    }
}
```

Solon 事务使用 `@Tran` 注解：

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

## 常见操作速览

```java
// 查询
User u     = DB.pojo.selectWrapper(User.class).eq(User::getId, 1).queryBean();
List<User> list = DB.pojo.selectWrapper(User.class).eq(User::getStatus, 1).queryBeanList();
Page<User> page = DB.pojo.selectWrapper(User.class)
        .page(Page.build(1, 10, Order.desc("create_time")))
        .queryBeanPage();

// 插入
DB.pojo.insert(user);
DB.batch.insert(users, 100);

// 更新
DB.pojo.updateWrapper(user).set(User::getName, "新名字").eq(User::getId, id).execute();
DB.pojo.updateWrapper(User.class).set(User::getName, "新名字").eq(User::getId, id).execute();

// 删除（有 deleted 字段自动走逻辑删除）
DB.pojo.deleteWrapper(User.class).eq(User::getId, id).execute();

// 预设 SQL（xml / db 中定义，key 以 "key." 开头）
List<User> users = DB.sql.list("key.user.find", User.class, new JSONMap("status", 1));
```

---

## 六个入口，职责分工清晰

```
主操作入口（按 SQL 风格选一个）
├─ DB.pojo   ← 有 Bean 时首选，链式 + Lambda，类型安全
├─ DB.table  ← 动态表名场景，不需要 Bean
├─ DB.jdbc   ← 一行搞定的简单 SQL，? 占位符，秒迁 JdbcTemplate
└─ DB.sql    ← 复杂 / 动态 / 可复用 SQL，#{} 占位符 + 预设 SQL

正交能力（任何时候可叠加）
├─ DB.batch  ← 批量写入
├─ DB.ds     ← 数据源切换作用域
└─ DB.tx     ← 编程式事务
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
DB.jdbc.selectWrapper("复杂的SQL语句 WHERE id=?", id).queryList();

// 预设 SQL
DB.sql.list("key.复杂查询", new JSONMap("x", 1));

// 条件构造器 + 自定义片段
DB.pojo.selectWrapper(User.class)
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

底层基于 JDBC，Wrapper 构建、实体映射和日志仍然会产生开销。性能取决于 SQL、数据量、数据库、连接池和日志配置；如需比较，请在真实负载下做 benchmark。**框架的主要目标是简单和可控，不是未经验证的性能倍数。**

**Q：能和现有 MyBatis / MP 项目共存吗？**

可以。DLZ-DB 不依赖 MyBatis 体系，迁移可以渐进。如果两者要加入同一 Spring 事务，必须确认它们使用同一个 `DataSource` 和事务绑定连接，并用集成测试验证。

**Q：v7 和 v6 的 API 兼容吗？**

不应假定完全兼容。静态门面的整体风格延续，但 Maven 坐标、自动配置、包名和部分 Wrapper 签名均有变化，应根据编译错误和回归测试逐项迁移。详见 [6.2-v6升级到v7](./docs/第06章-迁移与升级/6.2-v6升级到v7.md)。

---

## 版本历史

### v8.0.0 — 当前版本

架构级重构：插件化逻辑删除、API 命名统一、DbTable 一步式操作、测试覆盖大幅提升。

#### ✨ 新增

- **插件化架构**：新增 `SqlBuildInterceptor` 接口 + `DbPlugin` 注册中心，`LogicDeleteInterceptor` 从核心代码抽取为可插拔插件，后续可扩展租户隔离、数据权限等。
- **`DbTable` 一步式操作**：新增 `insert(table, map)` / `insertWithAutoKey()` / `insertOrUpdate()` / `selectById()` / `selectByIds()` / `deleteById()` / `deleteByIds()` 直接执行 API，无需 Wrapper 链式调用；其中 `insertOrUpdate()` 按主键是否为空选择 INSERT/UPDATE，不是数据库原子 Upsert。
- **`DBDynamic.testConnection(DataSourceProperty)`**：测试数据源连接，不注册到配置池；失败时抛出异常。
- **Table 主键元数据**：一步式 `DB.table` 操作由框架内部解析单列主键；该内部缓存类不是稳定业务 API。

#### 🔧 变更（Breaking）

| 变更 | 旧 API (7.0.x) | 新 API (8.0.0) | 说明 |
|------|----------------|----------------|------|
| **DbPojo 方法** | `DB.pojo.selectWrapper(User.class)` | `DB.pojo.selectWrapper(User.class)` | 当前 Wrapper API 使用 `selectWrapper` / `updateWrapper` / `deleteWrapper` |
| | `DB.pojo.selectWrapper(conditionBean)` | 移除 | 请改用 `selectWrapper(Class)` + 条件链 |
| | `DB.pojo.deleteWrapper(conditionBean)` | 移除 | 同上 |
| | `DB.pojo.updateWrapper(Class)` | `DB.pojo.updateWrapper(Class)` | 同上 |
| **DbTable 方法** | `DB.table.selectWrapper("user")` | `DB.table.selectWrapper("user")` | Wrapper API 使用 `selectWrapper` / `insertWrapper` / `updateWrapper` / `deleteWrapper` |
| **DbBatch 方法** | `DB.batch.insert(list)` | `DB.batch.insert(list)` | Pojo、Table 批量插入通过重载区分 |
| | `DB.batch.update(list)` | `DB.batch.update(list)` | JDBC 批量执行使用 `DB.batch.execute(sql, params)` |
| **DbJdbc / DbSql 执行** | `DB.jdbc.execute` / `DB.sql.execute` | 保留 | 查询使用 `list` / `one` / `count` 等直接方法 |
| **逻辑删除** | 删除执行链处理 | 使用 `LogicDeleteInterceptor` 插件 | 注册方式以框架初始化流程和实际配置为准 |
| **ISqlExecutor.doDb(Supplier)** | 公开默认方法 | 注释掉 | 改用 `doDb(SqlAction, msg)` 重载 |

#### 🐛 修复

- **`IExecutorDelete.execute()` 插件链**：逻辑删除从硬编码改为插件调用，修复 `ignoreLogicDelete` 作用域残留问题（`finally` 块确保清理）。
- **PojoCache.getLogicDeleteInfo**：不再从 `WrapperBuildUtil.logicDeleteField` 静态变量获取，改为由 `LogicDeleteInterceptor` 构造参数注入。
- **JDBC 原生辅助路径**：当前主流程使用 `NativeSqlUtil`；`NativeJdbcSupport` 仍保留在源码中，不应写成已删除的公共类。

#### 🗑 移除

- `DbJdbc.insert/update/delete`（统一为 `execute`）
- `DbSql.insert/update/delete`（统一为 `execute`）
- `DbPojo.select(conditionBean)` / `delete(conditionBean)`；当前统一使用 `selectWrapper(Class)` / `deleteWrapper(Class)`
- `SchemaDialect.getTableIndexs()`（未使用，替代方案为 JDBC `DatabaseMetaData.getIndexInfo()`）

#### 📦 依赖

- 版本号：`7.0.1-4` → `8.0.0`
- `dlz-kit` → `6.7.4`
- `solon` → `3.0.6`

#### ✅ 测试

- 2026-08-28 使用 JDK 17、21 执行完整 reactor 的 `mvn -B clean verify -Djacoco.skip=false`，均为 1281 个测试、0 失败、0 错误，且覆盖率门槛通过。
- 同日使用 JDK 8 执行三个发行库模块的 CI 兼容命令，1281 个测试同样全部通过。命令、模块边界和覆盖率见 [TESTING.md](./TESTING.md)。

---

## 文档导航

### 产品总览
- [产品介绍、能力边界与使用指南](./docs/DLZ-DB产品介绍与使用边界.md)（推荐首次阅读）

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
