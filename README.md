# DLZ-DB

> 一个轻量、直接、面向 Java 8+ 的数据库操作工具包。

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Runtime](https://img.shields.io/badge/Runtime-Java%208+-green.svg)](https://www.oracle.com/java/)
[![Build JDK](https://img.shields.io/badge/Build%20JDK-17+-blue.svg)](https://www.oracle.com/java/)
[![Build Status](https://github.com/dingkui/dlz-db/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/dingkui/dlz-db/actions/workflows/build-and-test.yml)
[![Target Version](https://img.shields.io/badge/Target-8.0.0-orange.svg)](CHANGELOG.md)
[![codecov](https://codecov.io/gh/dingkui/dlz-db/graph/badge.svg?token=UDX6ZH1R0Q)](https://codecov.io/gh/dingkui/dlz-db)

DLZ-DB 通过统一的 `DB` 门面提供实体、动态表、JDBC、预置 SQL、批处理、多数据源和事务能力。简单 CRUD 不需要 Mapper 接口或 XML；复杂业务仍可按项目需要组织 Service、Repository 或 DAO。

```java
List<User> users = DB.pojo.selectWrapper(User.class)
        .eq(User::getStatus, 1)
        .like(User::getName, "张")
        .orderByDesc(User::getCreateTime)
        .queryBeanList();
```

当前源码目标版本为 **8.0.0**；发布后将作为新的 8.x 稳定 API 基线。8.0 之后公共 API 以兼容新增为主，SPI 和实现包有明确边界，详见 [API、SPI 与实现边界](./docs/6.参考手册/6.4-API-SPI与实现边界.md)。

## 适合什么项目

- 希望减少 Mapper、XML 和样板 CRUD 的中小型服务。
- 需要运行时注册、选择或隔离数据源的 SaaS、ETL 和工具项目。
- 需要保留 JDBC 与显式 SQL 控制力，同时统一分页、映射、批处理和事务入口的项目。
- 希望 AI 能通过一份短规范稳定生成数据库代码的项目。

如果团队依赖完整 ORM 的关系管理、实体状态跟踪、二级缓存或高度成熟的可视化生态，应先阅读 [选型与使用边界](./docs/1.产品介绍/1.2-选型与使用边界.md)。

## 核心特点

- **入口集中**：从 `DB.pojo/table/jdbc/sql/batch/ds/tx/config` 选择能力。
- **显式调用**：条件、数据源作用域和编程式事务直接体现在代码中。
- **渐进使用**：可与已有 JDBC、MyBatis 或 MyBatis-Plus 项目并存并逐步迁移。
- **动态数据源**：支持运行时注册数据源，并通过 lambda 控制使用范围。
- **结果映射**：支持 Bean、`ResultMap`、分页以及 JSON 深层路径取值。
- **可诊断**：可记录 SQL、耗时，并将业务调用位置写入 MDC。
- **AI 友好**：入口和返回规则集中，降低生成代码时的选择分支。

## 八个入口

| 入口 | 用途 |
|---|---|
| `DB.pojo` | 基于实体和 Lambda 的类型安全 CRUD |
| `DB.table` | 按动态表名和 `JSONMap` 操作 |
| `DB.jdbc` | 使用 `?` 参数的原生 JDBC SQL |
| `DB.sql` | 使用 `#{name}` 参数的动态或预置 SQL |
| `DB.batch` | 实体、表和 JDBC 批处理 |
| `DB.ds` | 数据源注册、测试和作用域切换 |
| `DB.tx` | 编程式事务 |
| `DB.config` | 核心初始化配置与扩展注册 |

入口选择、终止方法和返回类型的完整说明见 [公共 API](./docs/6.参考手册/6.1-公共API.md)。

## 快速开始

### Spring Boot

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>8.0.0</version>
</dependency>
```

DLZ-DB Starter 将 Spring JDBC 声明为 `provided`，因此应用需要显式引入 `spring-boot-starter-jdbc`。配置标准 `spring.datasource` 并加入数据库驱动后即可使用。完整步骤见 [Spring Boot 快速开始](./docs/2.快速开始/2.1-SpringBoot快速开始.md)。

### Solon

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-solon-plugin</artifactId>
    <version>8.0.0</version>
</dependency>
```

Solon Plugin 不会创建数据源；应用还需引入 HikariCP 等 `DataSource` 实现和数据库驱动。使用 `@Tran` 时还要显式引入 `org.noear:solon-data`。完整配置见 [Solon 快速开始](./docs/2.快速开始/2.2-Solon快速开始.md)。

### 直接 CRUD 与 Wrapper

```java
// 直接 CRUD：调用时立即执行
User user = DB.pojo.selectById(User.class, id);
DB.pojo.insert(user);
DB.pojo.updateById(user);
DB.pojo.deleteById(User.class, id);

// Wrapper 写操作：以 execute() 终止
DB.pojo.updateWrapper(User.class)
        .set(User::getStatus, 1)
        .eq(User::getId, id)
        .execute();
```

更新和删除前应确保条件来自可信业务逻辑；动态 SQL 的 `${name}` 片段不得接收用户输入。完整安全边界见 [兼容性与限制](./docs/6.参考手册/6.5-兼容性与限制.md)。

## 模块与 JDK

| 模块 | 用途 | 发行目标 |
|---|---|---|
| `dlz-db-core` | 核心 API 与 JDBC 实现 | Java 8 |
| `dlz-db-spring-boot-starter` | Spring Boot 自动配置 | Java 8 |
| `dlz-db-solon-plugin` | Solon 集成 | Java 8 |
| `dlz-db-web-demos` | Spring Boot 2/3、Solon 示例 | 按示例框架决定 |

三个发行库模块兼容 Java 8。根聚合项目包含 Spring Boot 3 Demo，因此完整构建需要 JDK 17 或更高版本。验证命令和 CI 矩阵见 [TESTING.md](./TESTING.md)。

## 文档

- [中文文档总目录](./docs/README.md)
- [产品介绍](./docs/1.产品介绍/1.1-项目介绍.md)
- [第一个 CRUD](./docs/2.快速开始/2.3-第一个CRUD.md)
- [使用指南](./docs/3.使用指南/3.1-基础CRUD.md)
- [框架集成](./docs/4.框架集成/4.1-SpringBoot完整集成.md)
- [公共 API 参考](./docs/6.参考手册/6.1-公共API.md)
- [版本迁移](./docs/7.版本迁移/7.2-v7升级到v8.md)
- [更新日志](./CHANGELOG.md)

### 给 AI

- [llms.txt](./llms.txt)：快速了解项目全貌、能力和文档路由。
- [dlz-db-速读.md](./docs/5.AI辅助/dlz-db-速读.md)：生成或修改 DLZ-DB 代码时必须读取的唯一编程规范。

## API 稳定性

- 8.0 稳定公共 API 不随意删除、改名、修改签名或收窄可见性。
- 后续功能优先通过新增方法、重载、选项或 SPI 扩展。
- `com.dlz.db.internal` 属于实现细节，不承诺兼容性，业务代码不应导入。
- 发现公开签名泄漏实现类型时，应视为缺陷修复，并提供迁移说明。

## 参与贡献

提交问题或代码前请阅读 [参与贡献](./CONTRIBUTING.md)。项目当前只维护中文文档；有明确英文用户需求后再建立英文版本。

## License

[Apache License 2.0](./LICENSE) © DLZ KIT

**简单的事情简单做，复杂的事情也能简单做。**
