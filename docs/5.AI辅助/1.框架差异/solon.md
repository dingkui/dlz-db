# Solon 差异

> 仅在搭建或修改 Solon 项目时加载。公共 CRUD、条件、返回值和安全规则以 [DLZ-DB AI 编程契约](../dlz-db-速读.md) 为准。

## 依赖

~~~xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-solon-plugin</artifactId>
    <version>8.0.0</version>
</dependency>

<!-- 示例选择 HikariCP；也可以替换为应用已有的 DataSource 实现 -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>4.0.3</version>
</dependency>

<!-- 数据库驱动按实际数据库选择 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.32</version>
    <scope>runtime</scope>
</dependency>

<!-- 使用 @Tran 时必须显式加入；DLZ-DB Plugin 中该依赖是 optional -->
<dependency>
    <groupId>org.noear</groupId>
    <artifactId>solon-data</artifactId>
    <version>3.0.6</version>
</dependency>
~~~

`DlzDbSolonPlugin` 通过 SPI 自动加载，无需手动启动插件。但插件本身不传递 HikariCP、数据库驱动或 optional 的 `solon-data`。它必须从 Solon 容器取得一个 `DataSource` Bean 才能完成初始化；`dlz.db.*` 只配置 DLZ-DB，不会创建数据源。

## DataSource Bean

如果项目没有其他数据源插件，可显式注册：

~~~java
@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/demo");
        config.setUsername("root");
        config.setPassword("123456");
        return new HikariDataSource(config);
    }
}
~~~

## 最小配置

~~~yaml
dlz:
  db:
    logic-delete-field: deleted
    sqllist:
      - app/*
    log:
      show-run-sql: true
      show-caller: true
~~~

容器中的 `DataSource` 会作为默认数据源交给插件。运行期新增命名数据源时，应构造 `DataSourceProperty` 并调用 `DB.ds.setDataSource(...)`，由 DLZ-DB 创建和注册连接池；不要把已有 `DataSource` 传给这个方法。已有 `DataSource` 只能通过 `DB.ds.setDefaultDataSource(...)` 设置为默认源。

## 事务和注解差异

Solon 使用 `@Tran`，不是 Spring 的 `@Transactional`；生成该注解前先确认应用已显式依赖 `org.noear:solon-data`。

| Spring Boot | Solon |
|---|---|
| `@RestController` | `@Controller` |
| `@GetMapping/@PostMapping` | `@Get/@Post` + `@Mapping` |
| `@Autowired` | `@Inject` |
| `@RequestParam` | `@Param` |
| `@PathVariable` | `@Path` |
| `@RequestBody` | `@Body` |
| `@Transactional` | `@Tran` |
| `@Service` | `@Component` |

核心 `DB.pojo/table/jdbc/sql` 调用与 Spring Boot 相同，不要把 Spring Controller 注解原样复制到 Solon。

## 继续阅读

- 面向人的完整集成说明：[`docs/4.框架集成/4.2-Solon完整集成.md`](../../4.框架集成/4.2-Solon完整集成.md)
