# DLZ-DB 8.0 AI 编程契约

> 这是生成、修改和审查 DLZ-DB 业务代码时唯一必读的编程规则。其他 AI 文档只描述框架差异或特定任务，不重复本文件。

适用版本：

- `top.dlzio:dlz-db-core:8.0.0`
- `top.dlzio:dlz-db-spring-boot-starter:8.0.0`
- `top.dlzio:dlz-db-solon-plugin:8.0.0`

发行库兼容 Java 8。若生成的代码属于通用库或 Java 8 应用，不使用 `Map.of`、`List.of`、`var`、record、文本块等高版本语法。

## 1. 先选择唯一默认入口

| 需求 | 默认入口 | 参数方式 |
|---|---|---|
| 有实体类的单表 CRUD | `DB.pojo` | Lambda 字段引用 |
| 无实体类、动态表名 | `DB.table` | 表名、列名、`JSONMap` |
| 临时、一次性原生 SQL | `DB.jdbc` | `?` |
| 复杂、动态、可维护的预设 SQL | `DB.sql` | `#{name}` |
| 批量插入、更新或 JDBC 批处理 | `DB.batch` | 集合 |
| 注册或切换数据源 | `DB.ds` | 数据源名 |
| 编程式事务 | `DB.tx` | lambda |
| 初始化期配置 | `DB.config` | 启动前配置 |

Wrapper 主要解决单表条件构造。JOIN、CTE、UNION、窗口函数和复杂聚合优先使用 `DB.sql`；一次性 SQL 可使用 `DB.jdbc`。

## 2. API、SPI 与 internal 边界

业务代码默认只使用：

- `com.dlz.db`
- `com.dlz.db.wrapper`
- `com.dlz.db.model`
- `com.dlz.db.option`
- `com.dlz.db.core.anno`
- `com.dlz.db.core.ds`（仅在编程式注册动态数据源时）
- `com.dlz.kit.json`（仅在 Table、命名参数或 `ResultMap` 深层取值时；由 core 传递引入）

框架扩展任务才考虑 `ISqlExecutor`、`ITxExecutor`、`DlzDbAdapter`、`DbDialect`、`DialectRegistry`、`SqlBuildInterceptor` 等 SPI，并应先读维护者文档和源码。

`com.dlz.db.internal.*` 是实现包。业务代码、示例、工具类和生成代码都不得 import。Wrapper 以及 `Sort/Page` 等模型的少量继承签名可能显示 internal 类型，但不要显式声明、构造或扩展这些类型。

常用 import：

~~~java
import com.dlz.db.DB;
import com.dlz.db.core.anno.IdType;
import com.dlz.db.core.anno.TableField;
import com.dlz.db.core.anno.TableId;
import com.dlz.db.core.anno.TableName;
import com.dlz.db.core.ds.DataSourceProperty;
import com.dlz.db.model.BatchResult;
import com.dlz.db.model.Page;
import com.dlz.db.model.PageRequest;
import com.dlz.db.model.ResultMap;
import com.dlz.db.option.DeleteOption;
import com.dlz.db.option.SelectOption;
import com.dlz.kit.json.JSONMap;
~~~

## 3. 执行模型和返回值

| 调用 | 是否立即执行 | 返回 |
|---|---|---|
| `DB.pojo.insert(entity)` | 是 | 传入实体，生成主键会回填 |
| `selectById/selectByIds/existsById` | 是 | 实体、列表、boolean |
| `updateById/deleteById/deleteByIds` | 是 | 影响行数 `int` |
| `DB.table.insert/updateById/deleteById` | 是 | 影响行数 `int` |
| `DB.table.insertWithAutoKey` | 是 | 自动主键 `Long` |
| 查询 Wrapper | 否 | 以 `query*` 或 `count()` 终止 |
| insert/update/delete Wrapper | 否 | 以 `execute()` 或插入主键方法终止 |
| `DB.jdbc/DB.sql` 的直接方法 | 是 | 方法声明对应结果 |
| `DB.batch.*` | 是 | `BatchResult` |

