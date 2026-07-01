# Spring Boot 集成

## 依赖

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>7.2.0</version>
</dependency>
```

其余依赖（spring-boot-starter-web、mysql-connector-j、lombok）按 Spring Boot 标准项目配。

## 配置

### DlzDbConfig.java（必须）

```java
@Configuration
@EnableConfigurationProperties({SpringDlzDbProperties.class})
public class DlzDbConfig extends SpringDlzDbConfig {
}
```

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
