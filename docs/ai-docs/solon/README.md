# Solon 集成

## 依赖

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-solon-plugin</artifactId>
    <version>8.0.0</version>
</dependency>
```

其余依赖（solon-web、mysql-connector-j、lombok）按 Solon 标准项目配。

## 配置

### 插件无需手动启用，但必须有 DataSource Bean

插件通过 SPI 自动注册，并在 Solon 容器中获取 `DataSource` Bean 后初始化。`dlz.db.*` 只是 DLZ-DB 配置，不会凭空创建数据源。如果项目中没有其他数据源插件，可像下面这样注册：

```java
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
```

### app.yml

```yaml
dlz:
  db:
    logic-delete-field: deleted
    log:
      show-run-sql: true
      show-caller: true
```

> 多数据源需先由应用或数据源插件创建，再通过 `DB.ds.setDataSource(...)` 注册；切换时使用 `DB.ds.use("slave", () -> ...)`。

## 事务

用 `@Tran`（不是 `@Transactional`）。

## 注解差异

| Spring Boot | Solon |
|-------------|-------|
| `@RestController` | `@Controller` |
| `@GetMapping("/x")` | `@Get` + `@Mapping("/x")` |
| `@PostMapping("/x")` | `@Post` + `@Mapping("/x")` |
| `@Autowired` | `@Inject` |
| `@RequestParam` | `@Param` |
| `@PathVariable` | `@Path` |
| `@RequestBody` | `@Body` |
| `@Transactional` | `@Tran` |
| `@Service` | `@Component` |

## 其余全部参照 `web/` 目录

核心 `DB.pojo`/`DB.table`/`DB.jdbc`/`DB.sql` 调用形式与 Spring Boot 一致；数据源注册和事务语义以本章 Solon 说明为准。