不要笼统地说“所有写操作都要 `.execute()`”。直接 CRUD 立即执行；只有 Wrapper 写操作需要终止方法。

查询返回规则：

| 方法 | 返回 |
|---|---|
| `queryBean/queryBeanList/queryBeanPage` | 当前 Pojo 类型的 `T/List<T>/Page<T>` |
| `queryOne/queryList/queryPage` | `ResultMap/List<ResultMap>/Page<ResultMap>` |
| `queryOne(C)/queryList(C)/queryPage(C)` | 指定类型 |
| `queryFirst/queryFirst(C)/queryFirstBean` | 非严格取第一条 |
| `queryStr/queryLong/queryInt/queryDouble` 及 List 版 | 第一列标量 |
| `count()` | `long` |

`queryOne/queryBean` 是严格单条：无结果返回 `null`，多条抛非唯一结果异常。不确定唯一性时使用 `queryFirst/queryFirstBean`。

`Page<T>` 位于 `com.dlz.db.model`，核心属性是 `current`、`size`、`total`、`pages`、`records`。使用 `getRecords()/getTotal()` 或 `records()/total()`，不存在 MyBatis-Plus `IPage`，也不存在 `rows` 属性。

## 4. Entity 规则

默认类名和 getter 名按驼峰转下划线；只有不符合默认映射时才写 `@TableName`、`@TableField`。

~~~java
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("mobile_no")
    private String mobile;

    private Integer status;
    private Integer deleted;

    // getter / setter
}
~~~

- `@TableId` 默认值是 `IdType.SEQ`，不要把它误认为数据库自增。
- 数据库自增主键必须显式写 `@TableId(type = IdType.AUTO)`。
- 其他可选策略包括 `INPUT`、`ASSIGN_ID`、`ASSIGN_UUID`。
- Entity 放在哪个业务包、是否使用 Lombok 或 OpenAPI/Swagger 注解，不是 DLZ-DB 的强制规则。

默认逻辑删除字段名是 `deleted`：

- 目标数据库表存在该列时，查询自动附加 `deleted = 0`。
- 单条插入会根据目标表补入 `deleted = 0`；Pojo 批量插入若要同步回填，还需要 Bean 中存在对应映射字段。
- 删除默认改写为设置 `deleted = 1`。
- 直接查询接口如 `selectById` 可传 `SelectOption.INCLUDE_DELETED`。
- 当前 Pojo 查询 Wrapper 没有对称的稳定便捷方法用于包含已删除数据。
- `.physical()` 只属于 Pojo Delete Wrapper；直接按单个主键物理删除可用 `deleteById(..., DeleteOption.PHYSICAL)`。
- Table 条件物理删除当前没有对称的稳定便捷入口；确有需要时使用带明确业务条件和参数绑定的 `DB.jdbc.execute("DELETE ... WHERE ...")`。

## 5. Pojo CRUD

~~~java
// 按主键和主键集合查询
User user = DB.pojo.selectById(User.class, id);
List<User> users = DB.pojo.selectByIds(User.class, ids);
boolean exists = DB.pojo.existsById(User.class, id);

// 条件列表
List<User> enabled = DB.pojo.selectWrapper(User.class)
    .select(User::getId, User::getName)
    .eq(User::getStatus, 1)
    .orderByDesc(User::getId)
    .queryBeanList();

// 分页
Page<User> page = DB.pojo.selectWrapper(User.class)
    .eq(status != null, User::getStatus, status)
    .page(pageNo, pageSize)
    .queryBeanPage();

// 直接写操作立即执行
DB.pojo.insert(user);
int updated = DB.pojo.updateById(user);
int deleted = DB.pojo.deleteById(User.class, id);

// 部分更新和条件删除使用 Wrapper
int changed = DB.pojo.updateWrapper(User.class)
    .set(User::getStatus, 2)
    .eq(User::getId, id)
    .execute();

int removed = DB.pojo.deleteWrapper(User.class)
    .eq(User::getId, id)
    .execute();
~~~

`insertOrUpdateById(entity)` 只按主键是否为空选择 INSERT 或 UPDATE，不是数据库原子 Upsert；并发场景不能把它当成唯一键冲突处理。

