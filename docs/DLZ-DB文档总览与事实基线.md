# DLZ-DB 文档总览与事实基线

**基线版本：** v8.0.0
**基线原则：** 当前 Java 源码优先；升级计划中的内容不是当前 API。

## 一、当前 `DB` 门面入口

| 入口 | 代码对象 | 当前职责 |
|---|---|---|
| `DB.pojo` | `DbPojo` | 实体类查询、插入、按 ID 更新和删除 |
| `DB.table` | `DbTable` | 按表名直接操作 |
| `DB.jdbc` | `DbJdbc` | `?` 占位符的原生 JDBC 查询和执行 |
| `DB.sql` | `DbSql` | 预置 SQL、`#{key}` 参数查询和执行 |
| `DB.batch` | `DbBatch` | 批量插入、批量更新、JDBC 批量执行 |
| `DB.ds` | `DBDynamic` | 数据源注册、切换、获取和删除 |
| `DB.tx` | `DBTx` | 编程式事务 |
| `DB.config` | `DbConfig` | 数据库配置 |

门面字段名称区分大小写。当前代码使用小写入口；`DB.Table`、`DB.Jdbc`、`DB.Sql`、`DB.Batch`、`DB.Dynamic`、`DB.Tx` 不是当前代码中的入口名称。

## 二、当前 CRUD API

### `DB.pojo`

```java
DB.pojo.selectWrapper(User.class)
DB.pojo.insert(entity)
DB.pojo.insertOrUpdateById(entity)
DB.pojo.selectById(User.class, id)
DB.pojo.selectByIds(User.class, ids)
DB.pojo.updateWrapper(User.class)
DB.pojo.updateWrapper(entity)
DB.pojo.updateById(entity)
DB.pojo.deleteWrapper(User.class)
DB.pojo.deleteById(User.class, id)
DB.pojo.deleteByIds(User.class, ids)
DB.pojo.existsById(User.class, id)
```

`add` 和 `save` 仍存在但已废弃，不应在新文档中作为推荐 API。

Pojo/Table 查询 Wrapper 的查询列方法是 `.select(...)`；复合条件是 `.ands(...)` 和 `.ors(...)`。

### `DB.table`

```java
DB.table.selectWrapper("user")
DB.table.insert("user", values)
DB.table.insertWithAutoKey("user", values)
DB.table.insertOrUpdate("user", values)
DB.table.selectById("user", id)
DB.table.selectByIds("user", ids)
DB.table.updateById("user", values)
DB.table.deleteById("user", id)
DB.table.deleteByIds("user", ids)
```

链式操作使用 `insertWrapper`、`updateWrapper`、`deleteWrapper`，不是 `insert`、`update`、`delete` 构造器方法。

### `DB.jdbc`

```java
DB.jdbc.execute(sql, params)
DB.jdbc.one(sql, params)
DB.jdbc.first(sql, params)
DB.jdbc.list(sql, params)
DB.jdbc.count(sql, params)
DB.jdbc.page(sql, pageRequest, params)
DB.jdbc.selectWrapper(sql, params)
DB.jdbc.executeWrapper(sql, params)
```

### `DB.sql`

```java
DB.sql.execute(sqlKey, params)
DB.sql.one(sqlKey, params)
DB.sql.first(sqlKey, params)
DB.sql.list(sqlKey, params)
DB.sql.count(sqlKey, params)
DB.sql.selectWrapper(sqlKey, params)
DB.sql.executeWrapper(sqlKey, params)
```

当前 `DbSql` 未提供快捷 `page` 方法；分页应使用 `selectWrapper(...).page(...).queryPage()`。

## 三、批量和删除边界

`DB.batch` 当前提供：

- Bean 批量插入：`insert(List<T>)`、`insert(List<T>, int)`
- Bean 批量更新：`update(List<T>)`、`update(List<T>, int)`
- 表批量插入和更新
- 原生 JDBC 批量执行：`execute(String, List<Object[]>)`

