# dlz-db 门面 API 设计方案（最终冻结版）

> 目标：5 年不修改公开 API，偶尔新增靠重载/插件，零歧义。

---

## 一、设计原则

```
原则一：二级门面      → DB 只挂 8 个 final 字段，不挂任何业务方法
原则二：动词冻结      → select/update/delete/insert/save + byId 系列，5 年不加新动词
原则三：终结方法统一  → 不管哪个门面，查询收口 one/list/page/count/exists/value，变更收口 execute
原则四：正交不重叠    → 每个门面单一职责，无"用哪个都行"的灰色地带
原则五：快捷有门槛    → 快捷方法须满足"高频+通用+条件固定+语义唯一"四关
原则六：泛型入口绑定  → DB.pojo.select(User.class) 即绑死 T=User，后续不再传 Class
```

---

## 二、8 个门面总览

```
┌─────────────────────────────────────────────────────────────┐
│                          DB（唯一入口）                      │
├──────────────┬──────────────────────┬────────────────────────┤
│   数据操作    │     基础设施          │     辅助工具            │
├──────────────┼──────────────────────┼────────────────────────┤
│  DB.pojo     │  DB.tx               │  DB.config             │
│  DB.table    │  DB.ds               │                        │
│  DB.jdbc     │  DB.batch            │                        │
│  DB.sql      │                      │                        │
└──────────────┴──────────────────────┴────────────────────────┘
```

| 门面 | 实现类 | 定位 | 何时用 |
|---|---|---|---|
| `DB.pojo` | `DbPojo` | Lambda + 类型安全 | 有实体类，要 IDE 补全和重构安全 |
| `DB.table` | `DbTable` | 表名驱动，无需实体 | 动态表名、无实体、报表、低代码 |
| `DB.jdbc` | `DbJdbc` | 原生 SQL + `?` 占位 | 一句 SQL 跑完，简单场景 |
| `DB.sql` | `DbSql` | 预设 SQL + `#{}` 命名参数 | 复杂/可复用/需动态管理的 SQL |
| `DB.batch` | `DbBatch` | 批量操作 | 大量数据写入 |
| `DB.tx` | `DbTx` | 事务 | 需要原子性 |
| `DB.ds` | `DbDs` | 数据源管理 | 多租户、动态数据源、灰度 |
| `DB.config` | `DbConfig` | 配置与插件 | 注册插件/方言 |

**关键约束：**
1. `DB` 类只有 8 个 `final static` 字段，**不挂任何业务方法**——这是 5 年不变的核心。
2. 字段全小写：`DB.pojo` 而非 `DB.Pojo`（静态实例字段规范）。
3. 实现类统一 `Db` 前缀（`DbPojo` 而非 `DBPojo`，缩写不全大写）。

---

## 三、命名一致性规范

### 实现类命名：全部 `Db` 前缀

```java
DbPojo  DbTable  DbJdbc  DbSql  DbBatch  DbTx  DbDs  DbConfig
```

### 终结方法命名（全框架统一）

```
查询终结：one()  oneOrThrow()  list()  page()  count()  exists()  value()  value(Class)
变更终结：execute()
```

### 动词表（冻结，5 年不加新动词）

| 动词 | 形态 | 覆盖场景 |
|---|---|---|
| `select` | 链式 | 条件多变查询 |
| `update` | 链式 | 条件多变更新 |
| `delete` | 链式 | 条件多变删除 |
| `insert` | 快捷 + 选项参数 | 强制插入（含手动主键） |
| `save` | 快捷 | insertOrUpdate（按主键判断） |
| `*ById` | 快捷 | 条件固定为主键 |

**划分标准一句话：需要"条件"的走链式，不需要"条件"的走快捷。**

---

## 四、快捷方法准入准则

```
1. 高频吗？        否 → 不加
2. 通用吗（跨业务）？  否 → 不加
3. 条件固定吗？      否 → 不加
4. 语义唯一吗？      否 → 不加
全过 → 加
```

冻结的快捷方法清单（仅这些，5 年不加新名）：