## 6. 条件、组合与排序

常用条件：

- 比较：`eq/ne/gt/ge/lt/le`
- 空值：`isNull/isNotNull`
- 集合：`in/notIn`
- 范围：`between/notBetween`
- 模糊：`like/likeLeft/likeRight/notLike`
- 分组：`ands(a -> ...)`、`ors(o -> ...)`
- 自定义片段：`sql(text, JSONMap)`

多数条件有布尔开关重载：

~~~java
DB.pojo.selectWrapper(User.class)
    .eq(status != null, User::getStatus, status)
    .like(keyword != null, User::getName, keyword)
    .queryBeanList();
~~~

组合条件：

~~~java
boolean hasKeyword = keyword != null && !keyword.isEmpty();

DB.pojo.selectWrapper(User.class)
    .eq(User::getStatus, 1)
    .ors(o -> o.like(hasKeyword, User::getName, keyword)
               .like(hasKeyword, User::getMobile, keyword))
    .queryBeanList();
~~~

keyword 无效时两个子条件都会跳过，空的 `ors` 分组不会生成 SQL。不要 import lambda 参数的 internal `Condition` 类型，让 Java 自行推断。

`in/notIn` 优先传 `Collection`、`Object[]`、CSV 字符串或 `"sql:子查询"`。源码也接受 `Number` 标量，但单值条件使用 `eq/ne` 更清楚；原生类型数组不受支持。ID 集合为空时应在业务代码提前返回，避免无意义 SQL。

查询列使用 `.select(...)`，复合条件使用 `.ands/.ors`，不要生成 `.columns/.and/.or`。

## 7. Table API

~~~java
List<ResultMap> rows = DB.table.selectWrapper("user")
    .select("id", "name", "status")
    .eq("status", 1)
    .queryList();

JSONMap values = new JSONMap("name", "张三", "status", 1);
Long id = DB.table.insertWithAutoKey("user", values);

int changed = DB.table.updateWrapper("user")
    .set("status", 2)
    .eq("id", id)
    .execute();

int removed = DB.table.deleteWrapper("user")
    .eq("id", id)
    .execute();
~~~

`DB.table.insert(...)` 返回影响行数；只有 `insertWithAutoKey(...)` 返回 `Long` 主键。动态表名、列名必须来自服务端白名单，不得直接使用用户输入。

## 8. JDBC

~~~java
List<ResultMap> rows = DB.jdbc.list(
    "SELECT * FROM user WHERE status = ?", 1);

List<User> users = DB.jdbc.list(
    "SELECT * FROM user WHERE status = ?", User.class, 1);

Page<User> page = DB.jdbc.page(
    "SELECT * FROM user WHERE status = ?",
    PageRequest.of(1, 20), User.class, 1);

int changed = DB.jdbc.execute(
    "UPDATE user SET status = ? WHERE id = ?", 2, id);
~~~

`DB.jdbc` 只使用 `?`，不使用 `#{name}`。

当前 JDBC 自动 count 改写通过查找精确的 `" FROM "` 构造计数 SQL，大小写和格式敏感；`DISTINCT`、`GROUP BY`、`UNION`、CTE 等复杂 SQL 不应依赖自动改写。复杂分页应写明确的查询与计数方案，或使用经过验证的预设 SQL。

## 9. 预设 SQL

默认加载 `classpath*:sql/app/*.sql`；`dlz.db.sqllist` 默认值是 `app/*`。文件扩展名是 `.sql`，内容是 `<sqlList>` XML：

~~~xml
<sqlList>
    <sql sqlId="key.user.findActive"><![CDATA[
        SELECT * FROM user WHERE status = #{status}
    ]]></sql>
</sqlList>
~~~

~~~java
List<User> users = DB.sql.selectWrapper("key.user.findActive")
    .addPara("status", 1)
    .queryList(User.class);

Page<User> page = DB.sql.selectWrapper("key.user.findActive")
    .addPara("status", 1)
    .page(1, 20)
    .queryPage(User.class);

