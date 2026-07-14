# dlz-db API 8.0 LTS 升级实施文档

> 状态：P0 契约已冻结，进入 P1 公共 API 骨架实施。  
> 总计划：`dlz-db-API开发计划V2.md`  
> 设计基线：`dlz-db-API设计V2.md`

## 实施门禁

必须按 `P0 → P1 → P2 → P3 → P4 → P5 → P6` 顺序实施。P0 未完成前，仅允许修改设计、ADR、签名清单和契约测试，不允许批量重命名或替换 public API。

当前禁止直接实施以下旧条目：

1. 8.0 直接采用新 API，不保留 7.x 兼容行为；旧 API 不纳入 8.0 设计范围。
2. 不得把当前 `updateById(T)` 的返回类型原地改为 `int`。
3. 不得新增作为查询终端的 `page(int,int)`，应采用 `page(PageRequest)`。
4. 不得删除或替换现有大写 `DB` 门面字段；8.0 新入口与 7.x 兼容方式须先定稿。
5. 不得直接修改 `SqlBuildInterceptor` 现有方法签名；先引入 `SqlBuildContext` 和 default bridge。
6. 不得将“按 ID 值选择 insert/update”实现命名为 `upsert`。
7. 不得静默忽略不适用的 Option。
8. 不得把批处理 JDBC 返回值简单求和为总影响行数。
9. 不得在事务底层接口尚未扩展前实现 `runReadOnly` 或隔离级别快捷方法。
10. 不得移除二级门面扩展能力并声称 Option 可以替代全部扩展。

## 分阶段实施顺序

| 阶段 | 实施内容 | 验收后才能进入 |
|---|---|---|
| P0 | 契约、签名、ADR、迁移边界 | P1 |
| P1 | 公共 API 骨架、返回类型、异常类型 | P2 |
| P2 | Pojo/Table 与条件 DSL | P3 |
| P3 | Jdbc/Sql 与模板门面 | P4 |
| P4 | Tx/Ds/Config/SPI | P5 |
| P5 | BatchResult 与批处理策略 | P6 |
| P6 | 全量迁移、TCK、兼容检查和发布 | 8.0 RC |

---

## 一、命名规则

**带 `Wrapper` 后缀 = 返回构造器（需终结方法）；不带 = 直接执行。** 零例外。

## 二、门面总览

```
DB.pojo    实体驱动    4 wrapper + 9 快捷操作
DB.table   表名驱动    4 wrapper + 9 快捷操作
DB.jdbc    原生SQL(?占位)  2 wrapper + 6 快捷操作
DB.sql     命名参数SQL(#{}占位)  2 wrapper + 6 快捷操作
DB.batch   批量操作    3 组快捷操作（直接执行）
DB.tx      事务
DB.ds      数据源
DB.config  超级配置入口（插件/参数/启动项）
```

## 三、Pojo / Table API

### 3.1 Wrapper（4 个，CRUD 对称）

| 操作 | Pojo（参数 Class） | Table（参数 String） |
|------|-------------------|---------------------|
| 查 | `selectWrapper(Class<T>)` | `selectWrapper(String table)` |
| 增 | `insertWrapper(Class<T>)` | `insertWrapper(String table)` |
| 改 | `updateWrapper(Class<T>)` | `updateWrapper(String table)` |
| 删 | `deleteWrapper(Class<T>)` | `deleteWrapper(String table)` |

```java
// Pojo 查询（selectWrapper 指定字段重载）
PojoQuery<T> selectWrapper(Class<T> clazz, DlzFn<T,?>... fields);
```

### 3.2 快捷操作（9 个，直接执行，支持 DbOption...）

