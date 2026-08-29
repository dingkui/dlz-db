# DLZ-DB 8.0 AI 速读

适用依赖：

- `top.dlzio:dlz-db-spring-boot-starter:8.0.0`
- `top.dlzio:dlz-db-solon-plugin:8.0.0`

> 静态入口 `DB.*`，无需自写 Mapper/DAO。Spring Boot 和 Solon 共用核心 CRUD API。

## 1. 入口选择

| 入口 | 场景 | 参数形式 |
|---|---|---|
| `DB.pojo` | 有实体类的 CRUD，首选 | Lambda 条件 |
| `DB.table` | 动态表名 | 字段名 + `JSONMap` |
| `DB.jdbc` | 一次性 SQL | `?` |
| `DB.sql` | 预设 SQL/Key-SQL | `#{key}` |
| `DB.batch` | 批量写入 | Pojo/表数据/SQL 参数 |
| `DB.ds` | 数据源注册和切换 | 数据源名 |
| `DB.tx` | 编程式事务 | lambda |

## 2. 条件与分页

| 方法 | SQL 语义 |
|---|---|
| `eq/ne/gt/ge/lt/le` | `= / <> / > / >= / < / <=` |
| `isNull/isNotNull` | `IS NULL / IS NOT NULL` |
| `in/notIn` | `IN / NOT IN` |
| `between/notBetween` | `BETWEEN / NOT BETWEEN` |
| `like` | `LIKE '%key%'` |
| `likeLeft` | `LIKE '%key'` |
| `likeRight` | `LIKE 'key%'` |
| `notLike` | `NOT LIKE '%key%'` |
| `ands(a -> ...)` | lambda 内部使用 AND |
| `ors(o -> ...)` | lambda 内部使用 OR |
| `sql(text, JSONMap)` | 自定义 `#{key}` 条件片段 |

```java
Page<User> page = DB.pojo.selectWrapper(User.class)
    .select(User::getId, User::getName)
    .eq(status != null, User::getStatus, status)
    .ors(o -> o.like(User::getName, keyword)
               .like(User::getMobile, keyword))
    .orderByDesc(User::getCreateTime)
    .page(1, 20)
    .queryBeanPage();
```

## 3. 返回类型规则

| 方法 | 返回值 |
|---|---|
| `queryBean/queryBeanList/queryBeanPage` | `T / List<T> / Page<T>` |
| `queryOne/queryList/queryPage` | `ResultMap / List<ResultMap> / Page<ResultMap>` |
| `queryOne(C)/queryList(C)/queryPage(C)` | 指定类型 `C` |
| `queryFirst/queryFirstBean` | 非严格地取第一条 |
| `queryStr/Long/Int/Double` 及 List 版 | 第一列标量 |
| `count()` | `long` |
| 写 Wrapper 的 `execute()` | 影响行数 `int` |

`queryOne/queryBean` 在结果多于一条时抛出异常；不确定唯一性时用 `queryFirst/queryFirstBean`。

## 4. CRUD 模板

```java
// 查询
User user = DB.pojo.selectById(User.class, id);
List<User> users = DB.pojo.selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .queryBeanList();

// 插入/更新/删除的直接 API 会立即执行
DB.pojo.insert(user);
DB.pojo.updateById(user);
DB.pojo.deleteById(User.class, id);

// Wrapper 写操作以 execute() 结束
DB.pojo.updateWrapper(User.class)
    .set(User::getStatus, 2)
    .eq(User::getId, id)
    .execute();

DB.pojo.deleteWrapper(User.class)
    .eq(User::getId, id)
    .execute();
```

## 5. JDBC、预设 SQL 与批量

```java
List<ResultMap> rows = DB.jdbc.list(
    "SELECT * FROM user WHERE status = ?", 1);

Page<User> page = DB.jdbc.page(
    "SELECT * FROM user WHERE status = ?",
    PageRequest.of(1, 20), User.class, 1);

List<User> presetRows = DB.sql.selectWrapper("key.user.findActive")
    .addPara("status", 1)
    .queryList(User.class);

BatchResult result = DB.batch.insert(users, 500);
if (!result.isSuccess()) {
    // result.status() / failedPositions() / cause()
}
```

预设 SQL 文件路径为 `resources/sql/**/*.sql`，文件内使用 `<sqlList>` XML 结构。调用时 key 以 `key.` 开头。`${key}` 是直接 SQL 替换，只用于经过白名单校验的列名、排序和片段，不用于普通用户输入。

## 6. 事务与多数据源

```java
DB.ds.use("slave", () -> DB.pojo.selectById(User.class, id));

DB.tx.run(() -> {
    DB.pojo.insert(order);
    DB.pojo.insert(orderItem);
});

DB.tx.run("slave", () -> {
    // 指定数据源上的事务
});
```

`DB.ds.use` 只切换数据源，不开启事务；多数据源也不等于分布式事务。Spring 中可用 `@Transactional`，Solon 中可用 `@Tran`。

## 7. Entity 约定

- 表名：默认驼峰转下划线，自定义用 `@TableName`。
- 字段：自定义用 `@TableField`，主键用 `@TableId`。
- 注解包：`com.dlz.db.core.anno`。
- 逻辑删除字段默认为 `deleted`，可通过 `dlz.db.logic-delete-field` 修改。
- 支持 `DbOption...` 的直接查询（如 `selectById`）可传 `SelectOption.INCLUDE_DELETED`查询已删除数据；当前 Pojo 查询 Wrapper 没有对称的稳定便捷方法。物理删除使用 `DeleteOption.PHYSICAL` 或 PojoDelete `.physical()`。

## 8. 硬约束

1. 查询列用 `.select(...)`，不是 `.columns(...)`。
2. 复合条件用 `.ands(...)` / `.ors(...)`，不是 `.and(...)` / `.or(...)`。
3. `DB.jdbc` 用 `?`；`DB.sql` 和条件 `sql()` 用 `#{key}`。
4. 批量入口是 `DB.batch.insert(...)`，返回 `BatchResult`，不是 boolean。
5. 业务代码不依赖 `com.dlz.db.internal.*`。
6. Spring Boot Starter 自动装配，不要生成继承框架配置类的代码。
