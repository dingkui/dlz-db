# Solon 集成

## 依赖

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-solon-plugin</artifactId>
    <version>7.0.1-2</version>
</dependency>
```

其余依赖（solon-web、mysql-connector-j、lombok）按 Solon 标准项目配。

## 配置

### 无需 Config 类

插件通过 SPI 自动注册。

### app.yml

```yaml
datasource:
  default:
    jdbcUrl: jdbc:mysql://localhost:3306/{database}?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driverClassName: com.mysql.cj.jdbc.Driver

dlz:
  db:
    logic-delete-field: deleted
    log:
      show-run-sql: true
      show-caller: true
```

> 多数据源：`datasource.slave.jdbcUrl=...`，自动注册，代码中 `DB.Dynamic.use("slave", () -> ...)` 即可。

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

`DB.xxx` API 与 Spring Boot 完全一致。
