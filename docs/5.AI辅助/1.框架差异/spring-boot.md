# Spring Boot 差异

> 仅在搭建或修改 Spring Boot 项目时加载。公共 CRUD、条件、返回值和安全规则以 [DLZ-DB AI 编程契约](../dlz-db-速读.md) 为准。

## 依赖

~~~xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>8.0.0</version>
</dependency>
~~~

DLZ-DB Starter 中的 Spring JDBC 和自动配置依赖是 `provided`，不会传递给应用；生成依赖时必须确保项目已有 Spring Boot 自动配置环境，并显式加入 `spring-boot-starter-jdbc` 和目标数据库驱动。Starter 使用 Spring 容器中的 `DataSource` 初始化 DLZ-DB，并绑定 `dlz.db.*`。应用不需要编写 `DlzDbConfig`，也不要生成继承框架配置类的代码。当前自动装配类是 `SpringDlzDbAutoConfiguration`，通常不需要业务代码直接引用。

## 最小配置

~~~yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/demo?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456

dlz:
  db:
    logic-delete-field: deleted
    sqllist:
      - app/*
    log:
      show-run-sql: true
      show-caller: true
~~~

- 默认预设 SQL 位置是 `classpath*:sql/app/*.sql`。
- `show-caller` 将调用位置写入 MDC；只有日志 pattern 包含相应 MDC 字段时才会显示。
- 数据库驱动和 Web 依赖按目标 Spring Boot 版本配置；连接池通常由 `spring-boot-starter-jdbc` 提供的默认方案负责，不由 DLZ-DB Starter 替代。

## 事务

Spring 业务服务可使用标准 `@Transactional`；跨框架代码可使用 `DB.tx.run(...)`。两种方式都只承诺单数据源本地事务，`DB.ds.use(...)` 本身不会开启事务。

~~~java
@Service
public class OrderService {
    @Transactional
    public void create(Order order, OrderItem item) {
        DB.pojo.insert(order);
        item.setOrderId(order.getId());
        DB.pojo.insert(item);
    }
}
~~~

事务方法应通过 Spring 代理调用，避免同类内部调用导致 `@Transactional` 不生效。

## 继续阅读

- 面向人的完整集成说明：[`docs/4.框架集成/4.1-SpringBoot完整集成.md`](../../4.框架集成/4.1-SpringBoot完整集成.md)
- 完整 CRUD 示例：[`docs/2.快速开始/2.5-完整CRUD示例.md`](../../2.快速开始/2.5-完整CRUD示例.md)
