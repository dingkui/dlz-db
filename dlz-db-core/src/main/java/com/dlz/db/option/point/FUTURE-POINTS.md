# Option 桩点路线图（未接线设计存档）

> 本目录（`com.dlz.db.option.point`）只保留**框架已真实接线**的 8 个桩点。
> 下述 24 个桩点在 v8 中**已从公共 API 移除**：它们只有定义、没有任何框架调用点，
> 使用者实现后不报错但静默不生效，属于"空头支票 API"。
> 设计本身保留在这里，作为后续版本逐步接线的开发方向。

## 已接线桩点（v8 保留）

| 桩点 | 模式 | 适用操作 | 框架调用点 |
|---|---|---|---|
| `WherePoint` | MERGE | SELECT / UPDATE / DELETE | `WrapperBuildUtil.applyWherePoints` |
| `InsertFieldPoint` | MERGE | INSERT | `WrapperBuildUtil.applyInsertFieldPoints` |
| `InsertNullFieldPoint` | SINGLE | INSERT | `DbTable` / `DbPojo` |
| `UpdateNullFieldPoint` | SINGLE | UPDATE | `DbTable` / `DbPojo` |
| `DeleteModePoint` | SINGLE | DELETE | `IExecutorDelete` / `LogicDeleteOption` |
| `LogicDeleteValuePoint` | SINGLE | DELETE | `IExecutorDelete.rewriteAsLogicDelete` |
| `DeletedDataPoint` | SINGLE | SELECT | `LogicDeleteOption.contributeWhere` |
| `SelectLockPoint` | SINGLE | SELECT | `WrapperBuildUtil.buildSql` |

接线原则：**先接线、后注册、再公开**。新桩点必须同步改造框架调用点并通过
`OptionPointMechanismTest` 式的机制测试，才允许加入 `StandardOptionPoints`。

---

## 未接线设计（按接入难度分组）

### 一、已有硬编码实现，接线即迁移（推荐优先做）

这些功能的逻辑已存在于框架中，只是没走桩点。迁移 = 把硬编码位置改为查桩点、
把默认行为做成内置 Option，行为完全兼容。

#### 1. `GeneratedKeyPoint` — INSERT 主键生成（SINGLE，INSERT）

```java
public interface GeneratedKeyPoint extends OptionPoint {
    Object generateKey(ValueContext context);
}
```

- **设计意图**：主键生成策略（自增回填 / 雪花 / UUID）按操作注入。
- **现有硬编码位置**：
  - `ISqlExecutor.java`（`insertWithAutoKey` 内 `getGeneratedKeys()` 回填）
  - `IExecutorInsert.insertWithAutoKey()`、`IDbExecuteService.insertWithAutoKey()`
  - `DbTable.insertWithAutoKey(...)`
  - `NativeSqlUtil`（getGeneratedKeys 不可用时的兜底）
  - `IdType`（`core.anno`，雪花等注解式 ID 策略——注意与桩点的优先级关系）

#### 2. `PaginationPoint` — 分页 SQL 改写（SINGLE，SELECT）

```java
public interface PaginationPoint extends OptionPoint {
    SqlStatement applyPagination(SqlContext context, Pagination pagination);
}
```

- **现有硬编码位置**：`SqlDialect.pagination(sql, offset, limit)`（默认 `LIMIT offset,limit`，方言各自覆写）。
- **接线方式**：分页改写前查 `single(PaginationPoint.class)`，命中则交给桩点，
  未命中回落 `SqlDialect`。可做加密分页/游标分页插件。

#### 3. `ConditionSafetyPoint` — 空条件安全策略（SINGLE，SELECT/UPDATE/DELETE）

```java
public interface ConditionSafetyPoint extends OptionPoint {
    boolean allowEmptyCondition(CrudContext context);
}
```

- **现有硬编码位置**：`WrapperBuildUtil.buildWhere`（`!allowFullQuery && where为空 → "WHERE false"`）、`AQuery.allowFullQuery` 字段。
- **接线方式**：`"WHERE false"` 分支改为查桩点；现有 `setAllowFullQuery(true)` 语义等价于供给一个放行的 Option。

#### 4. `ColumnNamePoint` — 字段名→列名转换（SINGLE，全操作）

```java
public interface ColumnNamePoint extends OptionPoint {
    String mapColumnName(NameContext context);
}
```

- **现有硬编码位置**：`DbConvertUtil.toDbName/toFieldName`、`DbConfig.columnNameConvertor(...)`、`SqlRunThreadHolder` 上的名称转换器 ThreadLocal。
- **接线方式**：`DbConfig.columnNameConvertor` 注册的全局转换器改为供给此桩点的默认 Option；拦截器按表供给可实现分表不同列名风格。

