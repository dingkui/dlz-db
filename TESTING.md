# DLZ-DB 测试与构建指南

本文只维护可复现的验证方式。当前版本、编译目标、覆盖率门槛和 CI 矩阵分别以 POM 与 GitHub Actions 工作流为事实源，不在文档中复制某次运行的测试数量或覆盖率快照。

## JDK 策略

| 范围 | 发行目标 | 构建环境 |
|---|---|---|
| `dlz-db-core` | Java 8 | JDK 8、17、21 |
| `dlz-db-spring-boot-starter` | Java 8 | JDK 8、17、21 |
| `dlz-db-solon-plugin` | Java 8 | JDK 8、17、21 |
| Spring Boot 2 Demo | Java 8 | JDK 8 或更高 |
| Solon Demo | Java 8 | JDK 8 或更高 |
| Spring Boot 3 Demo | Java 17 | JDK 17 或更高 |
| 根聚合项目 | 包含 Spring Boot 3 Demo | JDK 17 或更高 |

“发行库兼容 Java 8”和“根聚合构建要求 JDK 17+”并不冲突：前者描述发布字节码和 API 目标，后者由聚合项目中的 Spring Boot 3 Demo 决定。

## 常用命令

### 验证单个模块

```bash
mvn -B -pl dlz-db-core test
mvn -B -pl dlz-db-spring-boot-starter test
mvn -B -pl dlz-db-solon-plugin test
```

执行单个测试类：

```bash
mvn -B -pl dlz-db-core -Dtest=DbPojoTest test
```

### 验证三个发行库模块

在 JDK 17 或更高版本执行：

```bash
mvn -B -pl dlz-db-core,dlz-db-spring-boot-starter,dlz-db-solon-plugin -am clean verify
```

复现 CI 的 JDK 8 兼容任务：

```bash
mvn -B clean test \
  -pl dlz-db-core,dlz-db-spring-boot-starter,dlz-db-solon-plugin \
  -am \
  -Denforcer.skip=true \
  -Djacoco.skip=true \
  -Dspotbugs.skip=true
```

### 验证完整聚合项目

使用 JDK 17 或更高版本：

```bash
mvn -B clean verify -Djacoco.skip=false
```

该命令构建三个发行库模块和 Demo，并在启用 JaCoCo 时生成报告、执行 POM 中配置的覆盖率检查。

## CI 验证

`.github/workflows/build-and-test.yml` 当前包含两类任务：

1. JDK 17、21 验证完整 reactor，并生成 JaCoCo 报告。
2. JDK 8 只验证三个发行库模块，不聚合构建 Spring Boot 3 Demo。

远端 CI 是合并前的最终依据。本地构建成功不能替代其他 JDK 和干净环境中的验证。

## 测试资源与报告

- core 集成测试使用 SQLite，公共测试基类为 `BaseDBTest`。
- Spring Boot 和 Solon 测试均应保持自包含，不应依赖开发者本机的 MySQL、Redis 或私有配置。
- Surefire 报告位于各模块的 `target/surefire-reports/`。
- JaCoCo 报告位于各发行模块的 `target/reports/jacoco/`。
- 覆盖率最低要求读取根 POM 中的 `jacoco.line.coverage` 和 `jacoco.branch.coverage`。

`reports/` 中的批处理脚本属于辅助工具。发行判断以本页列出的 Maven 命令、根 POM 和 CI 工作流为准。

## 变更对应的最低验证

| 修改类型 | 最低验证 |
|---|---|
| 文档与注释 | 链接、代码围栏和示例 API 检查 |
| core 查询、写入或映射 | core 单元测试与相关集成测试 |
| Spring Boot 自动配置 | Starter 测试和至少一个 Boot Demo 构建 |
| Solon 插件 | Solon Plugin 测试和 Solon Demo 构建 |
| 公共 API 或 SPI | 完整构建、兼容性检查和文档同步 |
| POM、插件或 CI | JDK 8 库任务及 JDK 17/21 完整任务 |
