# DLZ-DB 测试资料目录

当前测试命令、JDK 策略和 CI 差异统一以 [TESTING.md](../TESTING.md) 为准。

## 历史报告

`2026-05-17/` 保存的是 7.0.0 时期的测试和代码统计快照。目录中的 605 个测试、覆盖率、行数和耗时不代表当前 8.0.0 工作树，不能单独用作当前发行证据。

## 历史脚本

本目录的 `.bat` 脚本早于当前根 POM 和 Demo 结构，部分 JaCoCo、SpotBugs、PMD 命令尚未与当前配置重新校验。日常验证请优先直接运行：

```bash
# 三个库模块
mvn -B -pl dlz-db-core,dlz-db-spring-boot-starter,dlz-db-solon-plugin -am clean verify

# 整个聚合项目（JDK 17+）
mvn -B clean verify
```

在脚本的 profile、报告路径和外部依赖说明完成更新前，不建议把它们接入发布流程。