| 动作 | Pojo | Table | 返回 |
|------|------|-------|------|
| insert | `insert(T, DbOption...)` | `insert(table, JSONMap, DbOption...)` | T / int |
| insertOrUpdateById | `insertOrUpdateById(T, DbOption...)` | `insertOrUpdateById(table, idColumn, JSONMap, DbOption...)` | T / int |
| selectById | `selectById(Class, id, DbOption...)` | `selectById(table, idColumn, id, DbOption...)` | T / ResultMap |
| selectByIds | `selectByIds(Class, ids)` | `selectByIds(table, idColumn, ids)` | List |
| updateById | `updateById(T, DbOption...)` | `updateById(table, idColumn, JSONMap, DbOption...)` | int |
| deleteById | `deleteById(Class, id, DbOption...)` | `deleteById(table, idColumn, id, DbOption...)` | int |
| deleteByIds | `deleteByIds(Class, ids)` | `deleteByIds(table, idColumn, ids)` | int |
| existsById | `existsById(Class, id)` | `existsById(table, idColumn, id)` | boolean |
| count | `count(Class)` | `count(table)` | long |

动态 Table 的 ById 操作必须显式传入主键列，或使用已注册主键的 `TableRef`；不得猜测默认 `id`。

## 四、Jdbc / Sql API

### 4.1 Wrapper（2 个）

| | Jdbc（`?` 占位） | Sql（`#{}` 占位） |
|---|---|---|
| 查 | `selectWrapper(sql, Object... args)` | `selectWrapper(sqlKey)` |
| 写 | `executeWrapper(sql, Object... args)` | `executeWrapper(sqlKey)` |

### 4.2 快捷操作（6 个，直接执行）

| 操作 | Jdbc（Object... args） | Sql（Map params） | 返回 |
|------|----------------------|-------------------|------|
| execute | `execute(sql, args)` | `execute(sqlKey, Map)` | int |
| executeAndReturnId | `executeAndReturnId(sql, args)` | `executeAndReturnId(sqlKey, Map)` | Long |
| one | `one(sql, args)` / `one(sql, Class, args)` | `one(sqlKey, Map)` / `one(sqlKey, Map, Class)` | ResultMap / R |
| list | `list(sql, args)` / `list(sql, Class, args)` | `list(sqlKey, Map)` / `list(sqlKey, Map, Class)` | List |
| count | `count(sql, args)` | `count(sqlKey, Map)` | long |
| page | `page(sql, PageRequest, args)` / `pageAs(sql, PageRequest, Class, args)` | `page(sqlKey, Map, PageRequest)` / `pageAs(sqlKey, Map, PageRequest, Class)` | Page |

```java
// 快捷（高频首选）
User u = DB.jdbc.one("SELECT * FROM user WHERE id=?", User.class, 1L);
List<User> list = DB.sql.list("user.findByStatus", JSONMap.of("status",1), User.class);
int n = DB.jdbc.execute("UPDATE user SET status=? WHERE id=?", 0, 1L);

// Wrapper（需链式配置时用）
Page<User> p = DB.jdbc.selectWrapper("SELECT * FROM user WHERE status=?", 1)
    .orderBy("create_time desc").page(PageRequest.of(1, 20), User.class);
```

## 五、查询返回值规则

| 调用 | PojoQuery\<T\> | TableQuery / JdbcQuery / SqlQuery |
|------|---------------|-----------------------------------|
| `one()` | **T**（泛型绑定，Bean 直返） | ResultMap |
| `one(Class<R>)` | R（覆盖泛型） | R |
| `list()` | **List\<T\>** | List\<ResultMap\> |
| `list(Class<R>)` | List\<R\> | List\<R\> |
| `page(PageRequest)` | Page\<T\> | Page\<ResultMap\> |
| `page(PageRequest, Class<R>)` | Page\<R\> | Page\<R\> |
| `count()` | long | long |
| `exists()` | boolean | boolean |
| `oneNonNull()` | T / ResultMap | 无结果抛异常 |
| `firstColumn()` | Object | Object |
| `firstColumn(Class)` | R | R |

Bean 直接返回只有 Pojo 有（泛型绑定）。其余系 `one()` 返回 ResultMap，要 Bean 传 `Class`。

## 六、写操作终结方法

| 方法 | 返回 | 适用 |
|------|------|------|
| `execute()` | int | 所有写 wrapper |
| `executeAndReturnId()` | Long | Insert wrapper |

## 七、条件构造方法（所有 wrapper 通用）

