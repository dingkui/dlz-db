# dlz-db API 设计 V2（8.0 LTS）

> **版本**：8.0 LTS  
> **目标**：8.x 生命周期内主门面 `DB` 的方法签名和行为契约零破坏性变更，扩展优先通过二级门面进行  
> **状态**：契约冻结中，P0 验收前禁止实施源码改造  
> **日期**：2026-07-13  
> **执行计划**：`dlz-db-API开发计划V2.md`

## 设计勘误与冻结决策

本节优先级高于正文中尚未同步的旧描述；P0 完成时应将正文全部归一并删除勘误形式。

1. 本方案属于 `8.0` 破坏性大版本，直接采用新 API，不保留 `7.x` 兼容行为；旧 API 不纳入 8.0 设计范围。
2. `insert(T)` 返回完成主键回填后的实体，`updateById(T)` 返回影响行数；不通过 varargs 或返回类型变化伪造兼容。
3. 非原子“主键为空插入、非空更新”不得命名 `upsert`，统一称 `insertOrUpdateById`；真正 UPSERT 由 `onConflict(...)` 能力提供。
4. 查询分页终端使用 `page(PageRequest)`；`PageRequest` 使用 1-based 页码且只表示请求，`Page<T>` 只表示不可变结果；不得新增 `page(int,int)` 终端。
5. 动态 Table 的 `ById` 必须显式提供主键列，或传入已注册主键的 `TableRef`；标识符使用 `Column`/`SqlIdentifier` 等安全类型。
6. 错误、冲突、重复或为 `null` 的 Option 默认抛异常，禁止静默忽略；Option 执行顺序固定为校验、默认配置合并、用户 Option、生 SQL 构建、插件拦截、执行。
7. 插件演进使用 `SqlBuildContext`；旧 `SqlBuildInterceptor` 仅保留为过渡适配器，不作为 8.0 新开发入口。
8. 事务无返回值使用 `run`，有返回值使用 `call`；默认 `REQUIRED`，嵌套使用保存点，异常回滚，默认禁止跨数据源。
9. 批处理返回 `BatchResult`，默认失败即停止；外层事务回滚；无外层事务不回滚已完成批次；不得把未知影响行数和部分失败简单求和为 `int`。
10. Option 只修饰已有操作，不能替代聚合、元数据、模板等二级门面扩展。

---

## 一、设计目标与原则

### 1.1 五条不可动摇的原则

| # | 原则 | 含义 |
|---|------|------|
| P1 | **动词语义全局唯一** | 同一个动词在所有门面里只有一个含义 |
| P2 | **Wrapper 后缀即构造器** | 方法名带 `Wrapper` 后缀 = 返回构造器（需终结方法）；不带 = 直接执行 |
| P3 | **高频动作零摩擦** | `insert / insertOrUpdateById / selectById / updateById / deleteById` 五个最高频动作一行完成 |
| P4 | **终结方法全局统一** | 查询 wrapper 终结 `one/list/page/count/exists`；写 wrapper 终结 `execute` |
| P5 | **扩展不修改主门面** | 新能力通过二级门面注入，`DB` 主门面冻结 |

### 1.2 命名总规则（一条规则，零例外）

```
方法名带 Wrapper 后缀  →  返回构造器，必须以终结方法收尾（不产生副作用）
方法名不带 Wrapper     →  直接执行，返回业务结果
```

### 1.3 门面 × Wrapper 矩阵（极度对称）

| 门面 | 查 | 增 | 改 | 删 | 写(通用) | 快捷操作 |
|------|----|----|----|----|----------|----------|
| `DB.pojo` | `selectWrapper(Class)` | `insertWrapper(Class)` | `updateWrapper(Class)` | `deleteWrapper(Class)` | — | insert/insertOrUpdateById/selectById/updateById/deleteById/existsById/count |
| `DB.table` | `selectWrapper(String)` | `insertWrapper(String)` | `updateWrapper(String)` | `deleteWrapper(String)` | — | insert/insertOrUpdateById/selectById/updateById/deleteById/existsById/count |
| `DB.jdbc` | `selectWrapper(sql,args)` | — | — | — | `executeWrapper(sql,args)` | execute/one/list/count/page |
| `DB.sql` | `selectWrapper(sqlKey)` | — | — | — | `executeWrapper(sqlKey)` | execute/one/list/count/page |

**设计要点**：
- Pojo/Table 各 4 个 wrapper（CRUD 完整对称），Pojo 参数是 `Class<T>`，Table 参数是 `String table`
- Jdbc/Sql 各 2 个 wrapper（查 `selectWrapper` + 写 `executeWrapper`），因为原生 SQL 不区分增删改，只有"查"和"写"
- **四系都有快捷操作**：Pojo/Table 面向实体/表（insert/selectById/...），Jdbc/Sql 面向原生 SQL（execute/one/list/count/page）

---

## 二、现状问题诊断

| 问题 | 现状 | 危害 |
|------|------|------|
| `insert` 双义 | `DB.pojo.insert(T)` 直接执行；`DB.table.insert(table)` 返回 wrapper | 无法从方法名判断是否已执行 |
| `execute` 三义 | jdbc 直接执行 / sql 返回 wrapper / wrapper 终结方法 | 同名三层含义 |
| `select` 双义 | `select(Class)` 返回 wrapper；`selectById` 直接执行 | 词根复用歧义 |
| 终结方法不统一 | `queryOne/queryList` vs `one/list`；`queryBean/queryBeanList` 并存 | 两套 Bean 映射入口 |
| 新旧双执行路径 | wrapper 终结方法 vs `ICommService` 服务层 | 同一查询两种写法 |
| `likeLeft/likeRight` 反直觉 | `likeLeft` 实际生成 `'%value'`（左模糊，无法走索引） | 易误用，性能事故 |
| `PojoInsert.batch` 有 bug | `idInfo != null \|\| idInfo.getType()...` 用 `\|\|` 且 NPE 风险 | 批量插入自增 id 处理错误 |

---

## 三、核心设计：Wrapper 家族

### 3.1 Pojo / Table 的 4 个 Wrapper（CRUD 对称）

| 操作 | Pojo 系（参数 Class） | Table 系（参数 String） | 返回类型 |
|------|----------------------|------------------------|----------|
| 查 | `selectWrapper(Class<T>)` | `selectWrapper(String table)` | `PojoQuery<T>` / `TableQuery` |
| 增 | `insertWrapper(Class<T>)` | `insertWrapper(String table)` | `PojoInsert<T>` / `TableInsert` |
| 改 | `updateWrapper(Class<T>)` | `updateWrapper(String table)` | `PojoUpdate<T>` / `TableUpdate` |
| 删 | `deleteWrapper(Class<T>)` | `deleteWrapper(String table)` | `PojoDelete<T>` / `TableDelete` |

**对称性**：Pojo 和 Table 的 4 个 wrapper 完全对称，唯一差异是参数类型（`Class<T>` vs `String`）。用户记住一套方法名，换门面即可。

**`selectWrapper` 选用 `select` 而非 `query` 的理由**：
- `select` 是 SQL 标准关键字（SELECT 语句），贴合数据库语义
- CRUD 对称：`insertWrapper / selectWrapper / updateWrapper / deleteWrapper`，四个 SQL 动词一一对应
- `selectWrapper` 与 `selectById` 词根复用，但靠 `Wrapper` 后缀区分（带=构造器，不带=直接执行），无歧义

