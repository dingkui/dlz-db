# DLZ-DB · AI 速读

> Java 持久层框架。静态入口 `DB.`，链式 API，无 Mapper / XML 样板。
> 本文档为 AI 生成代码的唯一规范来源。

## 心智模型

```
DB.Pojo     —— 有 Bean 的 CRUD（首选）
DB.Table    —— 动态表名（无 Bean 或分表）
DB.Jdbc     —— 一行原生 SQL，占位符 ?
DB.Sql      —— 预设/复杂 SQL，占位符 #{}
DB.Batch    —— 批量写入
DB.Dynamic  —— 数据源切换作用域
```

## 入口职责

| 入口 | 场景 | 占位符 |
|---|---|---|
| `DB.Pojo.select(T.class)` | 简单 CRUD | 条件构造器 |
| `DB.Table.select("t_202401")` | 运行时表名查询 | 条件构造器 |
| `DB.Jdbc.select("SELECT ... WHERE x=?", v)` | 一次性 SQL | `?` |
| `DB.Sql.select("key.xxx")` | 预设 SQL / 复杂 SQL | `#{key}` |

## 条件方法（统一三参 `(condition, field, value)`）

| 方法 | SQL |
|---|---|
| `eq / ne / gt / ge / lt / le` | `= / <> / > / >= / < / <=` |
| `isn / isnn` | `IS NULL / IS NOT NULL` |
| `in / ni` | `IN / NOT IN`（值可为 `List` / `"1,2,3"` / `"sql:SELECT..."`） |
| `bt / nb` | `BETWEEN / NOT BETWEEN` |
| `lk / ll / lr / nl` | `LIKE '%v%' / 'v%' / '%v' / NOT LIKE` |
| `or(o -> ...)` / `and(a -> ...)` | 嵌套括号 |
| `sql("SQL 片段 #{k}", JSONMap)` | 自定义 SQL 片段（首选） |

**field 形式**：`User::getId`（Lambda，推荐）或 `"id"`（字符串）。
**省略 condition** 即等价于 `true`。

## 返回值规则

| 方法 | 返回 |
|---|---|
| `queryBean()` | `T` 或 `null` |
| `queryBeanList()` | `List<T>` |
| `queryBeanPage()` | `Page<T>` |
| `queryOne()` | `ResultMap` 或 `null`（**非 Bean**） |
| `queryList()` | `List<ResultMap>`（**非 List\<T\>**） |
| `queryPage()` | `Page<ResultMap>` |
| `queryOne(C)` / `queryList(C)` / `queryPage(C)` | 指定类型 |
| `count()` | `long` |

**规则**：带 `Bean` → Bean；不带 → `ResultMap`；带 `(Class)` → 指定类型。

## 硬约束（AI 必须遵守）

1. **无 Mapper / DAO**：不写 `@Mapper`、不 `@Autowired UserDao` / `UserMapper`。CRUD 直接 `DB.Pojo.*`。
2. **无 Wrapper 类**：不写 `new LambdaQueryWrapper<>()` / `new QueryWrapper<>()`。用链式 `DB.Pojo.select(...)`。
3. **无 `selectById` / `selectList` 等 MP 方法**：它们不存在。
4. **占位符不可混用**：`DB.Jdbc` 用 `?`；`DB.Sql` / `sql(...)` 用 `#{key}`；旧 `apply(...)` 用 `{0}`（新代码请用 `sql`）。
5. **`queryList()` 返回 `List<ResultMap>`，不是 `List<Bean>`**。要 Bean 用 `queryBeanList()` 或 `queryList(Class)`。
6. **写操作必须 `.execute()` 结尾**。缺失则不执行。
7. **预设 SQL 调用 key 必须以 `"key."` 开头**，即使 sqlId 本身不含。
8. **`in(field, value)` 的 value** 必须是 `List` / CSV 字符串 `"1,2,3"` / 子查询 `"sql:SELECT ..."`，**不可传单值**。

## 预设 SQL 规则

- XML 配置：`sqlId` 不加 `"key."` 前缀
- Java 调用：必须加 `"key."` 前缀
- 空值忽略：`[AND field = #{key}]` 方括号内参数为 null 时整段删除

## Entity 约定

- 表名：默认类名驼峰转下划线（`User` → `user`）。自定义：`@TableName("t_user")`
- 字段名：默认驼峰转下划线（`createTime` → `create_time`）。自定义：`@TableField("ct")`
- 逻辑删除：Bean 含 `isDeleted` 字段时自动注入 `IS_DELETED=0`

---

**版本适配**：DLZ-DB 6.x+ / Spring Boot 2.x+ / JDK 8+。
**依赖**：`top.dlzio:dlz.db`。
**详细文档**：超出本规范部分查 `docs/开发者指南.md`。