```
insert(entity[, InsertOption...])        强制插入
save(entity)                              insertOrUpdate
selectById(Class, id)                     单条
selectByIds(Class, Object...|Collection)  多条
updateById(entity)                        按主键改
deleteById(Class, id)                     按主键删
deleteByIds(Class, Object...|Collection)  按主键删多条
```

---

## 五、各门面详细 API

### 1. `DB.pojo` — 类型安全主力

```java
// ═══ 链式构建（需终结方法收口）═══
DB.pojo.select(User.class)
    .eq(User::getStatus, 1)
    .like(User::getName, "张")
    .orderByDesc(User::getCreateTime)
    .list();

DB.pojo.select(User.class, User::getId, User::getName)   // 指定字段
    .eq(User::getStatus, 1)
    .list();

DB.pojo.update(User.class)
    .set(User::getName, "新名字")
    .eq(User::getId, 1)
    .execute();

DB.pojo.delete(User.class)
    .eq(User::getId, 1)
    .execute();

// ═══ 快捷操作（直接执行）═══
User u1   = DB.pojo.insert(user);                         // 强制插入，回填 id
User u2   = DB.pojo.insert(user, InsertOption.IGNORE_NULL);
User u3   = DB.pojo.save(user);                           // insertOrUpdate
User u4   = DB.pojo.selectById(User.class, 1);            // 无结果返回 null
List<User> us = DB.pojo.selectByIds(User.class, 1, 2, 3); // 可变参数
List<User> us2 = DB.pojo.selectByIds(User.class, idList); // Collection
int n1    = DB.pojo.updateById(user);                     // 返回影响行数
int n2    = DB.pojo.deleteById(User.class, 1);
int n3    = DB.pojo.deleteByIds(User.class, 1, 2, 3);
```

**insert vs save：**
- `insert(entity)` — 强制插入，不管 id 有没有值（覆盖手动主键场景）
- `save(entity)` — 按 id 判断：id 空→插，id 有→改
- 选项（ignoreNull/主键冲突策略）用 `InsertOption` 可变参数，不走链式

### 2. `DB.table` — 表名驱动，无实体

```java
// ═══ 链式构建 ═══
DB.table.select("user").eq("status", 1).list();
DB.table.select("user").eq("status", 1).list(User.class);  // 映射到 Bean
DB.table.update("user").set("name", "x").eq("id", 1).execute();
DB.table.delete("user").eq("id", 1).execute();
DB.table.insert("user").set("name", "张三").set("age", 25).execute();

// ═══ 快捷操作 ═══
ResultMap r1  = DB.table.selectById("user", 1);
List<ResultMap> rs = DB.table.selectByIds("user", 1, 2, 3);
int t1 = DB.table.save("user", map);                       // insertOrUpdate
int t2 = DB.table.updateById("user", map);
int t3 = DB.table.deleteById("user", 1);
int t4 = DB.table.deleteByIds("user", 1, 2, 3);
```

**pojo vs table 边界：**
```
有实体类 → DB.pojo（类型安全、Lambda、重构安全）
没实体类 → DB.table（动态表名、报表、低代码、运维）
绝不重叠：DB.pojo 不接受 String 表名，DB.table 不接受 Lambda
```

### 3. `DB.jdbc` — 原生 SQL，`?` 占位

```java
// query 返回 Builder，可 .one()/.list()/.count()/.value()
List<ResultMap> list = DB.jdbc.query("SELECT * FROM user WHERE status = ?", 1).list();
List<User> users = DB.jdbc.query("SELECT * FROM user WHERE status = ?", 1).list(User.class);
User user = DB.jdbc.query("SELECT * FROM user WHERE id = ?", 1).one(User.class);
Long cnt = DB.jdbc.query("SELECT count(*) FROM user").value(Long.class);

// execute 直接返回 int（增删改/DDL）
int rows = DB.jdbc.execute("UPDATE user SET status = ? WHERE id = ?", 1, 100);
DB.jdbc.execute("CREATE TABLE temp_log (id INT, msg TEXT)");
```