**Pojo 系补全 `insertWrapper(Class)` 的理由**：
- 对称完整：Pojo/Table 都有 4 个 wrapper
- 支持链式插入（批量、返回 id、指定字段）：
  ```java
  DB.pojo.insertWrapper(User.class).entity(user).execute();
  DB.pojo.insertWrapper(User.class).entities(list).size(500).execute();
  DB.pojo.insertWrapper(User.class).entity(user).executeAndReturnId();
  ```
- `insert(T)` 快捷操作覆盖最简单的单条插入场景，`insertWrapper(Class)` 覆盖需要链式的复杂场景

### 3.2 Jdbc / Sql 的 2 个 Wrapper（查 + 写）

| 操作 | Jdbc 系（`?` 占位） | Sql 系（`#{}` 命名参数） | 返回类型 |
|------|---------------------|--------------------------|----------|
| 查 | `selectWrapper(sql, args)` | `selectWrapper(sqlKey)` | `JdbcQuery` / `SqlQuery` |
| 写 | `executeWrapper(sql, args)` | `executeWrapper(sqlKey)` | `JdbcExecute` / `SqlExecute` |

**为什么 Jdbc/Sql 只有 2 个**：
- 原生 SQL 不区分增删改，只有"查"（返回数据）和"写"（返回影响行数）两类
- `selectWrapper` 构造查询，终结 `one/list/page`
- `executeWrapper` 构造写操作，终结 `execute()` → int

**`executeWrapper` 与终结方法 `execute()` 重名说明**：
- `executeWrapper(sql, args)` 是门面入口，返回构造器
- `wrapper.execute()` 是终结方法，触发执行
- 读法：`DB.jdbc.executeWrapper(sql, args).execute()` = "构造一个写操作 wrapper，然后执行它"
- 上下文不同（门面方法 vs wrapper 方法），无歧义

**Jdbc/Sql 也有快捷操作**：

Jdbc/Sql 本就是为快捷而设计，除了 wrapper 外也提供快捷操作（直接执行，返回结果）：

| 快捷操作 | Jdbc 系（`?` 占位） | Sql 系（`#{}` 命名参数） | 返回 |
|----------|---------------------|--------------------------|------|
| `execute` | `execute(sql, args)` | `execute(sqlKey, Map)` | `int` |
| `executeAndReturnId` | `executeAndReturnId(sql, args)` | `executeAndReturnId(sqlKey, Map)` | `Long` |
| `one` | `one(sql, args)` / `one(sql, Class, args)` | `one(sqlKey, Map)` / `one(sqlKey, Map, Class)` | `ResultMap` / `R` |
| `list` | `list(sql, args)` / `list(sql, Class, args)` | `list(sqlKey, Map)` / `list(sqlKey, Map, Class)` | `List<ResultMap>` / `List<R>` |
| `count` | `count(sql, args)` | `count(sqlKey, Map)` | `long` |
| `page` | `page(sql, pageNo, pageSize, args)` | `page(sqlKey, Map, pageNo, pageSize)` | `Page` |

- 参数齐全时用快捷操作（高频首选），需要链式配置（排序、动态参数）时用 wrapper
- Jdbc 参数用 `Object... args`（`?` 占位），Sql 参数用 `Map`（`#{}` 命名参数）

### 3.3 快捷操作（Terminal API，四系都有）

四系都有快捷操作（直接执行，不带 Wrapper 后缀）。Pojo/Table 面向实体/表，Jdbc/Sql 面向原生 SQL：

| 动作 | Pojo 系 | Table 系 | 返回 |
|------|---------|----------|------|
| `insert` | `insert(T, DbOption...)` | `insert(table, JSONMap, DbOption...)` | `T`（id 回填） / `int` |
| `insertOrUpdateById` | `insertOrUpdateById(T, DbOption...)` | `insertOrUpdateById(table, JSONMap, DbOption...)` | T / int |
| `selectById` | `selectById(Class, id, DbOption...)` | `selectById(table, id, DbOption...)` | `T` / `ResultMap` |
| `selectByIds` | `selectByIds(Class, ids)` | `selectByIds(table, ids)` | `List<T>` / `List<ResultMap>` |
| `updateById` | `updateById(T, DbOption...)` | `updateById(table, JSONMap, DbOption...)` | `int` |
| `deleteById` | `deleteById(Class, id, DbOption...)` | `deleteById(table, id, DbOption...)` | `int` |
| `deleteByIds` | `deleteByIds(Class, ids)` | `deleteByIds(table, ids)` | `int` |
| `existsById` | `existsById(Class, id)` | `existsById(table, id)` | `boolean` |
| `count` | `count(Class)` | `count(table)` | `long` |

**所有增删改查快捷操作都支持 `DbOption...`**（可变参数，可空）。Option 分四类：`InsertOption` / `UpdateOption` / `DeleteOption` / `SelectOption`，详见第十一章。传入不适用、重复、冲突或 `null` 的 Option 必须抛出参数异常，不得静默忽略。

**`insertOrUpdateById` 语义**：id 为空 → `insert`，id 非空 → `updateById`。该操作是非原子流程，不得称为 `upsert`；真正 UPSERT 通过 `onConflict(...)` 能力提供。

**`insert` 返回值差异说明**：
- Pojo `insert(T)` 返回 `T`（实体，id 已回填）——因为实体对象可修改属性回填 id
- Table `insert(table, map)` 返回 `int`（影响行数）——map 不便回填；需自增 id 时走 `insertWrapper(table).executeAndReturnId()`

**规则**：这些方法不带 `Wrapper` 后缀 = 直接执行。与 wrapper 形成互补——简单场景用快捷操作，复杂场景用 wrapper。

---

## 四、门面结构（冻结）

```java
public final class DB {
    private DB() {}
    public static final DbPojo   pojo   = new DbPojo();
    public static final DbTable  table  = new DbTable();
    public static final DbJdbc   jdbc   = new DbJdbc();
    public static final DbSql    sql    = new DbSql();
    public static final DbBatch  batch  = new DbBatch();
    public static final DbTx     tx     = new DbTx();
    public static final DbDs     ds     = new DbDs();
    public static final DbConfig config = new DbConfig();
}
```

**冻结承诺**：8 个字段在 V2 生命周期内永不增改删。新能力走 `DbOption` 体系、插件链。

**8 个子门面分类**：

| 分类 | 子门面 | 职责 |
|------|--------|------|
| **数据操作** | `pojo` / `table` / `jdbc` / `sql` | 4 套数据访问风格（实体/表名/原生SQL/命名参数SQL） |
| **批量** | `batch` | 批量插入/更新/原生批量执行 |
| **事务** | `tx` | 事务执行器（`run/call` 与 `TxOptions`） |
| **数据源** | `ds` | 动态数据源切换（use/get） |
| **超级配置入口** | `config` | 插件链 + 运行时参数 + 启动初始化 + 扩展注册 |

---

## 五、各门面详细 API

### 5.1 `DB.pojo`（实体驱动，Lambda 类型安全）

