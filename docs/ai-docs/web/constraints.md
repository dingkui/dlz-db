# 硬约束（DLZ-DB）

> 以下 10 条为编写 DLZ-DB 代码时的硬性约束，违反将导致运行时错误。
> 完整 API 参考见 [dlz-db-速读.md](../dlz-db-速读.md)

---

1. **无 Mapper/DAO/Wrapper 类**，直接 `DB.Pojo.*`
2. **占位符不混用**：`DB.Jdbc` 用 `?`；`DB.Sql` / `.sql()` 用 `#{key}`
3. **返回值区分**：`queryOne/List/Page` 返回 `ResultMap`，要 Bean 用 `queryBean` 系列
4. **查询列用 `columns()`** 不是 `select()`（`.columns(User::getId, User::getName)`）
5. **insert 直接执行**：`DB.Pojo.insert(entity)` 直接执行并返回 entity（含自动填充主键），无需 `.execute()`
6. **物理删除**：绕过逻辑删除用 `.ignoreLogicDelete(true)`
7. **预设 SQL key** 必须以 `"key."` 开头
8. **`in()` 参数限制**：仅支持 `List` / CSV 字符串 / `"sql:子查询"`，不可传单值
9. **批量操作**用 `DB.Batch.insert(users)`，不是 `insertBatch()`
10. **`${key}` 安全**：`#{key}` 与 `${key}` 不可混用；`${key}` 仅用于列名/排序/SQL 片段拼接，不可作为用户输入值
