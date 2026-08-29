# Spring Boot 集成

## 依赖

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>8.0.0</version>
</dependency>
```

其余依赖（spring-boot-starter-web、mysql-connector-j、lombok）按 Spring Boot 标准项目配。

## 配置

无需编写 `DlzDbConfig` 或继承框架配置类。Starter 会自动绑定 `dlz.db.*` 并使用 Spring 容器中的 `DataSource` 初始化。

### application.yml

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/{database}?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456

dlz:
  db:
    logic-delete-field: deleted
    log:
      show-run-sql: true
      show-caller: true
```

## 事务

用 `@Transactional`（Spring 标准）。

## 其余全部參照 `web/` 目录