```java
public class DbPojo {

    // ============ 4 个 Wrapper（返回构造器）============

    /** 查询构造器（查全字段） */
    public <T> PojoQuery<T>  selectWrapper(Class<T> clazz);

    /** 查询构造器（指定查询字段） */
    public <T> PojoQuery<T>  selectWrapper(Class<T> clazz, DlzFn<T, ?>... fields);

    /** 插入构造器（链式：.entity(t).execute() / .entities(list).execute()） */
    public <T> PojoInsert<T> insertWrapper(Class<T> clazz);

    /** 更新构造器 */
    public <T> PojoUpdate<T> updateWrapper(Class<T> clazz);

    /** 删除构造器 */
    public <T> PojoDelete<T> deleteWrapper(Class<T> clazz);

    // ============ 快捷操作（直接执行，均支持 DbOption...）============

    public <T> T       insert(T entity, DbOption... opts);
    /** insertOrUpdateById：id 为空→insert，id 非空→updateById；非原子操作 */
    public <T> T       insertOrUpdateById(T entity, DbOption... opts);
    public <T> T       selectById(Class<T> clazz, Object id, DbOption... opts);
    public <T> List<T> selectByIds(Class<T> clazz, Collection<?> ids);
    public <T> int     updateById(T entity, DbOption... opts);
    public <T> int     deleteById(Class<T> clazz, Object id, DbOption... opts);
    public <T> int     deleteByIds(Class<T> clazz, Collection<?> ids);
    public <T> boolean existsById(Class<T> clazz, Object id);
    public <T> long    count(Class<T> clazz);
}
```

**使用示例**：

```java
// 快捷操作（直接执行）
User u = DB.pojo.insert(new User("tom", 20));   // id 回填
DB.pojo.updateById(user);
User u = DB.pojo.selectById(User.class, 1L);
DB.pojo.deleteById(User.class, 1L);

// Wrapper（链式构造）
List<User> list = DB.pojo.selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .gt(User::getAge, 18)
    .orderByDesc(User::getCreateTime)
    .list();

int n = DB.pojo.updateWrapper(User.class)
    .set(User::getName, "newName")
    .eq(User::getId, 1L)
    .execute();

int n = DB.pojo.deleteWrapper(User.class)
    .lt(User::getCreateTime, LocalDate.now().minusYears(1))
    .execute();

// 插入 Wrapper（链式：批量、返回 id）
DB.pojo.insertWrapper(User.class).entities(list).size(500).execute();
Long id = DB.pojo.insertWrapper(User.class).entity(user).executeAndReturnId();
```

### 5.2 `DB.table`（表名驱动，String 字段名）

```java
public class DbTable {

    // ============ 4 个 Wrapper ============
    public TableQuery  selectWrapper(String table);
    public TableInsert insertWrapper(String table);
    public TableUpdate updateWrapper(String table);
    public TableDelete deleteWrapper(String table);

    // ============ 快捷操作（与 Pojo 对齐，均支持 DbOption...）============
    public int               insert(String table, JSONMap value, DbOption... opts);
    /** insertOrUpdateById：id 为空→insert，id 非空→updateById；非原子操作 */
    public int               insertOrUpdateById(String table, JSONMap value, DbOption... opts);
    public ResultMap         selectById(String table, Object id, DbOption... opts);
    public List<ResultMap>   selectByIds(String table, Collection<?> ids);
    public int               updateById(String table, JSONMap value, DbOption... opts);
    public int               deleteById(String table, Object id, DbOption... opts);
    public int               deleteByIds(String table, Collection<?> ids);
    public boolean           existsById(String table, Object id);
    public long              count(String table);
}
```

**使用示例**：

```java
// 快捷（与 Pojo 对齐：insert / upsert / selectById / updateById / deleteById ...）
int n = DB.table.insertWrapper("user", JSONMap.of("name","tom","age",20));
int n = DB.table.insertOrUpdateById("user", "id", JSONMap.of("id",1,"name","x"));
ResultMap r = DB.table.selectById("user", 1L);
DB.table.updateById("user", JSONMap.of("id", 1, "name", "x"));

// Wrapper
List<ResultMap> list = DB.table.selectWrapper("user").eq("status", 1).list();
int n = DB.table.updateWrapper("user").set("name","x").eq("id",1L).execute();
int n = DB.table.deleteWrapper("user").eq("id",1L).execute();
int n = DB.table.insertWrapper("user").value("name","tom").value("age",20).execute();
Long id = DB.table.insertWrapper("user").value("name","tom").executeAndReturnId();
```

### 5.3 `DB.jdbc`（原生 SQL，`?` 占位）

Jdbc 系为快捷而设计，**既有 wrapper 又有快捷操作**：参数齐全时直接执行，需要链式配置时走 wrapper。

```java
public class DbJdbc {

    // ============ Wrapper（需要链式配置时用）============
    public JdbcQuery  selectWrapper(String sql, Object... args);
    public JdbcExecute executeWrapper(String sql, Object... args);

    // ============ 快捷操作（直接执行，高频首选）============

    /** 执行写 SQL，返回影响行数 */
    public int  execute(String sql, Object... args);

    /** 执行写 SQL，返回自增主键 */
    public Long executeAndReturnId(String sql, Object... args);

    /** 查一条，返回 ResultMap */
    public ResultMap       one(String sql, Object... args);
    /** 查一条，映射到指定类型 */
    public <R> R           one(String sql, Class<R> clazz, Object... args);

    /** 查列表，返回 List<ResultMap> */
    public List<ResultMap> list(String sql, Object... args);
    /** 查列表，映射到指定类型 */
    public <R> List<R>     list(String sql, Class<R> clazz, Object... args);

    /** 查条数 */
    public long            count(String sql, Object... args);

    /** 分页查询，返回 Page<ResultMap> */
    public Page<ResultMap> page(String sql, PageRequest request, Object... args);
    /** 分页查询，映射到指定类型 */
    public <R> Page<R>     page(String sql, PageRequest request, Class<R> clazz, Object... args);
}
```

**何时用快捷操作 vs wrapper**：

```java
// 快捷操作（参数齐全，直接执行——高频首选）
User u = DB.jdbc.one("SELECT * FROM user WHERE id=?", User.class, 1L);
List<User> list = DB.jdbc.list("SELECT * FROM user WHERE status=?", User.class, 1);
long cnt = DB.jdbc.count("SELECT count(*) FROM user WHERE status=?", 1);
int n = DB.jdbc.execute("UPDATE user SET status=? WHERE id=?", 0, 1L);

// Wrapper（需要链式追加条件/排序/分页配置时用）
Page<User> p = DB.jdbc.selectWrapper("SELECT * FROM user WHERE status=?", 1)
    .orderBy("create_time desc")
    .page(PageRequest.of(1, 20), User.class);
```

### 5.4 `DB.sql`（命名参数 SQL，`#{}` 占位）

Sql 系同 Jdbc，**既有 wrapper 又有快捷操作**。命名参数通过 `Map` 传入，无需 `.set()` 链式。

```java
public class DbSql {

    // ============ Wrapper（需要链式 .set() 配置时用）============
    public SqlQuery  selectWrapper(String sqlKey);
    public SqlExecute executeWrapper(String sqlKey);

    // ============ 快捷操作（直接执行，参数通过 Map 传入）============

    /** 执行写 SQL，返回影响行数 */
    public int  execute(String sqlKey, Map params);

    /** 执行写 SQL，返回自增主键 */
    public Long executeAndReturnId(String sqlKey, Map params);

    /** 查一条，返回 ResultMap */
    public ResultMap       one(String sqlKey, Map params);
    /** 查一条，映射到指定类型 */
    public <R> R           one(String sqlKey, Map params, Class<R> clazz);

    /** 查列表，返回 List<ResultMap> */
    public List<ResultMap> list(String sqlKey, Map params);
    /** 查列表，映射到指定类型 */
    public <R> List<R>     list(String sqlKey, Map params, Class<R> clazz);

    /** 查条数 */
    public long            count(String sqlKey, Map params);

    /** 分页查询，返回 Page<ResultMap> */
    public Page<ResultMap> page(String sqlKey, Map params, int pageNo, int pageSize);
    /** 分页查询，映射到指定类型 */
    public <R> Page<R>     page(String sqlKey, Map params, int pageNo, int pageSize, Class<R> clazz);
}
```