#### 5. `DataSourceRoutePoint` — 数据源路由（SINGLE，全操作）

```java
public interface DataSourceRoutePoint extends OptionPoint {
    String routeDataSource(CrudContext context);
}
```

- **现有硬编码位置**：`DBDynamic`（动态数据源切换，按 ThreadLocal/dbKey）。
- **接线方式**：`DBHolder.doDb` 取执行器前查桩点，命中的路由标识替代 ThreadLocal 约定。读写分离、按业务路由的插件落点。

#### 6. `InsertValuePoint` / `UpdateValuePoint` / `ConditionValuePoint` — 值转换链（CHAIN）

```java
public interface InsertValuePoint extends OptionPoint {
    Object convertInsertValue(ValueContext context);
}
public interface UpdateValuePoint extends OptionPoint {
    Object convertUpdateValue(ValueContext context);
}
public interface ConditionValuePoint extends OptionPoint {
    Object convertConditionValue(ValueContext context);
}
```

- **现有硬编码位置**：`DbConvertUtil.getVal4Db(tableName, column, value)`（`WrapperBuildUtil` 构建 INSERT/UPDATE/条件时调用）。
- **接线方式**：`getVal4Db` 调用点之后接值转换链。典型插件：字段级加密、脱敏查询、JSON 序列化定制。

#### 7. `ReadValuePoint` — JDBC 读取值转换链（CHAIN，SELECT）

```java
public interface ReadValuePoint extends OptionPoint {
    Object convertReadValue(ValueContext context);
}
```

- **现有硬编码位置**：`dialect.rowMapper` 包的各 RowMapper（`MySqlColumnMapRowMapper` 等）取值后直接进入结果映射。
- **接线方式**：RowMapper 取值后、结果装配前执行转换链。与写值链（6）成对，做加解密对称插件。

#### 8. `RowMapperPoint` — 行映射器选择（SINGLE，SELECT）

```java
public interface RowMapperPoint extends OptionPoint {
    <T> IRowMapper<T> selectRowMapper(RowMapperContext<T> context);
}
```

- **现有硬编码位置**：`JdbcSqlExecutor` 按 `dbType` + 结果类型选 RowMapper。
- **接线方式**：选择逻辑先查桩点，未命中走现有 dbType 判定。结果集装配定制点。

#### 9. `SelectFieldPoint` — SELECT 投影字段（MERGE，SELECT）

```java
public interface SelectFieldPoint extends OptionPoint {
    FieldContribution contributeSelectFields(FieldContext context);
}
```

- **现有硬编码位置**：`TableQuery.columns`、`WrapperBuildUtil.buildWhereColumns`（默认 `SELECT *`）。
- **接线方式**：`buildWhereColumns` 改为合并桩点贡献的投影字段。列级数据权限（只允许查某几列）落点。

#### 10. `UpdateFieldPoint` — UPDATE 字段聚合（MERGE，UPDATE）

```java
public interface UpdateFieldPoint extends OptionPoint {
    FieldContribution contributeUpdateFields(FieldContext context);
}
```

- **现有硬编码位置**：`TableUpdate.updateSets`、`WrapperBuildUtil.buildUpdateSql`。
- **接线方式**：与 `InsertFieldPoint` 对称，`buildUpdateSql` 前合并桩点贡献。审计字段自动填充（update_time/update_by）落点。

#### 11. `CountPoint` — COUNT SQL 生成（SINGLE，SELECT）

```java
public interface CountPoint extends OptionPoint {
    SqlStatement buildCountSql(SqlContext context);
}
```

- **现有硬编码位置**：`count()` API 生成 `SELECT COUNT(*)`（wrapper 查询体系内）。
- **接线方式**：count 语句生成时查桩点，可定制估算 COUNT、跳过 JOIN 的优化 count。

#### 12. `SqlBuildPoint` — 最终 SQL 改写链（CHAIN，全操作）

```java
public interface SqlBuildPoint extends OptionPoint {
    SqlStatement buildSql(SqlContext context);
}
```

- **现有硬编码位置**：`WrapperBuildUtil` 的四个模板（INSERT/DELETE/UPDATE/SEARCH）+ `SqlUtil.getRunSqlByJdbc`。
- **接线方式**：`jdbcSql()` 返回前执行改写链。脱敏 SQL 审计、SQL 改写类插件（如强制加 hint）的兜底扩展点。

### 二、框架目前无对应功能，接线需先实现功能本体

#### 13. `BeforeExecutionPoint` / `AfterExecutionPoint` / `ExecutionErrorPoint` — 执行生命周期（CHAIN，全操作）