```java
.eq / .ne / .gt / .ge / .lt / .le           // 支持 (boolean is, column, value) 三参动态版
.like(column, value)                          // '%value%'
.startsWith(column, value)                   // 'value%'   ← 原 likeRight
.endsWith(column, value)                     // '%value'   ← 原 likeLeft
.notLike(column, value)
.in(column, collection) / .notIn(column, collection)
.between(column, low, high) / .notBetween(column, low, high)
.isNull(column) / .isNotNull(column)
.op(String operator, column, value)          // 通用操作符出口
.and(lambda)  / .or(lambda)                  // 嵌套分组，方法名=括号内连接符
.sql(String fragment, Object... args)        // 原生 SQL 片段
.auto(Object bean) / .auto(Map)              // 动态条件，非空字段→eq
```

## 八、PojoInsert Wrapper 链式方法

```java
DB.pojo.insertWrapper(User.class)
    .entity(user)                    // 单条
    .execute();

DB.pojo.insertWrapper(User.class)
    .entities(userList).size(500)    // 批量
    .execute();

DB.pojo.insertWrapper(User.class)
    .entity(user)
    .executeAndReturnId();           // 返回自增 id
```

## 九、Batch API（直接执行，返回 BatchResult）

```java
// Pojo 批量
BatchResult insert(List<T> entities);
BatchResult insert(List<T> entities, int batchSize);
BatchResult update(List<T> entities);
BatchResult update(List<T> entities, int batchSize);

// Table 批量
BatchResult insert(String table, List<JSONMap> values);
BatchResult insert(String table, List<JSONMap> values, int batchSize);
BatchResult update(String table, List<JSONMap> values);
BatchResult update(String table, List<JSONMap> values, int batchSize);

// 原生 SQL 批量
BatchResult execute(String sql, List<Object[]> paramList);
BatchResult execute(String sql, List<Object[]> paramList, int batchSize);
```

## 十、Tx / Ds API

```java
// 事务
DB.tx.run(() -> { ... });
DB.tx.call(() -> value);
DB.tx.run(TxOptions options, () -> { ... });
DB.tx.call(TxOptions options, () -> value);

// 数据源
DB.ds.use("slave", () -> { ... });
DB.ds.get("slave");
DB.ds.get();
```

## 十一、Config API（超级配置入口）

```java
// 插件链
DB.config.plugin(SqlBuildInterceptor interceptor);

// 运行时参数
DB.config.dialect(DbTypeEnum dialect);
DB.config.sql(String key, String sql);             // 命名 SQL 注册/热更新
DB.config.logicDeleteField(String field);
DB.config.columnNameConvertor(Function<String,String> convertor);

// 启动初始化（通常由 starter 自动调用）
DB.config.init(DlzDbProperties props,
               Supplier<DataSource> dsMaker,
               Supplier<ISqlExecutor> execMaker,
               Function<DataSource, ITxExecutor> txMaker);
DB.config.dataSource(DataSource ds);
DB.config.sqlExecutor(ISqlExecutor executor);
```

## 十二、Option 体系

### 12.1 顶层接口

```java
public interface DbOption {}   // 标记接口
```

### 12.2 四类内置 Option

```java
// InsertOption
InsertOption.IGNORE_NULL                    // 忽略 null 字段
InsertOption.INCLUDE_NULL                   // 显式插入 NULL
InsertOption.INCLUDE_FIELDS(DlzFn... fs)    // 只插指定字段
InsertOption.IGNORE_FIELDS(DlzFn... fs)     // 排除指定字段
InsertOption.NO_AUTO_KEY                    // 不回填自增 id
InsertOption.IGNORE_ON_DUPLICATE            // INSERT IGNORE
InsertOption.UPDATE_ON_DUPLICATE(DlzFn...)  // ON DUPLICATE KEY UPDATE

// UpdateOption
UpdateOption.IGNORE_NULL
UpdateOption.INCLUDE_FIELDS(DlzFn... fs)
UpdateOption.OPTIMISTIC_LOCK                // 乐观锁

// DeleteOption
DeleteOption.LOGIC                          // 逻辑删除（默认）
DeleteOption.PHYSICAL                       // 强制物理删除

// SelectOption
SelectOption.FOR_UPDATE                     // SELECT ... FOR UPDATE
SelectOption.INCLUDE_DELETED                // 包含已逻辑删除记录
```

### 12.3 用法