int changed = DB.sql.execute(
    "key.user.disable", new JSONMap("id", id));
~~~

- XML 中的 `sqlId` 按原值存储，必须与调用 key 一致，并按当前规则以 `key.` 开头。
- 运行参数使用 `#{name}`；预设 SQL 引用可使用 `#{key.xxx}`。
- `${name}` 是直接 SQL 文本替换，只能用于经过白名单校验的列名、排序或固定片段，不能放入用户值。
- 预设 SQL 在初始化时加载；当前没有公共热重载 API。

## 10. 批量

~~~java
BatchResult result = DB.batch.insert(users, 500);
if (!result.isSuccess()) {
    // 检查 status()、failedPositions() 和 cause()
}

BatchResult updateResult = DB.batch.update(users, 500);
BatchResult tableResult = DB.batch.insert("user", valueMaps, 500);
BatchResult jdbcResult = DB.batch.execute(sql, parameterArrays, 500);
~~~

`BatchResult` 还提供 `totalItems()`、`batchCount()`、`completedBatches()`、`knownAffectedRows()` 和 `unknownAffectedRows()`。它不是 boolean，也没有 `hasFailure()`。

批量入口是 `DB.batch.insert/update/execute`，不存在 `insertBatch()`，当前也没有 `DB.batch.delete(...)`。批量删除使用 `deleteByIds` 或带 `in(...)` 的 Delete Wrapper。

批量 API 使用固定全列语义，不能按单条 CRUD 推断：

- Pojo/Table 批量插入会绑定全部可写列，普通 `null` 会写成 SQL `NULL`；启用逻辑删除时，可识别的逻辑删除值会被强制改成 `0`，并修改传入的 Entity 或 `JSONMap`。
- Pojo/Table 批量更新会更新主键之外的全部映射列，`null` 会覆盖数据库值；每条数据都必须带真实主键。
- 批量更新的 WHERE 只有主键，不会自动追加 `deleted = 0`。Pojo 中的旧值或 Table 缺失后补出的 `0` 都可能把已删除记录“复活”。部分字段更新或必须排除已删除记录时，改用带明确条件的 UPDATE Wrapper，或显式 `DB.batch.execute("... WHERE id = ? AND deleted = 0", ...)`。

## 11. 事务、多数据源与配置

~~~java
List<User> users = DB.ds.use("slave", () ->
    DB.pojo.selectWrapper(User.class).queryBeanList()
);

DB.tx.run(() -> {
    DB.pojo.insert(order);
    DB.pojo.insert(orderItem);
});

Order saved = DB.tx.run(() -> DB.pojo.insert(order));

DB.tx.run("tenant_001", () -> {
    DB.pojo.insert(order);
});
~~~

- `DB.ds.use` 只切换当前作用域的数据源，不开启事务。
- `DB.tx.run(name, ...)` 是指定单个数据源的本地事务。
- 跨数据源调用不提供分布式原子提交或回滚。
- Spring 可使用 `@Transactional`，Solon 可使用 `@Tran`。
- 命名动态数据源通过 `DataSourceProperty` 描述，再交给 `DB.ds.setDataSource(...)` 创建和注册；已有 `DataSource` 只能通过 `setDefaultDataSource(...)` 设置为默认源。

~~~java
DataSourceProperty tenant = new DataSourceProperty();
tenant.setName("tenant_001");
tenant.setDriverClassName("com.mysql.cj.jdbc.Driver");
tenant.setUrl(tenantJdbcUrl);
tenant.setUsername(tenantUsername);
tenant.setPassword(tenantPassword);
DB.ds.setDataSource(tenant);
~~~

`DB.config` 主要用于只依赖 core 时的手动启动：先注册方言、插件、预设 SQL、逻辑删除字段、数据源或底层执行器，再调用它自己的 `init()`；此后配置不可变。Spring Boot 和 Solon 集成会直接完成框架初始化，应使用容器 `DataSource` 和 `dlz.db.*` 配置，不要期待 `DB.config` setter 影响自动集成。当前 `columnNameConvertor(...)` 仍是未接入执行路径的保留方法，不要生成对它的调用。