**何时用快捷操作 vs wrapper**：

```java
// 快捷操作（参数通过 Map 传入，直接执行——高频首选）
User u = DB.sql.one("user.findById", JSONMap.of("id", 1L), User.class);
List<User> list = DB.sql.list("user.findByStatus", JSONMap.of("status", 1), User.class);
long cnt = DB.sql.count("user.countByStatus", JSONMap.of("status", 1));
int n = DB.sql.execute("user.deactivate", JSONMap.of("id", 1L));

// Wrapper（需要链式 .set() 逐步设参，或动态条件时用）
Page<User> p = DB.sql.selectWrapper("user.query")
    .set("status", 1)
    .set("name", keyword)    // 动态参数，可能为 null
    .page(PageRequest.of(1, 20), User.class);
```

### 5.5 `DB.batch`（批量操作）

```java
public class DbBatch {

    // ============ Pojo 系批量（实体列表，默认 batchSize=1000）============
    public BatchResult insert(List<T> entities);
    public BatchResult insert(List<T> entities, int batchSize);
    public BatchResult update(List<T> entities);
    public BatchResult update(List<T> entities, int batchSize);

    // ============ Table 系批量（表名 + map 列表，与 Pojo 对齐）============
    public BatchResult insert(String table, List<JSONMap> values);
    public BatchResult insert(String table, List<JSONMap> values, int batchSize);
    public BatchResult update(String table, List<JSONMap> values);
    public BatchResult update(String table, List<JSONMap> values, int batchSize);

    // ============ 原生 SQL 批量（? 占位）============
    public BatchResult execute(String sql, List<Object[]> paramList);
    public BatchResult execute(String sql, List<Object[]> paramList, int batchSize);
}
```

**batch 是纯快捷操作，直接执行**（不返回 builder）：

batch 的唯一配置项是 `batchSize`，用参数传递即可，不需要链式 builder。这与 V2 快捷操作（`insert(T)` / `selectById` 等）同一类别——动作型入口，传入数据即执行，返回 `BatchResult`。

**为什么不用链式 `.size(n).execute()`**：

链式 builder 的价值在于"需要配置多个参数"（如查询的条件、排序、分页）。batch 只有一个配置项（batchSize），用参数比重载更直接：

```java
// 直接执行（简洁）
DB.batch.insert(userList, 500);

// 链式 builder（多两步，无额外收益）
DB.batch.insert(userList).size(500).execute();
```

若未来 batch 需要更多配置（错误处理策略、进度回调等），再引入 builder，不破坏当前方法签名（只增不改）。

**方法名用重载，不用前缀**：

与 Pojo/Table 快捷操作风格一致——靠参数类型区分（`List<T>` vs `String table, List<JSONMap>`），不用 `pojoInsert`/`tableInsert` 前缀。

**为什么不提供批量删除**：

`deleteWrapper().in(column, ids).execute()` 一条 SQL 搞定，比逐条 batch 高效。不同表删除走 `execute(sql, paramList)`。

**使用示例**：

```java
// Pojo 系批量
DB.batch.insert(userList);            // 默认 batchSize=1000
DB.batch.insert(userList, 500);       // 指定 batchSize
DB.batch.update(userList, 200);

// Table 系批量（与 Pojo 对齐）
DB.batch.insert("user", mapList, 500);
DB.batch.update("user", mapList, 200);

// 原生 SQL 批量
DB.batch.execute("INSERT INTO log(msg) VALUES(?)", paramList, 100);

// 批量删除用 in（一条 SQL，比 batch 高效）
DB.pojo.deleteWrapper(User.class).in(User::getId, idList).execute();
```

**实现要点**：
- 返回 `BatchResult`，记录批次数、已知影响行数、未知计数与失败位置；不得把 `SUCCESS_NO_INFO` 或部分失败简单求和为 `int`
- `batchSize <= 0` 抛出参数异常
- 分批逻辑用索引遍历，**不修改入参 batchSize**（避免循环中变量被污染）
- 空列表返回空的 `BatchResult`（不访问数据库）

**修复**：`PojoInsert.batch` 的 `||` bug 修正为 `&&`，重命名 `doAutoId` → `skipAutoKey`。

### 5.6 `DB.tx`（事务）

```java
public class DbTx {
    public void run(Runnable action);
    public <T> T call(Supplier<T> action);
    public void run(TxOptions options, Runnable action);
    public <T> T call(TxOptions options, Supplier<T> action);
}
```

```java
DB.tx.run(() -> {
    DB.pojo.insert(order);
    DB.pojo.updateById(stock);
    DB.jdbc.executeWrapper("UPDATE account SET balance = balance - ?", amount).execute();
    return null;
});
```

### 5.7 `DB.ds`（动态数据源）

```java
public class DbDs {
    public <T> T use(String dsName, Supplier<T> action);
    public DataSource get(String dsName);
    public DataSource get();
}
```

```java
DB.ds.use("slave", () -> {
    return DB.pojo.selectWrapper(User.class).eq(User::getId, 1L).one();
});
```

### 5.8 `DB.config`（超级配置入口）

`config` 是框架的**超级配置入口**，统一管理三类配置：插件、参数、启动项。所有链式返回 `this`，支持流式配置。

```java
public class DbConfig {

    // ============ 插件链 ============
    /** 注册 SQL 构建插件（逻辑删除、租户隔离、数据权限、审计字段） */
    public DbConfig plugin(SqlBuildInterceptor interceptor);

    // ============ 运行时参数 ============
    /** 数据库方言（MySQL/PostgreSQL/Oracle/...，影响分页 SQL 等） */
    public DbConfig dialect(DbTypeEnum dialect);

    /** 注册/修改命名 SQL（运行时可热更新，配合 DB.sql 使用） */
    public DbConfig sql(String key, String sql);

    /** 逻辑删除字段（设置后自动注册 LogicDeleteInterceptor） */
    public DbConfig logicDeleteField(String field);

    /** 列名转换器（驼峰 ↔ 下划线，覆盖默认策略） */
    public DbConfig columnNameConvertor(Function<String, String> convertor);

    // ============ 启动项（通常由 starter 自动调用，也可手动初始化）===========
    /** 初始化数据源 + 执行器 + 事务执行器（一次性） */
    public DbConfig init(DlzDbProperties props,
                         Supplier<DataSource> dataSourceMaker,
                         Supplier<ISqlExecutor> sqlExecutorMaker,
                         Function<DataSource, ITxExecutor> txExecutorMaker);

    /** 绑定数据源（手动模式） */
    public DbConfig dataSource(DataSource ds);

    /** 绑定 SQL 执行器（手动模式） */
    public DbConfig sqlExecutor(ISqlExecutor executor);
}
```

**配置分层说明**：