**设计要点：**
- `query()` 用于 SELECT（返回 Builder）
- `execute()` 用于 INSERT/UPDATE/DELETE/DDL（直接返回 int）
- 不做条件构造——需要条件用 `DB.pojo` 或 `DB.table`

### 4. `DB.sql` — 预设 SQL + `#{}` 命名参数

```java
// 从 SQL 文件/DB 加载（key）
List<User> users = DB.sql.query("user.findActive")
    .set("status", 1).set("minAge", 18)
    .list(User.class);

// 内联命名参数 SQL
List<User> us = DB.sql.query("SELECT * FROM user WHERE name = #{name} AND age > #{age}")
    .set("name", "张三").set("age", 18)
    .list(User.class);

// 变更类
int rows = DB.sql.execute("user.deactivate")
    .set("userId", 100)
    .execute();
```

**jdbc vs sql 边界：**
```
DB.jdbc → "写一句跑完"  → ? 占位  → 简单场景
DB.sql  → "复用/管理/动态配置" → #{} 命名参数 → 复杂场景
绝不重叠：jdbc 不支持命名参数，sql 不支持 ? 占位
```

### 5. `DB.batch` — 批量操作

```java
// 批量插入（默认批次大小）
DB.batch.insert(userList).execute();
// 自定义批次
DB.batch.insert(userList).size(500).execute();
// 批量更新
DB.batch.update(userList).size(200).execute();
// 批量执行原生 SQL
DB.batch.execute("INSERT INTO log(msg) VALUES(?)", paramList).size(100).execute();
```

### 6. `DB.tx` — 事务

```java
// 默认数据源
DB.tx.run(() -> {
    DB.pojo.insert(user);
    DB.pojo.update(order);
});

// 带返回值
User u = DB.tx.call(() -> {
    DB.pojo.insert(user);
    return DB.pojo.selectById(User.class, 1);
});

// 指定数据源
DB.tx.run("report_db", () -> DB.pojo.insert(report));
User u2 = DB.tx.call("report_db", () -> DB.pojo.selectById(User.class, 1));

// 新事务（REQUIRES_NEW）
DB.tx.runNew(() -> { /* 独立事务 */ });
```

**方法：**
- `run(Runnable)` / `call(Supplier<T>)` — 默认数据源
- `run(dsName, Runnable)` / `call(dsName, Supplier<T>)` — 指定数据源（重载，避免链式线程安全问题）
- `runNew(Runnable)` / `callNew(Supplier<T>)` — 新事务（依赖底层支持）
- 底层桥接 Spring `PlatformTransactionManager` / Solon `TranExecutor`

### 7. `DB.ds` — 数据源管理

```java
DB.ds.add("tenant_001", props);                          // 注册
DB.ds.use("tenant_001", () -> {                          // 切换执行
    DB.pojo.select(User.class).list();
});
List<User> us = DB.ds.call("tenant_001", () -> ...);     // 切换执行（带返回值）
boolean ok = DB.ds.test(props);                          // 测试连接
boolean has = DB.ds.has("tenant_001");                   // 是否存在
Set<String> names = DB.ds.names();                       // 所有数据源名
DB.ds.remove("tenant_001");                              // 移除
```

### 8. `DB.config` — 配置与扩展注册

```java
// 注册插件/拦截器
DB.config.plugin(new LogicDeletePlugin("deleted"));
DB.config.plugin(new SlowSqlInterceptor(3000));

// 注册方言（像插件一样外部加，支持新数据库）
DB.config.dialect(new KingBaseDialect());

// 查询已注册
List<DbPlugin> plugins = DB.config.plugins();
List<SqlHelper> dialects = DB.config.dialects();
```

**config 只管"全局扩展点注册"，不管：**
- 初始化（init）→ 内部 `DBHolder`，由 starter 自动调
- 静态开关（showSql 等）→ `DlzDbProperties` + yml
- 数据源管理 → `DB.ds`
- 本次调用临时覆盖 → wrapper 链式 `.convert()`

