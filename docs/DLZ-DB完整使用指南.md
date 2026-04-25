# DLZ-DB 完整使用指南

> 轻量级、类型安全的数据库操作框架

---

## 目录

- [一、核心概念](#一核心概念)
- [二、查询操作](#二查询操作)
- [三、条件构造器](#三条件构造器)
- [四、插入操作](#四插入操作)
- [五、更新操作](#五更新操作)
- [六、删除操作](#六删除操作)
- [七、分页与排序](#七分页与排序)
- [八、预设 SQL](#八预设-sql)
- [九、动态数据源](#九动态数据源)
- [十、最佳实践](#十最佳实践)

---

## 一、核心概念

### 1.1 操作方式对比

| 方式 | 适用场景 | 类型安全 | 灵活性 |
|------|---------|---------|-------|
| **DB.Pojo** | 基于 Bean 的 CRUD | ✅ 高 | 中 |
| **DB.Table** | 动态表名、动态字段 | ❌ 低 | 高 |
| **DB.Jdbc** | 原生 SQL、复杂查询 | ❌ 低 | 最高 |
| **DB.Sql** | 预设 SQL、可在线编辑 | ❌ 低 | 高 |
| **DB.Dynamic** | 多数据源切换 | - | - |

### 1.2 核心特性

- **类型安全**：Lambda 表达式避免字段名拼写错误
- **链式操作**：流畅的 API 设计
- **条件判断**：所有条件方法都支持条件判断
- **自定义字段名**：支持字符串指定字段名
- **逻辑删除**：自动判断逻辑删除/物理删除
- **空值安全**：自动处理 null 值

---

## 二、查询操作

### 2.1 返回值类型

**记忆规则**：带 `Bean` → Bean，不带 → Map，带 `(Class)` → 指定类型

#### 单条记录

```java
User user = DB.Pojo.select(User.class).eq(User::getId, 1).queryBean();           // User / null
ResultMap map = DB.Pojo.select(User.class).eq(User::getId, 1).queryOne();        // ResultMap / null
UserDto dto = DB.Pojo.select(User.class).eq(User::getId, 1).queryOne(UserDto.class);  // UserDto / null
```

#### 列表记录

```java
List<User> users = DB.Pojo.select(User.class).queryBeanList();        // List<User>
List<ResultMap> maps = DB.Pojo.select(User.class).queryList();        // List<ResultMap>
List<UserDto> dtos = DB.Pojo.select(User.class).queryList(UserDto.class);  // List<UserDto>
```

#### 分页记录

```java
Page<User> page = DB.Pojo.select(User.class)
    .setPage(Page.build(1, 10))
    .queryBeanPage();

List<User> records = page.getRecords();  // 当前页数据
long total = page.getTotal();            // 总记录数
int pages = page.getPages();             // 总页数
```

#### 其他返回值

```java
long count = DB.Pojo.select(User.class).count();
String name = DB.Pojo.select(User.class).select(User::getName).queryStr();
List<String> names = DB.Pojo.select(User.class).select(User::getName).queryStrList();
```

### 2.2 DB.Pojo 查询（推荐）

```java
// 基础查询
User user = DB.Pojo.select(User.class).eq(User::getId, 1).queryBean();

// 多条件查询
List<User> users = DB.Pojo.select(User.class)
    .eq(User::getStatus, 1)
    .gt(User::getAge, 18)
    .lk(User::getName, "张")
    .queryBeanList();

// 指定字段查询
List<User> users = DB.Pojo.select(User.class)
    .select(User::getId, User::getName, User::getAge)
    .eq(User::getStatus, 1)
    .queryBeanList();
```

### 2.3 DB.Table 查询

```java
// 返回 Map
ResultMap result = DB.Table.query("sys_user").eq("status", 1).queryOne();
String name = result.getStr("name");
String city = result.getStr("profile.city");  // 深层路径

// 返回 Bean
User user = DB.Table.query("sys_user").eq("id", 1).queryOne(User.class);

// 动态表名
String tableName = "user_" + year;
List<ResultMap> list = DB.Table.query(tableName).eq("status", 1).queryList();
```

### 2.4 DB.Jdbc 查询

```java
// 使用 ? 占位符
List<ResultMap> list = DB.Jdbc.select(
    "SELECT * FROM user WHERE status = ? AND age > ?", 1, 18
).queryList();

// 复杂查询
String sql = """
    SELECT u.*, d.name as dept_name
    FROM user u
    LEFT JOIN department d ON u.dept_id = d.id
    WHERE u.status = ? AND d.type = ?
    """;
List<ResultMap> list = DB.Jdbc.select(sql, 1, "tech").queryList();
```

### 2.5 DB.Sql 查询

```java
// 使用预设 SQL（key 必须以 "key." 开头）
List<User> users = DB.Sql.select("key.user.findByCondition")
    .addPara("status", 1)
    .addPara("name", "张三")
    .queryList(User.class);

// 分页查询
Page<User> page = DB.Sql.select("key.user.findByCondition")
    .addPara("status", 1)
    .setPage(Page.build(1, 10))
    .page(User.class);
```

---

## 三、条件构造器

### 3.1 条件方法概览

所有条件方法都支持三种用法：
1. **Lambda 方式**：`.eq(User::getId, 1)` - 类型安全
2. **条件判断**：`.eq(id != null, "id", id)` - 条件为 false 时不添加
3. **自定义字段名**：`.eq("field_name", value)` - 使用字符串指定字段名

### 3.2 基础比较条件

```java
DB.Pojo.select(User.class)
    .eq(User::getStatus, 1)           // 等于：status = 1
    .ne(User::getType, 0)             // 不等于：type <> 0
    .gt(User::getAge, 18)             // 大于：age > 18
    .ge(User::getScore, 60)           // 大于等于：score >= 60
    .lt(User::getLevel, 10)           // 小于：level < 10
    .le(User::getRetryCount, 3)       // 小于等于：retry_count <= 3
    .queryBeanList();
```

### 3.3 空值检查

```java
DB.Pojo.select(User.class)
    .isn(User::getDeleteTime)         // IS NULL
    .isnn(User::getEmail)             // IS NOT NULL
    .queryBeanList();
```

### 3.4 IN 查询

```java
// 逗号分隔字符串
DB.Pojo.select(User.class).in(User::getId, "1,2,3,4,5").queryBeanList();
// 生成 SQL：id IN (1,2,3,4,5)

// List 或数组
DB.Pojo.select(User.class).in(User::getId, Arrays.asList(1, 2, 3)).queryBeanList();

// 子查询
DB.Pojo.select(User.class)
    .in(User::getDeptId, "sql:SELECT id FROM dept WHERE status = 1")
    .queryBeanList();

// NOT IN
DB.Pojo.select(User.class).ni(User::getName, "admin,root").queryBeanList();
```

### 3.5 BETWEEN 查询

```java
// 两个参数
DB.Pojo.select(User.class).bt(User::getAge, 18, 30).queryBeanList();

// 逗号分隔字符串
DB.Pojo.select(User.class).bt(User::getAge, "18,30").queryBeanList();

// List 或数组
DB.Pojo.select(User.class).bt(User::getAge, Arrays.asList(18, 30)).queryBeanList();

// NOT BETWEEN
DB.Pojo.select(User.class).nb(User::getScore, 0, 60).queryBeanList();
```

### 3.6 模糊查询

```java
DB.Pojo.select(User.class)
    .lk(User::getName, "张")          // LIKE '%张%' - 模糊匹配
    .ll(User::getPhone, "138")        // LIKE '138%' - 左模糊
    .lr(User::getAddress, "北京")      // LIKE '%北京' - 右模糊
    .nl(User::getDescription, "测试")  // NOT LIKE '%测试%' - 非模糊
    .queryBeanList();
```

### 3.7 逻辑嵌套

```java
// OR 嵌套（内部用 OR 连接）
DB.Pojo.select(User.class)
    .or(o -> o.eq(User::getCity, "北京").eq(User::getCity, "上海"))
    .queryBeanList();
// 生成 SQL：WHERE (city = '北京' OR city = '上海')

// AND 嵌套（内部用 AND 连接）
DB.Pojo.select(User.class)
    .and(a -> a.gt(User::getAge, 18).lt(User::getAge, 60))
    .queryBeanList();
// 生成 SQL：WHERE (age > 18 AND age < 60)

// 混合嵌套
DB.Pojo.select(User.class)
    .eq(User::getStatus, 1)
    .or(o -> o
        .eq(User::getVip, 1)
        .and(a -> a.eq(User::getVip, 0).gt(User::getScore, 100))
    )
    .queryBeanList();
// 生成 SQL：WHERE status = 1 AND (vip = 1 OR (vip = 0 AND score > 100))
```

### 3.8 条件判断

```java
String name = "张三";
Integer minAge = null;
Integer maxAge = 60;

DB.Pojo.select(User.class)
    .eq(name != null, "name", name)            // name 不为 null，添加条件
    .gt(minAge != null, "age", minAge)         // minAge 为 null，不添加条件
    .lt(maxAge != null, "age", maxAge)         // maxAge 不为 null，添加条件
    .queryBeanList();
// 生成 SQL：WHERE name = '张三' AND age < 60
```

### 3.9 自定义 SQL

#### apply 方法（使用 {0}, {1} 占位符）

```java
DB.Pojo.select(User.class)
    .apply("age > {0} AND age < {1}", 18, 60)
    .queryBeanList();

// 子查询
DB.Pojo.select(User.class)
    .apply("id IN (SELECT user_id FROM orders WHERE amount > {0})", 1000)
    .queryBeanList();

// EXISTS 查询
DB.Pojo.select(User.class)
    .apply("EXISTS (SELECT 1 FROM vip WHERE user_id = t.id AND level >= {0})", 3)
    .queryBeanList();
```

#### sql 方法（使用 #{key} 命名参数）

```java
// 基础用法
JSONMap params = new JSONMap("minAge", 18, "maxAge", 60);
DB.Pojo.select(User.class)
    .sql("age > #{minAge} AND age < #{maxAge}", params)
    .queryBeanList();

// 空值忽略（使用方括号）
DB.Pojo.select(User.class)
    .sql("[id < #{id}]", new JSONMap("id", null))
    .queryBeanList();
// id 为 null，整个条件被忽略

// 预设 SQL（key 必须以 "key." 开头）
DB.Pojo.select(User.class)
    .sql("key.user.customCondition", new JSONMap("param1", "value1"))
    .queryBeanList();
```

---

## 四、插入操作

### 4.1 单条插入

```java
User user = new User();
user.setName("张三");
user.setAge(25);

// 普通插入
DB.Pojo.insert(user).execute();

// 插入并返回自增主键
Long id = DB.Pojo.insert(user).insertWithAutoKey();
```

### 4.2 批量插入

```java
List<User> users = Arrays.asList(
    new User("张三", 25),
    new User("李四", 30)
);

DB.Batch.insert(users);           // 默认批次大小
DB.Batch.insert(users, 100);      // 每批 100 条
```

### 4.3 基于表名插入

```java
// 基础插入
DB.Table.insert("sys_user")
    .set("name", "张三")
    .set("age", 25)
    .execute();

// 返回自增主键
Long id = DB.Table.insert("sys_user")
    .set("name", "张三")
    .insertWithAutoKey();

// 使用 Map 插入
Map<String, Object> data = new HashMap<>();
data.put("name", "张三");
data.put("age", 25);
DB.Table.insert("sys_user").value(data).execute();
```

### 4.4 插入或更新

```java
User user = new User();
user.setId(1L);
user.setName("张三");

DB.Pojo.insertOrUpdate(user).execute();
// 如果 id=1 存在 → UPDATE，不存在 → INSERT
```

---

## 五、更新操作

### 5.1 基于 Bean 更新

```java
User user = new User();
user.setId(1L);
user.setName("李四");

// 根据 ID 更新非空字段
DB.Pojo.update(user).eq(User::getId, user.getId()).execute();
```

### 5.2 更新指定字段

```java
DB.Pojo.update(User.class)
    .set(User::getName, "李四")
    .set(User::getUpdateTime, new Date())
    .eq(User::getId, 1)
    .execute();
```

### 5.3 条件更新

```java
// 将 30 天未登录的用户设为禁用
DB.Pojo.update(User.class)
    .set(User::getStatus, 0)
    .lt(User::getLastLoginTime, DateUtil.addDays(new Date(), -30))
    .execute();
```

### 5.4 表达式更新

```java
// 数值增减（TODO 暂不支持）
DB.Pojo.update(User.class)
    .setSql("score = score + 10")
    .eq(User::getId, 1)
    .execute();
```

### 5.5 安全机制

```java
// 无条件更新会被保护
DB.Pojo.update(User.class).set(User::getStatus, 0).execute();
// 生成 SQL：UPDATE user SET status = 0 WHERE false（不会更新任何数据）
```

---

## 六、删除操作

### 6.1 逻辑删除 vs 物理删除

```java
// 自动判断逻辑删除/物理删除
DB.Pojo.delete(User.class).eq(User::getId, 1).execute();

// 强制物理删除
DB.Pojo.delete(User.class).eq(User::getId, 1).setLogicDelete(false).execute();
```

### 6.2 条件删除

```java
// 删除一年前已禁用的用户
DB.Pojo.delete(User.class)
    .eq(User::getStatus, 0)
    .lt(User::getCreateTime, DateUtil.addDays(new Date(), -365))
    .execute();
```

### 6.3 安全机制

```java
// 无条件删除会被保护
DB.Pojo.delete(User.class).execute();
// 生成 SQL：DELETE FROM user WHERE false（不会删除任何数据）
```

---

## 七、分页与排序

### 7.1 基础分页

```java
Page<User> page = DB.Pojo.select(User.class)
    .eq(User::getStatus, 1)
    .setPage(Page.build(1, 10))  // 第1页，每页10条
    .queryBeanPage();
```

### 7.2 排序

```java
// 单字段排序
Page<User> page = DB.Pojo.select(User.class)
    .setPage(Page.build(1, 10, Order.desc("create_time")))
    .queryBeanPage();

// 多字段排序
Page<User> page = DB.Pojo.select(User.class)
    .setPage(Page.build(1, 10, Order.descs("create_time", "id")))
    .queryBeanPage();

// 混合排序
Page<User> page = DB.Pojo.select(User.class)
    .setPage(Page.build(Order.asc("status"), Order.desc("create_time")))
    .queryBeanPage();
```

### 7.3 不分页排序

```java
List<User> users = DB.Pojo.select(User.class)
    .orderByDesc(User::getCreateTime)
    .orderByAsc(User::getId)
    .queryBeanList();
```

---

## 八、预设 SQL

### 8.1 XML 配置

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<sqlList>
    <!-- 基础查询 -->
    <sql sqlId="key.user.findActive"><![CDATA[
        SELECT * FROM user WHERE 1=1
        [AND status = #{status}]
        [AND name LIKE #{name}]
        [AND age > #{minAge}]
    ]]></sql>
    
    <!-- SQL 嵌套 -->
    <sql sqlId="key.condition.age"><![CDATA[
        age > #{minAge} [AND age < #{maxAge}]
    ]]></sql>
    
    <sql sqlId="key.user.findByAge"><![CDATA[
        SELECT * FROM user WHERE 1=1
        [AND #{key.condition.age}]
    ]]></sql>
</sqlList>
```

**说明**：
- `sqlId` 配置时不需要 `"key."` 前缀
- 方括号 `[...]` 内的条件：参数为空时自动忽略
- 支持 SQL 嵌套：`#{key.xxx}` 引用其他预设 SQL

### 8.2 使用预设 SQL

```java
// 基础查询（使用时必须加 "key." 前缀）
List<User> users = DB.Sql.select("key.user.findActive")
    .addPara("status", 1)
    .addPara("name", "张")      // 有值，条件生效
    .addPara("minAge", null)    // 空值，条件忽略
    .queryList(User.class);

// 分页查询
Page<User> page = DB.Sql.select("key.user.findActive")
    .addPara("status", 1)
    .setPage(Page.build(1, 10))
    .page(User.class);
```

### 8.3 空值处理

```java
// status 有值，name 和 minAge 为空
DB.Sql.select("key.user.find")
    .addPara("status", 1)
    .addPara("name", null)
    .addPara("minAge", null)
    .queryList(User.class);
// 生成 SQL：SELECT * FROM user WHERE 1=1 AND status = 1
```

---

## 九、动态数据源

### 9.1 切换数据源

```java
// 切换到指定数据源执行
User user = DB.Dynamic.use("test", () -> {
    return DB.Pojo.select(User.class).eq(User::getId, 1).queryBean();
});
```

---

## 十、最佳实践

### 10.1 查询优化

```java
// ✅ 只查询需要的字段
List<User> users = DB.Pojo.select(User.class)
    .select(User::getId, User::getName, User::getAge)
    .eq(User::getStatus, 1)
    .queryBeanList();

// ✅ 使用分页
Page<User> page = DB.Pojo.select(User.class)
    .setPage(Page.build(1, 20))
    .queryBeanPage();
```

### 10.2 条件构造

```java
// ✅ 使用条件判断
List<User> users = DB.Pojo.select(User.class)
    .lk(name != null, "name", name)
    .gt(minAge != null, "age", minAge)
    .queryBeanList();

// ✅ 复用条件
Condition activeUserCondition = Condition.where()
    .eq("status", 1)
    .eq("is_deleted", 0);

List<User> users = DB.Pojo.select(User.class)
    .where(activeUserCondition)
    .queryBeanList();
```

### 10.3 安全性

```java
// ✅ 使用参数化查询
DB.Jdbc.select("SELECT * FROM user WHERE name = ?", userInput).queryList();

// ✅ 使用 apply
DB.Pojo.select(User.class).apply("name = {0}", userInput).queryBeanList();

// ✅ 使用 sql
DB.Pojo.select(User.class)
    .sql("name = #{name}", new JSONMap("name", userInput))
    .queryBeanList();
```

### 10.4 性能优化

```java
// ✅ 批量插入
DB.Batch.insert(users, 100);

// ✅ 避免 N+1 查询，使用 JOIN
String sql = """
    SELECT u.*, d.name as dept_name
    FROM user u
    LEFT JOIN department d ON u.dept_id = d.id
    """;
List<ResultMap> results = DB.Jdbc.select(sql).queryList();
```

### 10.5 常见错误

```java
// ❌ queryList() 返回 List<ResultMap>
List<User> users = DB.Pojo.select(User.class).queryList();
// ✅ 使用 queryBeanList()
List<User> users = DB.Pojo.select(User.class).queryBeanList();

// ❌ apply 不能使用 #{key}
.apply("age > #{minAge}", params)
// ✅ apply 使用 {0}, {1}
.apply("age > {0}", 18)

// ❌ sql 不能使用 {0}
.sql("age > {0}", params)
// ✅ sql 使用 #{key}
.sql("age > #{minAge}", new JSONMap("minAge", 18))

// ❌ 忘记 "key." 前缀
DB.Sql.select("user.find")
// ✅ 必须加 "key." 前缀
DB.Sql.select("key.user.find")

// ❌ IN 查询传入单个数字
.in(User::getId, 1)
// ✅ 传入字符串或列表
.in(User::getId, "1,2,3")

// ❌ 忘记调用 execute()
DB.Pojo.insert(user);
// ✅ 必须调用 execute()
DB.Pojo.insert(user).execute();
```

---

## 附录：API 速查表

### 查询方法

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `queryBean()` | `Bean / null` | 单条 Bean |
| `queryOne()` | `ResultMap / null` | 单条 Map |
| `queryBeanList()` | `List<Bean>` | Bean 列表 |
| `queryList()` | `List<ResultMap>` | Map 列表 |
| `queryBeanPage()` | `Page<Bean>` | Bean 分页 |
| `queryPage()` | `Page<ResultMap>` | Map 分页 |
| `count()` | `long` | 数量 |

### 条件方法

| 方法 | SQL | 说明 |
|------|-----|------|
| `eq/ne` | `= / <>` | 等于/不等于 |
| `gt/ge/lt/le` | `> / >= / < / <=` | 大于/大于等于/小于/小于等于 |
| `isn/isnn` | `IS NULL / IS NOT NULL` | 空值检查 |
| `in/ni` | `IN / NOT IN` | 在列表中/不在列表中 |
| `bt/nb` | `BETWEEN / NOT BETWEEN` | 范围/非范围 |
| `lk/ll/lr/nl` | `LIKE` | 模糊/左模糊/右模糊/非模糊 |
| `or/and` | `OR / AND` | 逻辑嵌套 |
| `apply` | 自定义 SQL | 使用 `{0}`, `{1}` 占位符 |
| `sql` | 自定义 SQL | 使用 `#{key}` 命名参数 |

---

<div align="center">

**DLZ-DB：让数据库操作变得简单**

</div>