| 配置类型 | 何时配置 | 谁调用 | 示例 |
|----------|----------|--------|------|
| **启动项** | 应用启动时，一次性 | starter 自动 / 手动 `init()` | 数据源、执行器、方言 |
| **插件** | 启动时或运行时 | `DB.config.plugin(...)` | 逻辑删除、租户、数据权限 |
| **参数** | 运行时随时 | `DB.config.dialect(...)` 等 | 命名 SQL、列名转换 |
| **Option** | 每次调用时 | 快捷操作参数 `insert(T, DbOption...)` | 忽略 null、逻辑删除、加锁 |

**扩展机制：插件 + Option（取代通用门面注册）**

V2 不提供 `extend(name, facade)` 通用门面注册——实际场景中几乎没人需要注册一个全新的门面。真正的扩展需求是**给现有操作加行为选项**，通过 **插件 + Option** 组合满足：

- **插件**（`DB.config.plugin()`）：全局注册，自动介入所有操作（审计填充、租户过滤等）
- **Option**（`insert(T, DbOption...)`）：单次调用传入，控制本次操作行为（忽略 null、逻辑删除、加锁等）
- 插件可提供**自定义 Option**，用户传入后插件识别并执行对应逻辑

详见第十一章「Option 体系」。

**使用示例**：

```java
// 启动初始化（通常由 starter 自动完成，也可手动）
DB.config.init(props, () -> createDataSource(props), 
               () -> new JdbcSqlExecutor(), 
               ds -> new TxExecutor(ds));

// 插件注册
DB.config.plugin(new LogicDeleteInterceptor("deleted"));
DB.config.plugin(new TenantInterceptor("tenant_id"));
DB.config.plugin(new DataPermissionInterceptor());

// 运行时参数
DB.config.dialect(DbTypeEnum.MYSQL);
DB.config.sql("user.findByStatus", "SELECT * FROM user WHERE status = #{status}");
DB.config.logicDeleteField("deleted");

// Option 扩展（每次调用时传入，控制单次行为）
DB.pojo.insert(user, InsertOption.IGNORE_NULL);
DB.pojo.deleteById(User.class, 1L, DeleteOption.PHYSICAL);  // 强制物理删除
DB.pojo.selectById(User.class, 1L, SelectOption.FOR_UPDATE); // 加锁查询
```

---

## 六、Wrapper 终结方法统一约定

### 6.1 查询类 wrapper（`PojoQuery` / `TableQuery` / `JdbcQuery` / `SqlQuery`）

**返回值核心规则**：Pojo 系泛型已绑定 `T`，`one()`/`list()` 直接返回 Bean；其余系默认返回 `ResultMap`，需 Bean 时传 `Class`。

| 终结方法 | `PojoQuery<T>` | `TableQuery` / `JdbcQuery` / `SqlQuery` | 语义 |
|----------|----------------|------------------------------------------|------|
| `one()` | `T` | `ResultMap` | 单条查询，无结果返回 null |
| `one(Class<R>)` | `R`（覆盖泛型 T） | `R` | 单条查询并映射到指定类型 |
| `oneNonNull()` | `T` | `ResultMap` | 单条查询，无结果抛 `DbException` |
| `list()` | `List<T>` | `List<ResultMap>` | 列表查询 |
| `list(Class<R>)` | `List<R>` | `List<R>` | 列表查询并映射 |
| `page(PageRequest)` | `Page<T>` | `Page<ResultMap>` | 分页查询 |
| `page(PageRequest, Class<R>)` | `Page<R>` | `Page<R>` | 分页查询并映射 |
| `count()` | `long` | `long` | 条件计数 |
| `exists()` | `boolean` | `boolean` | 是否存在 |
| `firstColumn()` | `Object` | `Object` | 第一行第一列（标量） |
| `firstColumn(Class<R>)` | `R` | `R` | 第一行第一列并转型 |

**关键设计**：

1. **`one()` 的行为取决于 wrapper 是否绑定泛型**：
   - `PojoQuery<T>`：`one()` → `T`（Bean，构造时已传 `Class<T>`，泛型绑定）
   - `TableQuery` / `JdbcQuery` / `SqlQuery`：`one()` → `ResultMap`（无泛型绑定，默认 Map）

2. **`one(Class<R>)` 是统一的"显式指定类型"出口**：
   - 所有 wrapper 都支持，返回 `R`
   - PojoQuery 传 `one(Class)` 可覆盖泛型 T（跨类型映射，如 User → UserDTO）

3. **"Bean 直接返回只有 Pojo 有"**：
   - 只有 `PojoQuery<T>` 的 `one()`/`list()` 直接返回 Bean（泛型 T）
   - Table/Jdbc/Sql 必须传 `Class` 才能得到 Bean：`one(User.class)` / `list(User.class)`
   - 符合直觉：Pojo 系是"实体驱动"，知道目标类型；Table/Jdbc/Sql 是"数据驱动"，默认给 Map

```java
// Pojo 系：one() 直接返回 T（Bean）
User u = DB.pojo.selectWrapper(User.class).eq(User::getId, 1L).one();
List<User> list = DB.pojo.selectWrapper(User.class).eq(User::getStatus, 1).list();

// Table 系：one() 返回 ResultMap，传 Class 得 Bean
ResultMap r = DB.table.selectWrapper("user").eq("id", 1L).one();
User u = DB.table.selectWrapper("user").eq("id", 1L).one(User.class);
List<User> list = DB.table.selectWrapper("user").eq("status", 1).list(User.class);

// Jdbc/Sql 系：同 Table，one() 返回 ResultMap，one(Class) 返回 Bean
User u = DB.jdbc.selectWrapper("SELECT * FROM user WHERE id=?", 1L).one(User.class);
User u = DB.sql.selectWrapper("user.findById").set("id", 1L).one(User.class);
```

### 6.2 写操作 wrapper（`PojoInsert` / `PojoUpdate` / `PojoDelete` / `TableInsert` / `TableUpdate` / `TableDelete` / `JdbcExecute` / `SqlExecute`）

| 终结方法 | 返回 | 语义 |
|----------|------|------|
| `execute()` | `int` | 执行，返回影响行数 |
| `executeAndReturnId()` | `Long` | 执行插入并返回自增主键（仅 Insert） |

### 6.3 条件构造方法（所有 wrapper 通用）

```java
// 基础比较
.eq(column, value)                  eq(boolean is, column, value)
.ne(column, value)                  gt / ge / lt / le 同理

// 模糊匹配（修正命名）
.like(column, value)                // '%value%'
.startsWith(column, value)         // 'value%'   ← 原 likeRight
.endsWith(column, value)           // '%value'   ← 原 likeLeft
.notLike(column, value)

// 范围
.in(column, collection)  /  .notIn(column, collection)
.between(column, low, high)  /  .notBetween(column, low, high)

// 空值
.isNull(column)  /  .isNotNull(column)

// 自定义操作符
.op(String operator, column, value)

// 嵌套分组（方法名 = 括号内连接符）
.and(lambda)    // (A and B)
.or(lambda)     // (A or B)

// 原生 SQL 片段
.sql(String fragment, Object... args)

// 动态条件
.auto(Object bean)                 // 非空字段 → eq
.auto(Map<String,Object> map)
```

**关键修正**：
- `likeLeft` → `endsWith`（`'%value'`，匹配后缀）
- `likeRight` → `startsWith`（`'value%'`，匹配前缀，可走索引）
- `ands` → `and`，`ors` → `or`（单数）
- 保留与 MyBatis-Plus 相反的 and/or 语义（lambda 内部连接符），Javadoc 明确警告

