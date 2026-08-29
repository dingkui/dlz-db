# dlz-db-core 测试基础设施

当前 core 集成测试使用真实 SQLite 数据源，不再使用旧文档中的 `MockDbProvider`、`MockSqlExecutor` 或 `JdbcQuery`。

## 文件职责

| 文件 | 职责 |
|---|---|
| `BaseDBTest.java` | JUnit 5 集成测试基类，一次性初始化 SQLite 数据源和 DLZ-DB |
| `SqliteDbProviderUtil.java` | 创建测试用 Hikari/SQLite `DataSource` 与 `DlzDbProperties` |
| `TraceExtension.java` | 在 JUnit 5 用例边界清理线程跟踪上下文 |

SQLite 文件默认位于 `./test/testdb-core.sqlite3`。这是本地测试产物，不是应用配置。

## 编写集成测试

```java
class UserQueryTest extends BaseDBTest {
    @Test
    void queryById() {
        User user = new User();
        user.setName("张三");
        DB.pojo.insert(user);

        User actual = DB.pojo.selectById(User.class, user.getId());
        assertNotNull(actual);
        assertEquals("张三", actual.getName());
    }
}
```

- 需要真实 CRUD/事务/方言行为时继承 `BaseDBTest`。
- 纯 SQL 构建、参数校验或工具类测试可以独立运行，不必强制继承。
- 用例应避免依赖执行顺序，并为表名和测试数据保持隔离。

## 运行

```bash
mvn -B -pl dlz-db-core test
```

从根项目验证全部库模块：

```bash
mvn -B -pl dlz-db-core,dlz-db-spring-boot-starter,dlz-db-solon-plugin -am clean verify
```