```java
DB.pojo.insert(user, InsertOption.IGNORE_NULL);
DB.pojo.updateById(user, UpdateOption.OPTIMISTIC_LOCK);
DB.pojo.deleteById(User.class, 1L, DeleteOption.PHYSICAL);
DB.pojo.selectById(User.class, 1L, SelectOption.FOR_UPDATE);

// 混用框架 Option + 插件 Option
DB.pojo.insert(user, InsertOption.IGNORE_NULL, AuditOption.SKIP);
```

### 12.4 插件自定义 Option

```java
// 插件定义自己的 Option（实现 DbOption）
public final class AuditOption implements DbOption {
    public static final AuditOption SKIP = new AuditOption();
}

// 注册插件后，任何操作都可传该 Option
DB.config.plugin(new AuditInterceptor());
DB.pojo.insert(user, AuditOption.SKIP);    // 插件在钩子里识别
```

### 12.5 插件钩子签名（通过 SqlBuildContext 演进）

```java
public interface SqlBuildContext {
    String table();
    Map<String, Object> values();
    Condition where();
    List<DbOption> options();
}

public interface SqlBuildInterceptor {
    default void onBuild(SqlBuildContext context) {
        // 新 SPI 默认入口
    }
}
```

旧 SPI 若需兼容，必须通过独立适配器或 default bridge 委托，不得直接修改既有 public 方法签名。

### 12.6 类型安全说明

方法签名统一 `DbOption...`（非各操作专属类型）。每个 Option 必须声明适用操作；传入不适用、重复、冲突或 `null` 的 Option 必须抛参数异常，禁止静默忽略。

## 十三、迁移对照表

### 13.1 方法改名

| 旧 API | 新 API |
|--------|--------|
| `DB.pojo.selectWrapper(Class)` | `DB.pojo.selectWrapper(Class)` |
| `DB.pojo.update(Class)` | `DB.pojo.updateWrapper(Class)` |
| `DB.pojo.delete(Class)` | `DB.pojo.deleteWrapper(Class)` |
| `DB.table.select(table)` | `DB.table.selectWrapper(table)` |
| `DB.table.update(table)` | `DB.table.updateWrapper(table)` |
| `DB.table.delete(table)` | `DB.table.deleteWrapper(table)` |
| `DB.table.insert(table)` | `DB.table.insertWrapper(table)` |
| `DB.jdbc.query(sql, args)` | `DB.jdbc.selectWrapper(sql, args)` |
| `DB.sql.query(sqlKey)` | `DB.sql.selectWrapper(sqlKey)` |
| `DB.sql.execute(sqlKey)` | `DB.sql.executeWrapper(sqlKey)` |
| `DB.pojo.save(T)` | `DB.pojo.insertOrUpdateById(T)` |
| `DB.table.save(table, map)` | `DB.table.insertOrUpdateById(table, idColumn, map)` |

### 13.2 不变的方法

| API | 说明 |
|-----|------|
| `DB.pojo.insert(T)` | 不变（快捷操作） |
| `DB.jdbc.execute(sql, args)` | 不变（快捷操作） |
| `selectById / updateById / deleteById / existsById / count` | 不变 |
| `eq / ne / gt / ge / lt / le / like / in / between / isNull / ...` | 不变 |

### 13.3 终结方法改名

| 旧 | 新 |
|----|-----|
| `queryOne()` | `one()` |
| `queryList()` | `list()` |
| `.queryPage(PageRequest)` | `page(PageRequest)` |
| `queryBean()` | `one()` |
| `queryBeanList()` | `list()` |
| `queryBeanPage(PageRequest)` | `page(PageRequest)` |

### 13.4 条件方法改名

| 旧 | 新 |
|----|-----|
| `likeLeft(col, val)` | `endsWith(col, val)` |
| `likeRight(col, val)` | `startsWith(col, val)` |
| `ands(lambda)` | `and(lambda)` |
| `ors(lambda)` | `or(lambda)` |

### 13.5 新增