---

## 七、扩展机制（二级门面）

主门面冻结后，新能力通过二级门面注入：

```java
DB.pojo.ext        // Pojo 扩展（聚合查询、DTO 投影）
DB.table.ext       // Table 扩展（聚合查询、元数据）
DB.sql.template    // 命名 SQL 模板扩展
```

**聚合查询**（主门面不提供 group by / having，走二级门面）：

```java
List<Map> result = DB.pojo.ext.aggr(User.class)
    .select("status, count(*) as cnt")
    .groupBy("status")
    .having("count(*) > ?", 10)
    .list();
```

**插件链**（保留现有机制）：

```java
DB.config.plugin(new LogicDeleteInterceptor("deleted"));
DB.config.plugin(new TenantInterceptor("tenant_id"));
```

---

## 八、迁移指南（旧 API → 新 API）

| 旧 API | 新 API | 说明 |
|--------|--------|------|
| `DB.pojo.selectWrapper(Class)` | `DB.pojo.selectWrapper(Class)` | 加 Wrapper 后缀 |
| `DB.pojo.update(Class)` | `DB.pojo.updateWrapper(Class)` | 加 Wrapper 后缀 |
| `DB.pojo.delete(Class)` | `DB.pojo.deleteWrapper(Class)` | 加 Wrapper 后缀 |
| `DB.pojo.insert(T)` | `DB.pojo.insert(T)` | 不变（快捷操作） |
| `DB.pojo.save(T)` | `DB.pojo.upsert(T)` | save→upsert，语义更明确 |
| `DB.table.save(table, map)` | `DB.table.upsert(table, map)` | save→upsert |
| `DB.table.insert(table)` | `DB.table.insertWrapper(table)` | 加 Wrapper 后缀 |
| (新增) | `DB.table.insert(table, map)` | Table 补 insert 快捷操作，与 Pojo 对齐 |
| `DB.jdbc.query(sql, args)` | `DB.jdbc.selectWrapper(sql, args)` | query→selectWrapper |
| `DB.jdbc.execute(sql, args)` | `DB.jdbc.execute(sql, args)` | 不变（快捷操作直接执行） |
| `DB.sql.query(sql)` | `DB.sql.selectWrapper(sqlKey)` | query→selectWrapper |
| `DB.sql.execute(sql)` | `DB.sql.execute(sqlKey, params)` | 改为快捷操作，参数通过 Map 传 |
| (新增) | `DB.jdbc.one/list/count/page(...)` | Jdbc 补快捷查询操作 |
| (新增) | `DB.sql.one/list/count/page(...)` | Sql 补快捷查询操作 |
| `wrapper.queryOne()` | `wrapper.one()` | 简化 |
| `wrapper.queryList()` | `wrapper.list()` | 简化 |
| `wrapper.queryBean/List()` | `wrapper.one()/list()` | PojoQuery 泛型已绑定 |
| `.likeLeft(col, val)` | `.endsWith(col, val)` | 重命名 |
| `.likeRight(col, val)` | `.startsWith(col, val)` | 重命名 |
| `.ands(lambda)` | `.and(lambda)` | 单数 |
| `service.getBeanList(wrapper, Class)` | `wrapper.list(Class)` | 废弃服务层路径 |

**迁移策略**：8.0 不在同一门面原地伪兼容 7.x；通过独立兼容模块或迁移工具处理。8.x 只增不改不删，9.0 另行评审破坏性变更。

---

## 九、设计决策记录（ADR）

### ADR-001：统一 `Wrapper` 后缀作为"构造器"标记

**决策**：所有返回 wrapper 的方法带 `Wrapper` 后缀；不带 = 直接执行
**理由**：一条规则零例外，从方法名即可判断行为，无需记忆门面差异
**代价**：方法名略长（+7 字符）

### ADR-002：选用 `select` 而非 `query` 作为查询构造器词根

**决策**：`selectWrapper`（CRUD 对称），不用 `queryWrapper`
**理由**：`select` 是 SQL 标准关键字，CRUD 对称（insert/select/update/delete）；`selectWrapper` 与 `selectById` 靠 Wrapper 后缀区分，无歧义
**代价**：select 词根复用，但 Wrapper 后缀已消除歧义

### ADR-003：Pojo/Table 各 4 个 wrapper，Jdbc/Sql 各 2 个

**决策**：Pojo/Table 有 selectWrapper/insertWrapper/updateWrapper/deleteWrapper；Jdbc/Sql 有 selectWrapper/executeWrapper
**理由**：Pojo/Table 面向对象/表，CRUD 分明；Jdbc/Sql 面向原生 SQL，只有查/写两类。极度对称，规则一句话说完
**代价**：Jdbc 写操作从 `execute(sql)` 变为 `executeWrapper(sql).execute()`，多一步

### ADR-004：Pojo 补全 `insertWrapper(Class)`

**决策**：Pojo 系提供 `insertWrapper(Class<T>)`，与 Table 系对称
**理由**：对称完整；支持链式插入（批量、返回 id、指定字段）。`insert(T)` 快捷操作覆盖简单场景，`insertWrapper` 覆盖复杂场景
**代价**：与 `insert(T)` 功能重叠，但层级不同（快捷 vs 构造器）

### ADR-005：Jdbc/Sql 既有 wrapper 又有快捷操作

**决策**：Jdbc/Sql 提供 `selectWrapper`/`executeWrapper`（wrapper）+ `execute`/`one`/`list`/`count`/`page`（快捷操作）
**理由**：Jdbc/Sql 本就是为快捷而设计，参数齐全时直接执行更方便。wrapper 用于需要链式配置（排序、动态参数）的场景，快捷操作用于参数齐全的高频场景。`execute(sql, args)` 不带 Wrapper = 直接执行（快捷操作），`executeWrapper(sql, args)` 带 Wrapper = 返回构造器，规则依然零例外
**代价**：Jdbc/Sql 方法数增加，但覆盖了"快捷"和"灵活"两端

### ADR-006：废弃 `ICommService` 服务层执行路径

**决策**：移除 `service.getBean(wrapper, Class)`，统一用 wrapper 终结方法
**理由**：双执行路径导致同一查询两种写法
**代价**：存量代码迁移

### ADR-007：`likeLeft/likeRight` → `endsWith/startsWith`

**决策**：重命名，消除反直觉
**理由**：原名方向反直觉，易误用导致性能事故。新名与 Java String 方法对齐
**代价**：迁移成本

### ADR-008：主门面 `DB` 冻结

**决策**：8 个子门面字段永不增改删
**理由**：保证 5-10 年二进制兼容
**代价**：新能力走二级路径

### ADR-009：`save` 改名为 `insertOrUpdateById`

**决策**：`save(T)` / `save(table, map)` 改为 `insertOrUpdateById(T)` / `insertOrUpdateById(table, idColumn, map)`。
**理由**：该操作按主键是否为空选择 insert/update，是非原子流程；名称必须准确表达语义，不能使用数据库原子 UPSERT 术语。真正 UPSERT 通过 `onConflict(...)` 提供。
**代价**：迁移成本；动态 Table 调用增加显式 `idColumn`。

### ADR-010：Table 补 `insert(table, map)` 快捷操作，与 Pojo 完全对齐

