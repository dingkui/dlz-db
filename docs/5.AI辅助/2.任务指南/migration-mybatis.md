# MyBatis / MyBatis-Plus 迁移任务

> 仅在迁移任务中加载。开始前必须先读 [DLZ-DB AI 编程契约](../dlz-db-速读.md)，本文件只描述迁移步骤和差异。

## 迁移原则

不要机械地把所有 SQL 改成 Wrapper：

| 原代码 | DLZ-DB 默认去向 |
|---|---|
| `BaseMapper<T>` 单表 CRUD | `DB.pojo` |
| `QueryWrapper/LambdaQueryWrapper` | Pojo Query Wrapper |
| 无实体类、动态表名 | `DB.table` |
| 简单 `@Select/@Update` | `DB.jdbc` |
| 复杂 XML SQL、动态 SQL、可复用报表 | `DB.sql` 预设 SQL |
| MyBatis-Plus 批量方法 | `DB.batch` |
| MP `Page/IPage` | `com.dlz.db.model.Page` |
| Spring 事务 | 继续使用 `@Transactional` |

DLZ-DB 不要求 Mapper/DAO，但迁移可以分阶段进行。只要避免事务、数据源和实体注解冲突，旧 Mapper 与 DLZ-DB 可以暂时共存；不要为了“一次删干净”扩大上线风险。

## 建议步骤

1. 盘点 Mapper、XML、分页、批量、逻辑删除、主键策略、事务和数据源。
2. 引入目标依赖并完成 Spring Boot 或 Solon 初始化。
3. 先迁简单只读查询，再迁单表写入。
4. 将复杂 XML SQL 迁到 `resources/sql/` 下的预设 SQL，而不是强塞进 Wrapper。
5. 旧 Mapper 下线前保留其实体上的 MP 注解；下线后再决定是否替换，并逐个核对属性语义。
6. 迁移批量与事务，验证影响行数、回滚和数据源作用域。
7. 删除确认无调用的 Mapper、XML 和旧依赖。
8. 编译、跑测试并对关键 SQL 做结果与执行计划对比。

## 代码映射

~~~java
// MyBatis-Plus
userMapper.selectList(
    Wrappers.<User>lambdaQuery()
        .eq(status != null, User::getStatus, status));

// DLZ-DB
DB.pojo.selectWrapper(User.class)
    .eq(status != null, User::getStatus, status)
    .queryBeanList();
~~~

~~~java
// MyBatis-Plus
userMapper.update(
    null,
    Wrappers.<User>lambdaUpdate()
        .set(User::getStatus, 2)
        .eq(User::getId, id));

// DLZ-DB
DB.pojo.updateWrapper(User.class)
    .set(User::getStatus, 2)
    .eq(User::getId, id)
    .execute();
~~~

## Entity 注解

DLZ-DB 8.0 会通过反射识别 MP 常用的 `@TableName`、`@TableField`、`@TableId` 及主键策略。渐进迁移时，旧 Mapper 仍使用某个实体，就先保留该实体的 MP 注解；不要为了 DLZ-DB 提前替换 import。兼容映射不等于支持 MP 的全部注解属性或插件行为。

旧 Mapper 下线后，如果把 `com.baomidou.mybatisplus.annotation.*` 替换为 `com.dlz.db.core.anno.*`，必须逐项核对：

- `@TableName`、`@TableField` 只迁移 DLZ-DB 支持的属性。
- `@TableId` 默认策略不同；数据库自增显式写 `IdType.AUTO`。
- `@TableLogic` 不直接迁移。DLZ-DB 默认按 `deleted` 字段启用逻辑删除。
- MP 自动填充、乐观锁、租户插件、枚举处理器等不能假定有等价实现。

## 复杂 SQL

保留 SQL 的结构和含义，把调用入口迁到 `DB.sql`：

~~~java
List<UserReport> reports = DB.sql.selectWrapper("key.report.userSummary")
    .addPara("startTime", startTime)
    .addPara("endTime", endTime)
    .queryList(UserReport.class);
~~~

参数值用 `#{name}`。只有经过白名单校验的动态列名、排序或固定片段才使用 `${name}`。

## 迁移时重点验证

- `queryOne/queryBean` 的严格单条语义是否符合旧代码。
- `Page.records/total` 与原分页响应字段是否需要适配。
- `insertOrUpdateById` 不是数据库原子 Upsert。
- 逻辑删除会影响查询和删除 SQL。
- 所有 UPDATE/DELETE 都有显式业务条件。
- `DB.ds.use` 不会自动开启事务，跨数据源不提供分布式事务。
- 没有遗留 `IPage`、`QueryWrapper` 或错误的同名注解 import。

面向人的完整迁移说明见 [`docs/7.版本迁移/7.1-从MyBatis迁移.md`](../../7.版本迁移/7.1-从MyBatis迁移.md)。