**方言机制：** 方言自带 `supports(url)` 识别能力，`DataSourceConfig.getSqlHelper()` 遍历已注册方言匹配，找不到回退内置。外部加新数据库零框架改动。

---

## 六、终结方法统一规范

所有查询 Builder（pojo/table/jdbc/sql）共享同一套终结方法：

```java
.one()              → T / Record          // 单条，无结果返回 null
.oneOrThrow()       → T / Record          // 单条，无结果抛异常
.list()             → List<T>             // 列表（永不 null，空集合）
.list(Class<T>)     → List<T>             // 列表，指定类型
.page(Page)         → Page<T>             // 分页
.page(num, size)    → Page<T>             // 分页（便捷重载）
.count()            → long                // 计数
.exists()           → boolean             // 是否存在
.value()            → Object              // 单值（第一行第一列）
.value(Class<T>)    → T                   // 单值，指定类型
```

变更终结：
```java
.execute()          → int                 // 受影响行数
```

---

## 七、条件方法统一规范

不管在 pojo/table/where 里，名字完全一致：

```java
.eq(field, value)           .ne(field, value)           .gt(field, value)
.lt(field, value)           .ge(field, value)           .le(field, value)
.like(field, value)         .likeLeft(field, value)     .likeRight(field, value)
.in(field, values)          .notIn(field, values)       .between(field, v1, v2)
.isNull(field)              .isNotNull(field)
.orderByAsc(field)          .orderByDesc(field)
.groupBy(field)             .having(condition)
.and(c -> c.eq(...).gt(...))
.or(c -> c.eq(...).gt(...))
.eq(condition, field, value)    // 动态条件：第一参为 true 才生效
```

field 类型区别：
- `DB.pojo` → `SFunction<T,?>` Lambda
- `DB.table` → `String` 字段名

**条件复用（不开 DB.where 顶级入口，低频）：**
```java
Condition cond = Condition.of(User.class).eq(User::getStatus, 1).gt(User::getAge, 18);
List<User> list = DB.pojo.select(User.class).where(cond).list();
long count = DB.pojo.select(User.class).where(cond).count();
int deleted = DB.pojo.delete(User.class).where(cond).execute();
```

---

## 八、模块化

```
dlz-db-core                 ← DB 门面 + SqlHelper 抽象 + 默认 MySQL 方言 + ITableColumnMapper
dlz-db-dialect-postgresql   ← PostgreSQL 方言（SPI 自动注册）
dlz-db-dialect-oracle       ← Oracle/DM8 方言
dlz-db-dialect-sqlite       ← SQLite 方言
dlz-db-dialect-xxx          ← 未来新方言独立包，不动 core
dlz-db-spring-boot-starter  ← Spring 集成
dlz-db-solon-plugin         ← Solon 集成
```

**方言按需加载（SPI）：** 方言包放 `META-INF/services/...SqlHelper`，core 启动 `ServiceLoader` 自动发现注册。引了即用，无需手动 `config.dialect()`。core 留 MySQL 默认方言保证开箱即用。

**tableColumnMapper 留 core：** 单一默认 + 用户自定义模式，非多实现按库选，拆包无收益。

---

## 九、配置归属总览

| 配置类 | 归属 | 说明 |
|---|---|---|
| 静态部署配置（showSql/logicDeleteField/blob_charset/sqllist...） | yml / `DlzDbProperties` | 启动读一次，可接配置中心 |
| 数据源配置（url/方言/rowMapper/dbType） | `DB.ds` / `DataSourceConfig` | 数据源级，多数据源各自独立 |
| 本次调用临时覆盖（列名转换/值转换/忽略逻辑删除） | wrapper 链式 `.convert()` | 线程级，生命周期一次调用 |
| 框架内部（executor/service/adapter/序列号） | `DBHolder` | 生命周期管理，用户透明 |
| 全局扩展点（插件/拦截器/方言） | `DB.config` | 注册一次全局生效 |

---

## 十、5 年稳定性规则

