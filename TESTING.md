# DLZ-DB 8.0 测试与构建指南

**当前版本**：8.0.0
**最后核对**：2026-08-28

## 建议的验证命令

### 库模块

只验证 core、Spring Boot Starter 和 Solon Plugin：

```bash
mvn -B -pl dlz-db-core,dlz-db-spring-boot-starter,dlz-db-solon-plugin -am clean verify -Denforcer.skip=true
```

这三个库模块通过 `maven.compiler.release=8` 生成 Java 8 兼容产物。JDK 8 兼容任务只验证这些库模块，并跳过面向完整 reactor 的 JDK 17 Enforcer 规则。

### 根聚合项目与 Demo

根 POM 包含 `dlz-db-web-demos`，其中 Spring Boot 3 Demo 需要 JDK 17。因此聚合验证使用 JDK 17 或更高版本：

```bash
mvn -B clean verify -Djacoco.skip=false
```

2026-08-28 本地复现 CI 矩阵的结果：

| 环境 | 范围 | 结果 |
|---|---|---|
| JDK 8u66 | core、Spring Boot Starter、Solon Plugin | 1281 个测试，0 失败、0 错误 |
| JDK 17.0.10 | 完整 reactor（含三个 Demo） | 构建成功，1281 个测试，覆盖率门槛通过 |
| JDK 21.0.2 | 完整 reactor（含三个 Demo） | 构建成功，1281 个测试，覆盖率门槛通过 |

JDK 17 覆盖率快照为：core 行 81.97% / 分支 73.64%，Spring 行 90.09% / 分支 75%，Solon 行 85% / 分支 68.18%；均高于 POM 的行 70%、分支 60% 门槛。这些结果是当前工作树的本地快照，远端 CI 仍是合并前的最终依据。

### 单模块与单测试

```bash
mvn -B -pl dlz-db-core test
mvn -B -pl dlz-db-spring-boot-starter test
mvn -B -pl dlz-db-solon-plugin test

mvn -B -pl dlz-db-core -Dtest=DbPojoTest test
mvn -B -pl dlz-db-core -Dtest=TransactionTest test
```

## JDK 兼容策略

| 范围 | 目标/SDK | 建议验证环境 |
|---|---|---|
| `dlz-db-core` | Java 8 | JDK 8、17、21 |
| `dlz-db-spring-boot-starter` | Java 8 | JDK 8、17、21 |
| `dlz-db-solon-plugin` | Java 8 | JDK 8、17、21 |
| Spring Boot 2 Demo | Java 8 | JDK 8 或更高 |
| Solon 3 Demo | Java 8 | JDK 8 或更高 |
| Spring Boot 3 Demo | Java 17 | JDK 17 或更高 |
| 整个根聚合项目 | 包含 Boot 3 Demo | JDK 17 或更高 |

这意味着“库支持 Java 8”和“根目录包含只能在 JDK 17+ 构建的 Demo”可以同时成立。CI 若要使用 JDK 8 验证，应只选择三个库模块；不应在 JDK 8 job 中聚合构建 Spring Boot 3 Demo。

## GitHub Actions 验证矩阵

`.github/workflows/build-and-test.yml` 将构建拆成两类：

1. JDK 17/21 执行完整 reactor 的 `clean verify`，包含三个 Demo，并通过生命周期生成 JaCoCo 报告。
2. JDK 8 仅执行 core、Spring Boot Starter 和 Solon Plugin 的测试，验证发行库的 Java 8 兼容性；不聚合构建 Spring Boot 3 Demo。

CI 不再直接调用 `jacoco:report`，避免该目标传播到没有配置 JaCoCo 的 Demo 模块。

## 测试资源

- core 集成测试使用 SQLite，基类为 `BaseDBTest`。
- Spring Boot 和 Solon 测试也已在当前本地构建中自包含运行，不要再将它们描述为“必须外部 MySQL/Redis 才能测试”。
- Surefire 报告位于各模块 `target/surefire-reports/`。

## 覆盖率与质量脚本

`reports/` 中的 `.bat` 脚本是历史工具，部分命令与当前 POM 的默认 skip/profile 设置不一致。在这些脚本重新校验前，发行验证以上述 Maven `verify` 命令为准，不把未生成的 JaCoCo/SpotBugs 报告当成已完成检查。

## 历史报告

`reports/2026-05-17/` 是 7.0.0 时期的历史快照，其 605 个测试、覆盖率和代码行数不代表当前 8.0.0 工作树。该目录只用于历史追溯。
