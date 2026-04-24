# DLZ-DB

> **一个不到 1 万行代码的 Java 数据库框架，让你写 SQL 像写本地代码一样直接。**

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](../LICENSE)
[![JDK](https://img.shields.io/badge/JDK-8+-green.svg)](https://www.oracle.com/java/)

```java
List<User> users = DB.Pojo.select(User.class)
        .eq(User::getStatus, 1)
        .lk(User::getName, "张")
        .orderByDesc(User::getCreateTime)
        .queryBeanList();
```

没有 Mapper 接口，没有 Service 层，没有 XML。

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
DBDynamic.addDataSource(prop);

// 运行时用任意逻辑决定走哪个库
String dsName = routeByTenant(tenantId);
User user = DB.Dynamic.use(dsName, () ->
    DB.Pojo.select(User.class).eq(User::getId, id).queryBean()
);
```

**SaaS 多租户、动态数据源管理、ETL 工具、灰度迁移**——这些场景里注解派要绕一大圈，这里两行解决。

---

### 3. 核心代码不到 1 万行，一个下午能通读

这不是"功能少"，是**"不做你不需要的事"**：

- 不做 Mapper 接口和 XML 双向映射 → 省掉解析引擎。
- 不做 SqlSession / Executor 分层 → 调用栈直通 JDBC。
- 不做一二级缓存 → 交给 Redis / Caffeine，各司其职。

你因此得到的实际好处：

- **📖 可通读**：整个框架没有黑盒，出 bug 能自己跟进源码。
- **🔧 可定制**：想改一个行为？fork 下来一眼能看到改哪里。
- **🐛 异常栈短**：查询异常直接告诉你 SQL 在哪，不需要穿越 10 层代理。
- **📦 部署轻**：jar 体积小、启动快、常驻内存低，适合微服务和工具类项目。

> 运行时单次查询性能与 MyBatis 相近——数据库才是瓶颈，框架层差距可以忽略。我们不在这个维度卷。

---

### 4. 查询结果自带深度取值

```java
ResultMap result = DB.Pojo.select("user").eq("id", 1).queryOne();

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
.or(o -> o.lk(User::getName, "关键词").lk(User::getAddress, "关键词"))

// 数据源作用域：lambda 包起来
DB.Dynamic.use("other_db", () -> { ... });

// 空值自动忽略：SQL 里用方括号
[AND status = #{status}]
```

**代码里能看见的控制流，才是真正可靠的控制流。**

---

## ⚡ 30 秒上手

### 1. 引入依赖

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz.db</artifactId>
    <version>${最新版本}</version>
</dependency>
```

### 2. 配置数据源

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test
    username: root
    password: 123456
```

### 3. 启用 DLZ-DB

```java
@Configuration
@EnableConfigurationProperties(DlzDbProperties.class)
public class DlzDbConfigs extends DlzDbConfig {}
```

### 4. 开始使用

```java
@Data
public class User {
    private Long id;
    private String name;
    private Integer age;
    private Integer isDeleted;      // 可选：存在即启用逻辑删除
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

## 📚 常见操作速览

```java
// 查询
User u     = DB.Pojo.select(User.class).eq(User::getId, 1).queryBean();
List<User> list = DB.Pojo.select(User.class).eq(User::getStatus, 1).queryBeanList();
Page<User> page = DB.Pojo.select(User.class)
        .setPage(Page.build(1, 10, Order.desc("create_time")))
        .queryBeanPage();

// 插入
DB.Pojo.insert(user).execute();
Long id = DB.Pojo.insert(user).insertWithAutoKey();
DB.Batch.saveBatch(users, 100);

// 更新
DB.Pojo.update(user).eq(User::getId, id).execute();
DB.Pojo.update(User.class).set(User::getName, "新名字").eq(User::getId, id).execute();

// 删除（有 isDeleted 字段自动走逻辑删除）
DB.Pojo.delete(User.class).eq(User::getId, id).execute();

// 预设 SQL（xml / db 中定义，key 以 "key." 开头）
List<User> users = DB.Sql.select("key.user.find")
        .addPara("status", 1)
        .queryList(User.class);
```

---

## 🧭 六个入口，职责分工清晰

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

六个入口覆盖四种 SQL 书写风格加两个正交维度——**不是"入口多"，是"恰好分完"**。

---

## 🤖 对 AI 也友好

> 不是刻意为之，是"API 简单"的副产品。

- 入口收敛到 `DB.`，决策树很浅。
- 条件方法统一 `(condition, field, value)` 三参形式，特例少。
- 返回值有机械规则：**带 `Bean` → Bean，不带 → Map，带 `(Class)` → 指定类型**。
- 整个使用规范可以压进 **1000 token** 以内塞给 AI（见 [docs/AI-速读指南.md](./docs/AI-速读指南.md)）。

在 Cursor / Windsurf / Copilot 时代，**AI 能一次写对的框架**本身就是生产力。

---

## 🤝 常见问题

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

---

## 🎯 谁适合用？谁不适合？

### ✅ 很适合

- 中小型业务系统、内部工具、微服务。
- SaaS / 多租户 / 需要运行时动态数据源的场景。
- 想摆脱 MyBatis XML 又受不了 JPA 黑盒的团队。
- 看重"能读懂每一行框架代码"的工程师。

### ⚠️ 要谨慎

- 已经深度绑定 Spring Data JPA 或 MP 生态的存量项目——迁移成本需要评估。
- 需要 Seata 分布式事务 / ShardingSphere 分库分表等重型基础设施的场景——这些我们不造轮子。
- 对"社区规模 = 安全感"要求很高的大型企业——DLZ-DB 还在成长期，请先小范围试用。

**诚实地说：我们不是银弹，只是一把趁手的小刀。**

---

## 📖 文档导航

- 📘 [快速开始](./docs/01-快速开始.md)
- 📗 [完整使用指南](./docs/DLZ-DB完整使用指南.md)
- 🤖 [AI 速读指南](./docs/AI-速读指南.md)（也适合人类快速查阅）
- 📙 [迁移指南](./docs/16-迁移指南.md)（从 MyBatis / MP 切换）
- 📕 [框架对比](./docs/18-框架对比.md)
- 📚 [速查文档](./docs/00-速查文档.md)

分主题文档：[核心概念](./docs/02-核心概念.md) · [基础操作](./docs/03-基础操作.md) · [条件构造器](./docs/04-条件构造器.md) · [分页排序](./docs/05-分页排序.md) · [结果映射](./docs/06-结果映射.md) · [Lambda 表达式](./docs/07-Lambda表达式.md) · [预设 SQL](./docs/08-预设SQL.md) · [多数据源](./docs/09-多数据源.md) · [日志调试](./docs/10-日志调试.md) · [高级特性](./docs/11-高级特性.md) · [最佳实践](./docs/12-最佳实践.md) · [API 参考](./docs/13-API参考.md)

---

## 📄 License

[MIT License](../LICENSE) © DLZ KIT

---

<div align="center">
**简单的事情简单做，复杂的事情也能简单做。**
如果觉得有帮助，请点个 ⭐ Star 支持一下！
</div>