1. **DB 下 8 字段冻结**：`pojo/table/jdbc/sql/batch/tx/ds/config`，永不增减。
2. **动词冻结**：不加新动词，新操作走现有动词重载。
3. **终结方法冻结**：`one/list/page/count/exists/value/execute`，不加新终结词。
4. **分页用 Page 对象**：`.page(Page)` 与 `.page(num,size)` 并存，Page 可加字段不破坏签名。
5. **重载而非新名**：变体用重载，不造 `listBean/listMap` 多名字。
6. **避免 boolean 参数**：选项用链式方法或枚举（`InsertOption`），不用 boolean。
7. **门面正交**：pojo 不接 String 表名，table 不接 Lambda。
8. **新增扩展走 config**：新插件/方言实现 `DbPlugin`/`SqlHelper` 接口注册，不改门面。

---

## 附录：完整 API 速查卡

```
DB
├── .pojo                                    实体驱动，类型安全
│   ├── .select(Class)                       → .eq() .like() ... → .one() .list() .page() .count() .exists()
│   ├── .select(Class, fields...)            指定字段
│   ├── .update(Class)                       → .set() .eq() ... → .execute()
│   ├── .delete(Class)                       → .eq() ... → .execute()
│   ├── .insert(entity[, InsertOption...])   → entity（强制插入，回填 id）
│   ├── .save(entity)                        → entity（insertOrUpdate）
│   ├── .selectById(Class, id)               → T / null
│   ├── .selectByIds(Class, Object...|Collection)  → List<T>
│   ├── .updateById(entity)                  → int
│   ├── .deleteById(Class, id)               → int
│   └── .deleteByIds(Class, Object...|Collection)  → int
│
├── .table                                   表名驱动，无需实体
│   ├── .select(tableName)                   → .eq() ... → .one() .list() .page() .count()
│   ├── .update(tableName)                   → .set(k,v) .eq() ... → .execute()
│   ├── .delete(tableName)                   → .eq() ... → .execute()
│   ├── .insert(tableName)                   → .set(k,v) ... → .execute()
│   ├── .save(tableName, map)                → int（insertOrUpdate）
│   ├── .selectById(tableName, id)           → ResultMap
│   ├── .selectByIds(tableName, ...|Collection) → List<ResultMap>
│   ├── .updateById(tableName, map)          → int
│   ├── .deleteById(tableName, id)           → int
│   └── .deleteByIds(tableName, ...|Collection) → int
│
├── .jdbc                                    原生 SQL，? 占位
│   ├── .query(sql, args...)                 → .one() .list() .count() .value()
│   └── .execute(sql, args...)               → int
│
├── .sql                                     预设 SQL，#{} 命名参数
│   ├── .query(keyOrSql)                     → .set(k,v) ... → .one() .list() .value()
│   └── .execute(keyOrSql)                   → .set(k,v) ... → .execute()
│
├── .batch                                   批量
│   ├── .insert(list)                        → .size(n) → .execute()
│   ├── .update(list)                        → .size(n) → .execute()
│   └── .execute(sql, paramList)             → .size(n) → .execute()
│
├── .tx                                      事务
│   ├── .run(Runnable)                       默认数据源
│   ├── .call(Supplier<T>)                   → T
│   ├── .run(dsName, Runnable)               指定数据源
│   ├── .call(dsName, Supplier<T>)           → T
│   ├── .runNew(Runnable)                    新事务
│   └── .callNew(Supplier<T>)                → T
│
├── .ds                                      数据源管理
│   ├── .add(name, props)                    注册
│   ├── .remove(name)                        移除
│   ├── .use(name, Runnable)                 切换执行
│   ├── .call(name, Supplier<T>)             切换执行（带返回值）
│   ├── .test(props)                         → boolean
│   ├── .names()                             → Set<String>
│   └── .has(name)                           → boolean
│
└── .config                                  配置与扩展注册
    ├── .plugin(DbPlugin)                    注册插件/拦截器
    ├── .plugins()                           → List<DbPlugin>
    ├── .dialect(SqlHelper)                  注册方言
    └── .dialects()                          → List<SqlHelper>
```
