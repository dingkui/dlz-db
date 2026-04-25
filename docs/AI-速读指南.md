# 🤖 DLZ-DB AI 速读指南

> **极简、无歧义、不出错**

---

## 📌 核心概念

- **DB.Pojo** - 基于 Bean（推荐）
- **DB.Table** - 基于表名
- **DB.Jdbc** - 原生 SQL
- **DB.Sql** - 预设 SQL（key 必须以 `"key."` 开头）
- **DB.Dynamic** - 动态数据源

---

## ⚙️ 配置说明

### Maven 依赖
```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz.db</artifactId>
    <version>6.6.2-SNAPSHOT</version>
</dependency>
```

### 数据源配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_db
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

dlz:
  db:
    # 逻辑删除字段（默认：IS_DELETED）
    logic-delete-field: IS_DELETED
    # 预设 SQL 路径（默认：app/*）
    sqllist: 
      - app/*
      - custom/*
    # 从数据库加载 SQL（默认：false）
    use-db-sql: false
    sql: select sql_key as k, sql_value as s from sys_sql
    # 数据库结构缓存时间（秒，-1 不失效，默认：-1）
    table-cache-time: -1
    # 日志配置
    log:
      show-result: false      # 显示结果日志
      show-run-sql: false     # 显示运行 SQL
      show-caller: false      # 显示调用处
    # 自动更新数据库（生产环境关闭）
    helper:
      auto-update: false      # 自动更新表结构
      package-name: com.dlz   # 扫描包名
```

## 🎯 完整示例（包含所有用法）

```java
// 构建查询
String name = "张三";
Integer minAge = null;
PojoQuery<User> query = DB.Pojo.select(User.class)
    // 基础条件（所有条件方法都支持条件判断和自定义字段名）
    .eq(User::getStatus, 1)                    // Lambda 方式
    .eq(name != null, "name", name)            // ⚠️ 条件判断 + 自定义字段名
    .ne(User::getType, 0)                      // 不等于
    .gt(User::getAge, 18)                      // 大于（ge/lt/le 同理）
    .isn(User::getDeleteTime)                  // IS NULL（isnn 同理）
    .in(User::getId, "1,2,3")                  // IN（支持 List 或 "sql:SELECT..."）
    .ni(User::getName, "admin,root")           // NOT IN
    .bt(minAge != null, "age", minAge, 60)     // ⚠️ 条件判断：minAge 为 null 时不添加此条件
    .lk(User::getName, "张")                   // LIKE '%张%'（ll/lr/nl 同理）
    .or(o -> o.lk(User::getName, "key").lk(User::getAddress, "key"))  // OR 嵌套
    .and(a -> a.gt(User::getAge, 18).lt(User::getAge, 60))            // AND 嵌套
    .apply("EXISTS (SELECT 1 FROM vip WHERE user_id = t.id AND level >= {0})", 3)  // 自定义 SQL，使用 {0}, {1}
    .sql("[id < #{id}]", new JSONMap("id", 123))  // 自定义 SQL，使用 #{key}，方括号表示空值忽略
    .sql("key.user.condition", params);           // 预设 SQL，key 必须以 "key." 开头

// ⚠️ 返回值类型（容易出错）
List<User> beanList = query.queryBeanList();           // List<User>
List<ResultMap> mapList = query.queryList();           // ⚠️ List<ResultMap>（不是 List<User>）
List<UserDto> dtoList = query.queryList(UserDto.class);// List<UserDto>
User user = query.queryBean();                         // User / null
ResultMap map = query.queryOne();                      // ⚠️ ResultMap / null（不是 User）
Page<User> userPage = query.queryBeanPage();           // Page<User>
Page<ResultMap> mapPage = query.queryPage();           // ⚠️ Page<ResultMap>（不是 Page<User>）
long count = query.count();                            // long
```

---

## ⚠️ 返回值规则（必记）

| 方法 | 返回类型 | 记忆规则 |
|------|---------|---------|
| `queryBean()` | `User / null` | 带 `Bean` → Bean |
| `queryOne()` | `ResultMap / null` | ⚠️ 不带 `Bean` → Map |
| `queryBeanList()` | `List<User>` | 带 `Bean` → Bean |
| `queryList()` | `List<ResultMap>` | ⚠️ 不带 `Bean` → Map |
| `queryList(Class)` | `List<Bean>` | 带 `(Class)` → 指定类型 |
| `queryBeanPage()` | `Page<User>` | 带 `Bean` → Bean |
| `queryPage()` | `Page<ResultMap>` | ⚠️ 不带 `Bean` → Map |
| `queryPage(Class)` | `Page<Bean>` | 带 `(Class)` → 指定类型 |

---

## 🔧 条件方法速查

**⚠️ 所有条件方法都支持：**
1. **条件判断**：`.eq(condition, field, value)` - condition 为 false 时不添加此条件
2. **自定义字段名**：`.eq("field_name", value)` - 使用字符串指定字段名

| 方法 | SQL | 示例 |
|------|-----|------|
| `eq/ne/gt/ge/lt/le` | `= / <> / > / >= / < / <=` | `.eq(User::getId, 1)` 或 `.eq(id != null, "id", id)` |
| `isn/isnn` | `IS NULL / IS NOT NULL` | `.isn(User::getDeleteTime)` |
| `in/ni` | `IN / NOT IN` | `.in(User::getId, "1,2,3")` 或 `List` 或 `"sql:SELECT..."` |
| `bt/nb` | `BETWEEN / NOT BETWEEN` | `.bt(User::getAge, 18, 30)` |
| `lk/ll/lr/nl` | `LIKE '%v%' / 'v%' / '%v' / NOT LIKE` | `.lk(User::getName, "张")` |
| `or/and` | `(... OR ...) / (... AND ...)` | `.or(o -> o.eq(...).eq(...))` |
| `apply` | 自定义 SQL | ⚠️ `.apply("sql {0} {1}", v1, v2)` 使用 `{0}`, `{1}` |
| `sql` | 自定义 SQL | ⚠️ `.sql("sql #{key}", new JSONMap("key", v))` 使用 `#{key}` |

---

## 💾 插入/更新/删除

```java
// 插入
DB.Pojo.insert(user).execute();
Long id = DB.Pojo.insert(user).insertWithAutoKey();  // 返回主键
DB.Batch.insert(users, 100);  // 批量

// 更新
DB.Pojo.update(user).eq(User::getId, id).execute();
DB.Pojo.update(User.class).set(User::getName, "新名字").eq(User::getId, id).execute();
DB.Pojo.update(User.class).setSql("score = score + 10").eq(User::getId, id).execute();  // 原生 SQL 片段更新（col = expr）

// 删除（根据系统配置的逻辑删除字段自动判断）
DB.Pojo.delete(User.class).eq(User::getId, id).execute();  // 自动判断逻辑/物理删除
DB.Pojo.delete(User.class).eq(User::getId, id).setLogicDelete(false).execute();  // 强制物理删除
```

---

## 📄 分页与排序

```java
Page<User> page = DB.Pojo.select(User.class)
    .eq(User::getStatus, 1)
    .setPage(Page.build(1, 10, Order.desc("create_time")))  // 第1页，每页10条，降序
    .queryBeanPage();

List<User> records = page.getRecords();  // 当前页数据
long total = page.getTotal();            // 总记录数
```

---

## 🔑 预设 SQL

```xml
<!-- 配置：不加 "key." 前缀 -->
<sql sqlId="key.user.find"><![CDATA[
    SELECT * FROM user WHERE 1=1
    [AND status = #{status}]
    [AND name LIKE #{name}]
]]></sql>
```

```java
// ⚠️ 使用：必须加 "key." 前缀
List<User> users = DB.Sql.select("key.user.find")
    .addPara("status", 1)
    .addPara("name", null)  // 空值时条件忽略（因为有方括号）
    .queryList(User.class);
```

---

## 🔀 动态数据源

```java
// 动态注册多数据源
DataSourceProperty prop = new DataSourceProperty();
prop.setName("test");
prop.setUrl("jdbc:mysql://localhost:3306/db_test");
prop.setUsername("root");
prop.setPassword("123456");
prop.setMaxPoolSize(10);        // 最大连接数（默认：10）
prop.setMinIdle(2);             // 最小空闲连接（默认：2）
prop.setConnectionTimeout(30000);  // 连接超时（默认：30秒）
DBDynamic.addDataSource(prop);

// 切换数据源
User user = DB.Dynamic.use("test", () -> {
    return DB.Pojo.select(User.class).eq(User::getId, 1).queryBean();
});
```

---

## ⚠️ 常见错误（必读）

### 1. 返回值类型混淆
```java
// ❌ queryList() 返回 List<ResultMap>
List<User> users = query.queryList();

// ✅ 使用 queryBeanList()
List<User> users = query.queryBeanList();
```

### 2. apply 和 sql 参数混淆
```java
// ❌ apply 不能使用 #{key}
.apply("age > #{minAge}", params)

// ✅ apply 使用 {0}, {1}
.apply("age > {0}", 18)

// ❌ sql 不能使用 {0}
.sql("age > {0}", params)

// ✅ sql 使用 #{key}
.sql("age > #{minAge}", new JSONMap("minAge", 18))
```

### 3. 预设 SQL key 错误
```java
// ❌ 忘记 "key." 前缀
DB.Sql.select("user.find")

// ✅ 必须加 "key." 前缀
DB.Sql.select("key.user.find")
```

### 4. IN 查询参数错误
```java
// ❌ 传入单个数字
.in(User::getId, 1)

// ✅ 传入字符串或列表
.in(User::getId, "1,2,3")
.in(User::getId, Arrays.asList(1, 2, 3))
```

### 5. 忘记调用 execute()
```java
// ❌ 没有调用 execute()
DB.Pojo.insert(user);

// ✅ 必须调用 execute()
DB.Pojo.insert(user).execute();
```

---

## 🎯 AI 代码生成规则

### 查询
1. 使用 `DB.Pojo.select(User.class)` 开始
2. 添加条件：所有条件方法都支持条件判断和自定义字段名
   - Lambda：`.eq(User::getId, 1)`
   - 条件判断：`.eq(id != null, "id", id)`
   - 自定义字段名：`.eq("field_name", value)`
3. 返回值：
   - Bean：`queryBean()`, `queryBeanList()`, `queryBeanPage()`
   - Map：`queryOne()`, `queryList()`, `queryPage()`
   - 指定类型：`queryOne(Class)`, `queryList(Class)`, `queryPage(Class)`

### 条件
- 基础：`eq`, `ne`, `gt`, `ge`, `lt`, `le`, `isn`, `isnn`
- 范围：`in("1,2,3")` 或 `in(List)`, `bt(v1, v2)`
- 模糊：`lk`, `ll`, `lr`, `nl`
- 嵌套：`.or(o -> o.eq(...))`, `.and(a -> a.eq(...))`
- 自定义：`apply("sql {0}", v)` 或 `sql("sql #{key}", map)`
- 空值忽略：`[AND field = #{key}]`

### 安全
- 参数化查询：使用 `?` 或 `#{key}`，不拼接
- 预设 SQL：使用时必须加 `"key."` 前缀

---

## 📝 快速记忆

**条件方法**：所有条件都支持条件判断 `.eq(condition, field, value)` 和自定义字段名 `.eq("field_name", value)`

**返回值**：带 `Bean` → Bean，不带 → Map，带 `(Class)` → 指定类型

**自定义 SQL**：`apply` 用 `{0}`, `{1}`，`sql` 用 `#{key}`

**预设 SQL**：配置不加 `key.`，使用必须加 `key.`

**空值忽略**：`[AND field = #{key}]`

---

<div align="center">

**DLZ-DB：类型安全 | 链式操作 | 动态 SQL**

</div>