```java
public interface BeforeExecutionPoint extends OptionPoint {
    void beforeExecution(ExecutionContext context);
}
public interface AfterExecutionPoint extends OptionPoint {
    <R> R afterExecution(ExecutionResultContext<R> context);
}
public interface ExecutionErrorPoint extends OptionPoint {
    RuntimeException onExecutionError(ExecutionErrorContext context);
}
```

- **接线位置（需新增）**：`JdbcSqlExecutor` 各 execute 方法的 try 前 / 正常返回后 / catch 内。
- **注意**：dlz-lens 已有 SQL 采集（走 DbLogUtil 日志通道），接线前先明确两套机制的边界，避免重复采集。
- 典型插件：慢 SQL 告警、执行耗时统计、异常统一转译。

#### 14. `JdbcParameterPoint` — 参数绑定（SINGLE，全操作）

```java
public interface JdbcParameterPoint extends OptionPoint {
    void bindJdbcParameter(ParameterContext context) throws SQLException;
}
```

- **接线位置（需新增）**：`JdbcValueUtils` 的 `setNull`/`setObject` 分派处。
- **接线方式**：绑定前查桩点，命中则完全接管绑定（如定制 JSON 列绑定、Oracle 特殊类型）。

#### 15. `InsertConflictPoint` — INSERT 冲突策略（SINGLE，INSERT）

```java
public interface InsertConflictPoint extends OptionPoint {
    InsertConflictMode resolveInsertConflict(CrudContext context);
}
```

- **功能本体**：目前无 `ON DUPLICATE KEY UPDATE` / `ON CONFLICT` 支持。
- **实现路线**：方言层加冲突子句生成 → `WrapperBuildUtil.buildInsertSql` 按桩点结果拼接。
- 三个枚举值已设计：`ERROR / IGNORE / UPDATE`。

#### 16. `OptimisticLockPoint` — 乐观锁版本字段（MERGE，UPDATE）

```java
public interface OptimisticLockPoint extends OptionPoint {
    FieldContribution contributeOptimisticLock(FieldContext context);
}
```

- **功能本体**：目前无版本字段自动追加（`version = version + 1 WHERE version = ?`）。
- **实现路线**：`buildUpdateSql` 时合并桩点贡献的 SET 值 + `applyWherePoints` 追加版本条件；`AfterExecutionPoint`（13）检查影响行数为 0 时抛冲突异常。
- 内置 `LogicDeleteOption` 的跨操作桩点组合方式可直接复用。

#### 17. `UpdateSafetyPoint` / `DeleteSafetyPoint` — 无条件更新/删除防护（SINGLE）

```java
public interface UpdateSafetyPoint extends OptionPoint {
    boolean allowUnsafeUpdate(CrudContext context);
}
public interface DeleteSafetyPoint extends OptionPoint {
    boolean allowUnsafeDelete(CrudContext context);
}
```

- **功能本体**：目前 UPDATE/DELETE 空条件未强制拦截（仅 SELECT 有 `"WHERE false"` 防护）。
- **实现路线**：`buildUpdateSql` / `IExecutorDelete.execute` 中空 WHERE 时查桩点，未放行则抛异常。生产防误删的重要防线，建议早做。

#### 18. `ResultMapperPoint` — 结果类型转换（SINGLE，SELECT）

```java
public interface ResultMapperPoint<S, T> extends OptionPoint {
    T mapResult(ResultMappingContext<S, T> context);
}
```

- **接线位置（需新增）**：查询结果从 Map/Row 到目标类型（Bean/List）的最终转换处。
- 典型插件：结果脱敏（手机号打码）、自动字典翻译。

#### 19. `TableRoutePoint` — 物理表名路由（SINGLE，全操作）

```java
public interface TableRoutePoint extends OptionPoint {
    String routeTable(CrudContext context);
}
```

- **接线位置（需新增）**：wrapper 的 `getTableName()` 统一出口（`AParaTable`）。
- 典型插件：按时间分表（`order_202608`）、按租户分表。
- **注意**：与 `DataSourceRoutePoint`（5）区分——路由的是表名不是数据源。

---

## 恢复某个桩点的标准流程

1. 按"接入难度"分组顺序选目标（第一组迁移现有硬编码，风险最低）；
2. 在 `option/point` 下恢复接口文件（签名见上文，避免随手改动）；
3. 恢复/新建所需 `context` 值对象（同样签名见上文）；
4. 在 `StandardOptionPoints.REGISTRY` 注册；
5. **改造框架调用点**（本文件每个条目都标注了位置）；
6. 在 `OptionPointMechanismTest` 增加机制测试：注册供给型拦截器 → 断言 SQL/行为变化；
7. 全量回归（含 `OptionPointBindingsTest` 的注册矩阵断言更新为对应数量）。