**决策**：Table 系补充 `insert(table, JSONMap)` 和 `insert(table, JSONMap, DbOption...)` 快捷操作
**理由**：Pojo 有 `insert(T)` 快捷操作，Table 原本没有，导致两系快捷操作不对齐。补全后 Pojo/Table 的快捷操作完全对称（insert/upsert/selectById/updateById/deleteById/existsById/count），仅参数类型不同
**代价**：Table `insert(table, map)` 返回 int（影响行数），Pojo `insert(T)` 返回 T（id 回填），返回值不一致。但差异合理：实体可回填 id，map 不便回填；需 id 时走 `insertWrapper(table).executeAndReturnId()`

### ADR-011：去掉 `extend/ext` 通用门面注册，改用 Option 体系扩展

**决策**：移除 `DB.config.extend(name, facade)` / `ext(name)`，所有快捷操作改为支持 `DbOption...` 参数
**理由**：通用门面注册实际无人使用——开发者不需要注册一个全新的门面，真正的扩展需求是"给现有操作加行为选项"。Option 体系（InsertOption/UpdateOption/DeleteOption/SelectOption + 插件自定义 Option）更贴合实际：插件全局注册自动介入，Option 单次传入控制行为，两者组合覆盖所有扩展场景
**代价**：丧失"注册自定义门面"的通用能力。但该能力是伪需求，Option 体系已覆盖真实扩展场景

### ADR-012：所有快捷操作支持 `DbOption...`，统一接口非各操作专属类型

**决策**：快捷操作统一使用 `DbOption...`，但必须在运行时校验适用操作、重复、冲突和 `null`；错误输入抛参数异常。
**理由**：保留插件 Option 的扩展能力，同时禁止静默忽略错误配置。

---

## 十、完整 API 速查表

### 10.1 Pojo 系

```java
// Wrapper（4 个，带 Wrapper 后缀）
PojoQuery<T>  q = DB.pojo.selectWrapper(User.class);
PojoInsert<T> i = DB.pojo.insertWrapper(User.class);
PojoUpdate<T> u = DB.pojo.updateWrapper(User.class);
PojoDelete<T> d = DB.pojo.deleteWrapper(User.class);

// 查询
T       u  = DB.pojo.selectWrapper(User.class).eq(User::getId, 1L).one();
List<T> us = DB.pojo.selectWrapper(User.class).gt(User::getAge, 18).list();
Page<T>  p  = DB.pojo.selectWrapper(User.class).eq(User::getStatus, 1).page(PageRequest.of(1, 20));
long    c  = DB.pojo.selectWrapper(User.class).count();
boolean ok = DB.pojo.selectWrapper(User.class).eq(User::getId, 1L).exists();

// 插入（wrapper）
DB.pojo.insertWrapper(User.class).entity(user).execute();
DB.pojo.insertWrapper(User.class).entities(list).size(500).execute();
Long id = DB.pojo.insertWrapper(User.class).entity(user).executeAndReturnId();

// 更新
int n = DB.pojo.updateWrapper(User.class).set(User::getName, "x").eq(User::getId, 1L).execute();

// 删除
int n = DB.pojo.deleteWrapper(User.class).lt(User::getCreateTime, oldDate).execute();

// 快捷操作（不带 Wrapper）
T       e  = DB.pojo.insert(entity);
T       e  = DB.pojo.insertOrUpdateById(entity);
T       u  = DB.pojo.selectById(User.class, 1L);
int     n  = DB.pojo.updateById(entity);
int     n  = DB.pojo.deleteById(User.class, 1L);
boolean ok = DB.pojo.existsById(User.class, 1L);
long    c  = DB.pojo.count(User.class);
```

### 10.2 Table 系

```java
// Wrapper（4 个）
TableQuery  q = DB.table.selectWrapper("user");
TableInsert i = DB.table.insertWrapper("user");
TableUpdate u = DB.table.updateWrapper("user");
TableDelete d = DB.table.deleteWrapper("user");

// 查询
List<ResultMap> rs = DB.table.selectWrapper("user").eq("status", 1).list();
// 插入
int  n  = DB.table.insertWrapper("user").value("name","tom").execute();
Long id = DB.table.insertWrapper("user").value("name","tom").executeAndReturnId();
// 更新
int  n  = DB.table.updateWrapper("user").set("name","x").eq("id",1L).execute();
// 删除
int  n  = DB.table.deleteWrapper("user").eq("id",1L).execute();

// 快捷（与 Pojo 对齐）
int        n  = DB.table.insertWrapper("user", JSONMap.of("name","tom","age",20));
int        n  = DB.table.upsert("user", JSONMap.of("id",1,"name","x"));
ResultMap  r  = DB.table.selectById("user", 1L);
int        n  = DB.table.updateById("user", JSONMap.of("id",1,"name","x"));
int        n  = DB.table.deleteById("user", 1L);
```

### 10.3 JDBC 系（2 wrapper + 快捷操作）

```java
// 快捷操作（高频首选，直接执行）
User       u  = DB.jdbc.one("SELECT * FROM user WHERE id=?", User.class, 1L);
List<User> us = DB.jdbc.list("SELECT * FROM user WHERE status=?", User.class, 1);
long       c  = DB.jdbc.count("SELECT count(*) FROM user WHERE status=?", 1);
int        n  = DB.jdbc.execute("UPDATE user SET status=? WHERE id=?", 0, 1L);
Long       id = DB.jdbc.executeAndReturnId("INSERT INTO user(name) VALUES(?)", "tom");
Page<User> p  = DB.jdbc.page("SELECT * FROM user WHERE status=?", 1, 20, User.class, 1);

// Wrapper（需要链式配置时用）
Page<User> p = DB.jdbc.selectWrapper("SELECT * FROM user WHERE status=?", 1)
    .orderBy("create_time desc")
    .page(PageRequest.of(1, 20), User.class);
```

### 10.4 SQL 系（2 wrapper + 快捷操作）

```java
// 快捷操作（参数通过 Map 传入，直接执行）
User       u  = DB.sql.one("user.findById", JSONMap.of("id", 1L), User.class);
List<User> us = DB.sql.list("user.findByStatus", JSONMap.of("status", 1), User.class);
long       c  = DB.sql.count("user.countByStatus", JSONMap.of("status", 1));
int        n  = DB.sql.execute("user.deactivate", JSONMap.of("id", 1L));
Page<User> p  = DB.sql.page("user.query", JSONMap.of("status", 1), PageRequest.of(1, 20), User.class);

// Wrapper（需要链式 .set() 逐步设参时用）
Page<User> p = DB.sql.selectWrapper("user.query")
    .set("status", 1)
    .set("name", keyword)
    .page(PageRequest.of(1, 20), User.class);
```

### 10.5 批量 / 事务 / 数据源

```java
// Pojo 批量（直接执行，返回 BatchResult）
DB.batch.insert(userList);              // 默认 batchSize=1000
DB.batch.insert(userList, 500);         // 指定 batchSize
DB.batch.update(userList, 200);
// Table 批量（与 Pojo 对齐）
DB.batch.insert("user", mapList, 500);
DB.batch.update("user", mapList, 200);
// 原生 SQL 批量
DB.batch.execute("INSERT INTO log(msg) VALUES(?)", paramList, 100);

// 事务 / 数据源
DB.tx.run(() -> { DB.pojo.insert(a); DB.pojo.updateById(b); });
DB.ds.use("slave", () -> DB.pojo.selectById(User.class, 1L));
```

---

## 十一、Option 体系（标准扩展机制）