| API | 说明 |
|-----|------|
| `DB.pojo.insertWrapper(Class)` | Pojo 插入构造器（新增） |
| `DB.table.insert(table, map)` | Table insert 快捷操作（补全，与 Pojo 对齐） |
| `DB.jdbc.selectWrapper / executeWrapper` | Jdbc wrapper（新增） |
| `DB.jdbc.one / list / count / page` | Jdbc 快捷查询（新增） |
| `DB.sql.selectWrapper / executeWrapper` | Sql wrapper（新增） |
| `DB.sql.one / list / count / page / execute` | Sql 快捷操作（新增） |
| `DbOption` 接口 + 四类 Option | Option 扩展体系（新增） |
| `DB.batch.insert(table, mapList)` 等 | Table 批量（补全） |

### 13.6 废弃

| 旧 API | 处理 |
|--------|------|
| `ICommService` 服务层方法（`service.getBean(wrapper, Class)` 等） | `@Deprecated`，内部委托兼容模块中的新 API；移除时间另行按 8.x/9.0 评审 |
| `DB.config.extend(name, facade)` / `ext(name)` | 移除，改用 DbOption 体系 |

## 十四、文件修改清单

| 文件 | 修改内容 |
|------|----------|
| `DB.java` | 冻结，添加 LTS 注释 |
| `DbPojo.java` | `select`→`selectWrapper`；新增 `insertWrapper/updateWrapper/deleteWrapper`；快捷操作加 `DbOption...`；`save`→`insertOrUpdateById` |
| `DbTable.java` | 4 wrapper 统一命名；快捷操作加 `DbOption...`；补 `insert(table,map)`；`save`→`insertOrUpdateById`，ById 显式传入 `idColumn` |
| `DbJdbc.java` | `query`→`selectWrapper`；新增 `executeWrapper`；补快捷操作 `execute/one/list/count/page` |
| `DbSql.java` | `query`→`selectWrapper`；新增 `executeWrapper`；补快捷操作 `execute/one/list/count/page`（参数走 Map） |
| `DbConfig.java` | 去掉 `extend/ext`；补全启动项/参数配置 |
| `DbBatch.java` | 方法名去前缀改重载；返回 `BatchResult`；补 Table 批量；修复 jdbcExecute 分批逻辑 |
| `PojoInsert.java` | 新增 `.entity(T)/.entities(List)/.executeAndReturnId()`；修复 `||` bug（→`&&`）；重命名 `doAutoId`→`skipAutoKey` |
| 各 Query wrapper | 终结方法统一 `one/list/page/count/exists`；废弃 `queryOne/queryList/queryBean*` |
| `ICondAddBy*` 接口 | `likeLeft`→`endsWith`；`likeRight`→`startsWith`；`ands`→`and`；`ors`→`or` |
| `DbOperateEnum` | 对应枚举值同步改名 |
| `ICommService` | `@Deprecated`，内部委托 wrapper 终结方法 |
| `SqlBuildInterceptor` | 引入 `SqlBuildContext`，通过 default bridge 或独立适配器演进 |
| `DbOption.java` | **新增**：Option 顶层标记接口 |
| `InsertOption.java` | **新增**：实现 DbOption，补全所有选项 |
| `UpdateOption.java` | **新增** |
| `DeleteOption.java` | **新增** |
| `SelectOption.java` | **新增** |

## 十五、Bug 修复清单

| 位置 | 问题 | 修复 |
|------|------|------|
| `PojoInsert.batch` L53 | `idInfo != null \|\| idInfo.getType() != IdType.AUTO` 用 `\|\|`，idInfo==null 时 NPE | 改 `&&` |
| `PojoInsert.batch` | `doAutoId` 命名与逻辑相反 | 重命名 `skipAutoKey` |
| `DbBatch.jdbcExecute` | `batchSize` 在循环中被修改 | 改用索引遍历，不修改入参 |
| `DbBatch` 返回值 | 返回 `boolean` 信息量不足 | 改返回 `int`（总影响行数） |

## 十六、版本路线

| 版本 | 内容 |
|------|------|
| 8.0.0 | 新 API 上线；7.x 通过独立兼容模块或迁移工具处理，不在同一门面原地伪兼容 |
| 8.x | 主门面签名和行为冻结，仅增强二级门面与实现能力 |
| 9.0 | 另行进行破坏性变更评审 |
