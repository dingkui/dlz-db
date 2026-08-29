# 更新日志

本文记录 DLZ-DB 对使用者有影响的版本变化。当前只维护中文版本；提交级历史请查看 Git 记录和发行标签。

## 8.0.0（待发布）

8.0 建立新的稳定 API 基线。后续 `8.0.x` 版本以兼容修复和功能新增为主，不随意删除公共 API、修改既有签名或收窄可见性。SPI 与实现包的承诺范围见 [API、SPI 与实现边界](./docs/6.参考手册/6.4-API-SPI与实现边界.md)。

### 新增

- 增加 `SqlBuildInterceptor` 扩展契约，并通过 `DB.config.plugin(...)` 注册；逻辑删除通过内置拦截器接入。`DbPlugin` 注册中心本身不属于稳定 SPI。
- 增加 `DB.table` 按表名执行的直接 CRUD，包括按主键查询、更新和删除。
- 增加 `DB.config` 配置入口，统一公开入口为 `DB.pojo/table/jdbc/sql/batch/ds/tx/config`。
- 增加动态数据源连接测试能力。
- 增加 Spring Boot Starter、Solon Plugin 和示例工程的统一聚合验证。

### 调整

- Wrapper API 统一使用 `selectWrapper`、`insertWrapper`、`updateWrapper` 和 `deleteWrapper` 命名。
- `DB.pojo` 与 `DB.table` 的直接 CRUD 立即执行；Wrapper 写操作继续以 `.execute()` 终止。
- 发行库模块保持 Java 8 兼容，包含 Spring Boot 3 Demo 的完整聚合构建使用 JDK 17 或更高版本。
- 日志调用位置由框架写入 MDC，最终显示形式由应用日志 pattern 和控制台决定。

### 移除或替代

- 移除 `DbJdbc.insert/update/delete`，统一使用 `DB.jdbc.execute(...)`。
- 移除 `DbSql.insert/update/delete`，统一使用 `DB.sql.execute(...)`。
- 移除接受条件 Bean 的旧 `DbPojo.select(...)`、`selectWrapper(...)` 和 `deleteWrapper(...)` 重载，改用类型入口与条件链。
- 不再把 `com.dlz.db.internal` 下的实现类型作为用户可依赖 API。

### 升级

从 7.x 升级时应通过编译错误和回归测试逐项处理包名、自动配置和 Wrapper 签名变化，详见 [v7 升级到 v8](./docs/7.版本迁移/7.2-v7升级到v8.md)。