**Option 是 V2 的标准扩展机制**，取代通用门面注册。所有增删改查快捷操作都支持 `DbOption...` 可变参数，控制单次操作行为。

### 11.1 顶层接口

```java
/** 所有 Option 的标记接口 */
public interface DbOption {}
```

### 11.2 框架内置四类 Option

按操作分类，各自有专属选项（均实现 `DbOption`）：

```java
// ========== InsertOption ==========
public final class InsertOption implements DbOption {
    public static final InsertOption IGNORE_NULL;           // 忽略 null 字段
    public static final InsertOption INCLUDE_NULL;          // 显式插入 NULL
    public static InsertOption INCLUDE_FIELDS(DlzFn... fs); // 只插指定字段
    public static InsertOption IGNORE_FIELDS(DlzFn... fs);  // 排除指定字段
    public static final InsertOption NO_AUTO_KEY;           // 不回填自增 id
    public static final InsertOption IGNORE_ON_DUPLICATE;   // INSERT IGNORE
    public static InsertOption UPDATE_ON_DUPLICATE(DlzFn... updateFields); // ON DUPLICATE KEY UPDATE
}

// ========== UpdateOption ==========
public final class UpdateOption implements DbOption {
    public static final UpdateOption IGNORE_NULL;           // 忽略 null 字段
    public static UpdateOption INCLUDE_FIELDS(DlzFn... fs); // 只更指定字段
    public static final UpdateOption OPTIMISTIC_LOCK;       // 乐观锁检查
}

// ========== DeleteOption ==========
public final class DeleteOption implements DbOption {
    public static final DeleteOption LOGIC;                 // 逻辑删除（默认行为）
    public static final DeleteOption PHYSICAL;              // 强制物理删除
}

// ========== SelectOption ==========
public final class SelectOption implements DbOption {
    public static final SelectOption FOR_UPDATE;            // SELECT ... FOR UPDATE
    public static final SelectOption INCLUDE_DELETED;       // 包含已逻辑删除记录
}
```

### 11.3 使用方式

```java
// 插入选项
DB.pojo.insert(user, InsertOption.IGNORE_NULL);
DB.pojo.insert(user, InsertOption.IGNORE_FIELDS(User::getCreateTime));
DB.pojo.insert(user, InsertOption.UPDATE_ON_DUPLICATE(User::getName, User::getAge));

// 更新选项
DB.pojo.updateById(user, UpdateOption.IGNORE_NULL, UpdateOption.OPTIMISTIC_LOCK);

// 删除选项
DB.pojo.deleteById(User.class, 1L, DeleteOption.PHYSICAL);     // 强制物理删除
DB.pojo.deleteById(User.class, 1L, DeleteOption.LOGIC);        // 显式逻辑删除

// 查询选项
DB.pojo.selectById(User.class, 1L, SelectOption.FOR_UPDATE);   // 加锁查询
DB.pojo.selectById(User.class, 1L, SelectOption.INCLUDE_DELETED); // 查已删除记录

// 可组合（不同类型 Option 混用，运行时各取所需）
DB.pojo.insert(user, InsertOption.IGNORE_NULL, AuditOption.SKIP);  // 框架 Option + 插件 Option
```

### 11.4 插件提供自定义 Option

插件可定义自己的 Option 类（实现 `DbOption`），用户传入后插件识别：

```java
// 审计插件自定义 Option
public final class AuditOption implements DbOption {
    public static final AuditOption SKIP = new AuditOption();        // 跳过审计
    public static final AuditOption FILL_CREATE = new AuditOption(); // 强制填充创建字段
}

// 注册插件
DB.config.plugin(new AuditInterceptor());

// 使用（任何操作都可传 AuditOption，插件在钩子里识别）
DB.pojo.insert(user, InsertOption.IGNORE_NULL, AuditOption.SKIP);
```

插件钩子签名扩展，接收 Option 列表：

```java
public interface SqlBuildInterceptor {
    // 钩子增加 DbOption[] 参数，插件从中识别自己关心的 Option
    void onBuildInsert(String table, Map<String,Object> values, DbOption[] opts);
    void onBuildWhere(String table, Condition where, DbOption[] opts);
    // ...
}
```

### 11.5 类型安全说明

方法签名统一用 `DbOption...`（非各操作专属类型），兼顾插件扩展能力与运行时校验：
- 传入不适用、重复、冲突或 `null` 的 Option **必须抛出参数异常**，不得静默忽略
- 理由：插件 Option 可跨操作通用（如 `AuditOption.SKIP` 对 insert/update 都有意义），但每个 Option 必须声明适用操作并在调用入口校验
- 建议提供 `DbOptionValidator` 或统一校验器，保证框架内置 Option 与插件 Option 的错误行为一致

---

## 十二、稳定性承诺

### 12.1 V2 LTS 承诺

- `DB` 主门面 8 字段**永不增改删**
- 各子门面 public 方法签名**只增不改不删**
- 终结方法 `one/list/page/count/exists/execute` **永不改名**
- `Wrapper` 后缀规则**永不破坏**
- 条件构造方法 `eq/ne/gt/...` **永不改名**
- `DbOption` 接口**永不改名**，插件可放心实现自定义 Option
- 新增 Option 只加不改不删（枚举值只增不移除）

### 12.2 版本路线

| 版本 | 时间 | 内容 |
|------|------|------|
| 8.0.0 | T+0 | 新 API 上线；7.x 通过独立兼容模块或迁移工具处理，不在同一门面原地伪兼容 |
| 8.x | T+1~2y | 增强二级门面和能力实现；主门面签名只增不改不删 |
| 9.0 | 另行评审 | 仅在新一轮大版本契约评审完成后处理移除或破坏性变更 |

---

## 附录 A：需修改的文件清单

| 文件 | 修改内容 |
|------|----------|
| `DbPojo.java` | `select`→`selectWrapper`；新增 `insertWrapper/updateWrapper/deleteWrapper`；快捷操作加 `DbOption...` |
| `DbTable.java` | 4 个 wrapper 统一命名；快捷操作加 `DbOption...`；补 `insert(table,map)` |
| `DbJdbc.java` | `query`→`selectWrapper`；新增 `executeWrapper`；补快捷操作 `execute/one/list/count/page` |
| `DbSql.java` | `query`→`selectWrapper`；新增 `executeWrapper`；补快捷操作 `execute/one/list/count/page`（参数走 Map） |
| `DbConfig.java` | 去掉 `extend/ext`；补全启动项/参数配置 |
| `DbBatch.java` | 补全 Table 批量（insert/update with table+mapList） |
| `PojoInsert.java` | 新增 `.entity(T)/.entities(List)/.executeAndReturnId()`；修复 `\|\|` bug |
| 各 Query wrapper | 终结方法统一 `one/list/page/count/exists` |
| `ICondAddBy*` | `likeLeft`→`endsWith`；`likeRight`→`startsWith`；`ands`→`and`；`ors`→`or` |
| `ICommService` | `@Deprecated`，内部委托 wrapper 终结方法 |
| `DbOption.java` | **新增**：Option 顶层标记接口 |
| `InsertOption/UpdateOption/DeleteOption/SelectOption.java` | **新增**：四类内置 Option |
| `SqlBuildInterceptor.java` | 钩子签名扩展，增加 `DbOption[]` 参数 |
| `DB.java` | 冻结，添加 LTS 注释 |

---

*本设计文档为 dlz-db V2 API 的权威规范。任何对主门面的修改必须先修订本文档。*
