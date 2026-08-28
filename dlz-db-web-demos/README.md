# DLZ-DB 可运行示例

三个独立可运行的 Web 示例，演示 DLZ-DB 在不同框架下的接入方式。均使用 SQLite 零配置，`java -jar` 即可运行，无需外部数据库。

> 本目录是独立 Maven 工程（`top.dlzio:dlz-db-demos`），**不参与 dlz-db 的发布与测试**：不在 `dlz-db` 父 POM 的 `<modules>` 中（根目录构建不会打包它），聚合 POM 已配置 `skipTests=true`，且每个示例都配置了 `maven-deploy-plugin` 跳过（即使单独 `mvn deploy` 也不会上传）。

## 前置条件

- JDK 8+（dlz-db-web-solon3 / dlz-db-web-springboot2 用 JDK 8，dlz-db-web-springboot3 需要 JDK 17）
- 本地 Maven 仓库已安装 `dlz-db` 8.0.0（在 `dlz-db` 目录执行 `mvn install -DskipTests=true`）

## 构建

```bash
# 在 dlz-db-web-demos 目录下一次性构建三个示例
mvn package
```

## 运行与接口

三个示例共用 8080 端口与同一套 REST 接口（`/user`），每次只能运行一个。

| 示例 | 框架 | 启动命令 | 接口地址 |
| ---- | ---- | ---- | ---- |
| dlz-db-web-springboot2 | Spring Boot 2.6.12 | `java -jar dlz-db-web-springboot2/target/dlz-db-web-springboot2-8.0.0.jar` | http://localhost:8080/user |
| dlz-db-web-springboot3 | Spring Boot 3.3.5 | `java -jar dlz-db-web-springboot3/target/dlz-db-web-springboot3-8.0.0.jar` | http://localhost:8080/user |
| dlz-db-web-solon3 | Solon 3.0.6 | `java -jar dlz-db-web-solon3/target/dlz-db-web-solon3-8.0.0.jar` | http://localhost:8080/user |

### 接口一览

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| GET | `/user` | 用户列表（支持 `?name=` 模糊查询） |
| GET | `/user/{id}` | 按 id 查询 |
| GET | `/user/page?pageNum=1&pageSize=10` | 分页查询 |
| POST | `/user` | 新增（body: `{"name":"张三","age":25}`） |
| PUT | `/user/{id}` | 更新（body: `{"name":"李四","age":30}`） |
| DELETE | `/user/{id}` | 删除（逻辑删除，`deleted` 字段置 1） |
| GET | `/user/count` | 统计数量（不含逻辑删除数据） |

### 快速验证

```bash
# 新增
curl -X POST http://localhost:8080/user -H "Content-Type: application/json" -d '{"name":"张三","age":25}'
# 列表
curl http://localhost:8080/user
# 分页
curl "http://localhost:8080/user/page?pageNum=1&pageSize=10"
# 删除（逻辑删除）
curl -X DELETE http://localhost:8080/user/1
```

## 接入方式对比

| 示例 | 接入方式 | 说明 |
| ---- | ---- | ---- |
| dlz-db-web-springboot2 | 自动装配（`META-INF/spring.factories`） | 引入 `dlz-db-spring-boot-starter` 即生效，无需手动配置 |
| dlz-db-web-springboot3 | 自动装配（`AutoConfiguration.imports`） | 同一个 `dlz-db-spring-boot-starter` 统一支持 Boot 2/3，导入即生效 |
| dlz-db-web-solon3 | Solon SPI 插件 | 引入 `dlz-db-solon-plugin`，SPI 自动加载 |

## 配置说明

三个示例共用同一套 `dlz.db` 配置（见各示例 `application.yml` / `app.yml`）：

```yaml
dlz:
  db:
    logic-delete-field: deleted   # 逻辑删除字段
    table-cache-time: -1          # 表结构缓存时间（-1 不过期）
    helper:
      package-name: com.example.demo.entity   # 实体扫描包
      auto-update: true           # 自动建表/加字段（生产建议 false）
    log:
      show-run-sql: true          # 打印执行 SQL
      show-caller: true           # 打印 SQL 调用位置
      show-result: false          # 打印查询结果
```

数据库文件生成在工作目录 `demo.sqlite3`（已在 `.gitignore` 中排除）。