当前没有 `DB.batch.delete(...)`。批量删除使用 `DB.pojo.deleteByIds(...)`、`DB.table.deleteByIds(...)` 或 Wrapper 的 `in(...)` 条件。

## 四、返回值基线

- `DB.pojo.insert(entity)`：返回传入实体
- `DB.pojo.updateById(...)`、`deleteById(...)`：返回影响行数 `int`
- `DB.table.insert(...)`、`updateById(...)`、`deleteById(...)`：返回影响行数 `int`
- `DB.table.insertWithAutoKey(...)`：返回自动生成主键 `Long`
- `DB.batch.*(...)`：返回 `BatchResult`
- `one`：严格返回单条 `ResultMap` 或指定类型对象；无结果返回 `null`，多条抛出非唯一结果异常
- `queryOne` / `queryBean`：严格模式
- `queryFirst` / `queryFirst(Class)` / `queryFirstBean`：非严格模式，无结果返回 `null`，多条返回第一条
- `list`：返回列表
- `count`：返回 `long`
- `page`：返回 `Page`

## 五、参数约束基线

- SQL、SQL Key 不能为空
- 实体类型、实体对象、主键 ID 不能为 `null`
- ID 集合不能为 `null`；空集合应避免生成无意义的 `IN`
- `batchSize` 必须是有效正数
- `DB.jdbc` 使用 `?` 占位符
- `DB.sql` 使用 `#{key}` 占位符
- `in` 应传集合、CSV 字符串或项目支持的子查询表达式，不能传单个普通值
- `DB.ds.use(...)` 只切换数据源，不自动开启事务；事务使用 `DB.tx`
- `DB.pojo.insertOrUpdateById(...)` 与 `DB.table.insertOrUpdate(...)` 按主键是否为空选择 INSERT/UPDATE，不是数据库原子 Upsert
- Wrapper 主要面向单表；JOIN、CTE、UNION、窗口函数和复杂聚合使用 `DB.jdbc` 或 `DB.sql`
- 当前事务只承诺单数据源本地事务，跨数据源不提供原子提交/回滚

## 六、文档状态约定

- 根目录 `README.md`、`README_EN.md`、`llms.txt` 和第 1～7 章描述当前 v8.0.0 能力。
- `docs/第08章-升级计划/` 只描述未来设计，文档中的新门面和新方法不得当作当前 API 使用。
- 旧版本迁移文档可以保留旧写法，但必须标明“旧 API”并给出当前替代写法。
- 用户无需自行编写 Mapper、DAO 或 XML；框架提供的 Wrapper 是当前公开的链式 API，可以直接使用。

## 七、公共 API、SPI 和 internal 边界

- 业务 API：`com.dlz.db`、`com.dlz.db.wrapper`、`com.dlz.db.model`、`com.dlz.db.option`、`com.dlz.db.core.anno`。
- 框架扩展 SPI：`com.dlz.db.core.ISqlExecutor`、`ITxExecutor`、`DlzDbAdapter`、`com.dlz.db.dialect.DbDialect`、`DialectRegistry` 和 `SqlBuildInterceptor`。
- 实现详情：`com.dlz.db.internal.*`，新业务代码和教程不应依赖。
- 当前 Wrapper 的类继承与部分签名仍会暴露 internal 类型，这是现存代码边界，不应通过用户文档继续扩大。特别是 `Condition` 目前位于 `com.dlz.db.internal.condition`，教程只展示 Wrapper 上的条件方法。

## 八、框架集成基线

- Spring Boot 当前自动装配入口是 `SpringDlzDbAutoConfiguration`，应用无需继承配置类。
- Solon 当前 SPI 入口是 `DlzDbSolonPlugin`，必须从容器获取 `DataSource` Bean 后才能完成初始化。
- 库主体目标 Java 8，Spring Boot 3 Demo 需要 JDK 17；聚合构建所有 Demo 时使用 JDK 17+。