## 12. 写入安全

UPDATE 和 DELETE Wrapper 必须显式添加业务条件，并在执行前验证关键参数不为 null。

不要依赖框架的空 WHERE 兜底：当最终 WHERE 为空时框架可能生成 `WHERE false`，但逻辑删除会自动注入 `deleted = 0`，使 WHERE 不再为空。此时遗漏业务条件可能更新或删除全部未删除记录。

~~~java
if (id == null) {
    throw new IllegalArgumentException("id must not be null");
}

DB.pojo.updateWrapper(User.class)
    .set(User::getStatus, 2)
    .eq(User::getId, id)
    .execute();
~~~

原生 SQL 写操作同样必须带明确 WHERE；动态表名、列名、排序和 SQL 片段必须使用白名单。

## 13. 生成代码硬约束

- **DB-AI-001**：门面字段使用小写 `DB.pojo/table/jdbc/sql/batch/ds/tx/config`。
- **DB-AI-002**：普通 CRUD 不生成 Mapper、DAO 或 XML 映射层。
- **DB-AI-003**：业务代码禁止依赖 `com.dlz.db.internal.*`。
- **DB-AI-004**：查询列用 `.select`；组合条件用 `.ands/.ors`。
- **DB-AI-005**：`DB.jdbc` 用 `?`；`DB.sql` 和条件 `sql()` 用命名占位。
- **DB-AI-006**：直接 CRUD 立即执行；Wrapper 写操作才以执行方法结束。
- **DB-AI-007**：UPDATE/DELETE 必须有显式业务条件，不依赖空条件保护。
- **DB-AI-008**：`in/notIn` 优先传 `Collection` 或 `Object[]`；单值用 `eq/ne`，禁止原生类型数组，空集合提前处理。
- **DB-AI-009**：Bean 结果用 `queryBean*`；ResultMap 结果用 `query*`。
- **DB-AI-010**：批量结果按 `BatchResult` 检查，不当作 boolean；批量更新只接收完整行快照，并显式处理逻辑删除边界。
- **DB-AI-011**：数据库自增主键显式使用 `IdType.AUTO`。
- **DB-AI-012**：`${name}`、动态标识符和 SQL 片段只能接收白名单值。
- **DB-AI-013**：数据源切换不是事务，跨数据源事务不是分布式事务。
- **DB-AI-014**：生成通用库代码时保持 Java 8 兼容。

## 14. 禁止幻觉和自检

不要生成这些旧写法或不存在的类型：

- `DB.Pojo`、`DB.Table`、`DB.Jdbc` 等大写门面。
- `JdbcQuery`、`SpringDlzDbConfig`、MyBatis-Plus `IPage`。
- `com.dlz.db.modal.*` 或任何 `com.dlz.db.internal.*` import。
- `.columns()`、`.and()`、`.or()`、`insertBatch()`。
- `Page.rows/getRows()`、`BatchResult.hasFailure()`。

提交前逐项检查：

1. 入口与占位符是否匹配。
2. 返回类型、包名和终止方法是否真实存在。
3. UPDATE/DELETE 是否有经过验证的业务条件。
4. 主键策略、逻辑删除和事务边界是否符合数据库设计。
5. 是否引入 internal 或其他框架的同名类型。
6. 是否符合目标 Java 版本。
7. 是否完成编译和相关测试。

完整 reactor 使用 JDK 17+ 执行 `mvn -B clean verify`。只验证发行库的 Java 8 兼容命令见根目录 `TESTING.md`。

## 15. 按需继续读取

- Spring Boot：[`1.框架差异/spring-boot.md`](./1.框架差异/spring-boot.md)
- Solon：[`1.框架差异/solon.md`](./1.框架差异/solon.md)
- MyBatis 迁移：[`2.任务指南/migration-mybatis.md`](./2.任务指南/migration-mybatis.md)
- Web 分层：[`2.任务指南/web-layer.md`](./2.任务指南/web-layer.md)
- JSONMap / JSONList：[`3.按需能力/dlz-kit.md`](./3.按需能力/dlz-kit.md)
