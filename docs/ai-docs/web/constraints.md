# 硬约束（DLZ-DB）

> 以下 11 条为编写 DLZ-DB 代码时必须遵守的约束；部分错误会在运行期暴露，写入条件遗漏还可能造成数据事故。
> 完整 API 参考见 [dlz-db-速读.md](../dlz-db-速读.md)

---

1. **无需自写 Mapper/DAO**，直接使用 `DB.pojo.*` 返回的框架 Wrapper
2. **占位符不混用**：`DB.jdbc` 用 `?`；`DB.sql` / `.sql()` 用 `#{key}`
3. **返回值区分**：`queryOne/List/Page` 返回 `ResultMap`，要 Bean 用 `queryBean` 系列
4. **查询列用 `select()`**（`.select(User::getId, User::getName)`）
5. **insert 直接执行**：`DB.pojo.insert(entity)` 直接执行并返回 entity（含自动填充主键），无需 `.execute()`
6. **物理删除**：PojoDelete 用 `.physical()`，直接 API 用 `DeleteOption.PHYSICAL`
7. **预设 SQL key** 必须以 `"key."` 开头
8. **`in()` 参数限制**：支持数组 / `Collection` / CSV 字符串 / `"sql:子查询"`，不可传标量单值
9. **批量操作**用 `DB.batch.insert(users)`，不是 `insertBatch()`
10. **`${key}` 安全**：`#{key}` 与 `${key}` 不可混用；`${key}` 仅用于列名/排序/SQL 片段拼接，不可作为用户输入值
11. **写入条件**：UPDATE/DELETE Wrapper 必须显式添加业务条件；逻辑删除的 `deleted = 0` 会让空 WHERE 兜底不触发，不可依赖框架自动拒绝全表写操作
