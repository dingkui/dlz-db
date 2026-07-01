# Mapper 层，可能是 Java 后端最该重新审视的一层

> 每个 Java 后端项目里都有 Mapper 层。但如果我问你：Mapper 接口到底解决了什么问题？你大概要想一会儿。这篇文章不是要否定 MyBatis，而是想认真聊一聊——我们是不是把一个历史遗留的间接层，当成了不可质疑的标配。

## 一、先算一笔账：Mapper 层的成本

一个最简单的"根据 ID 查用户"，MyBatis 的标准写法是这样的：

**文件 1：实体类**

```java
public class User {
    private Long id;
    private String name;
    private Integer status;
    // getter/setter...
}
```

**文件 2：Mapper 接口**

```java
@Mapper
public interface UserMapper {
    User selectById(Long id);
}
```

**文件 3：Mapper XML**

```xml
<mapper namespace="com.example.UserMapper">
    <select id="selectById" resultType="User">
        SELECT * FROM user WHERE id = #{id}
    </select>
</mapper>
```

**文件 4：Service 接口**

```java
public interface UserService {
    User getById(Long id);
}
```

**文件 5：Service 实现**

```java
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
```

5 个文件，就为了 `SELECT * FROM user WHERE id = ?`。

其中 `UserMapper` 接口是空的壳子——它不包含任何逻辑，只是一个让 MyBatis 能生成代理对象的标记。`UserServiceImpl` 也是一个壳子——它只是把调用转发给 Mapper。

**真正有业务价值的代码，只有 XML 里那一行 SQL。** 其他全是"为了让框架跑起来"而存在的样板。

你可能说：MyBatis-Plus 不用写 XML 啊。对，但你仍然要写：

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 空的
}
```

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> {
    // 可能也是空的
}
```

接口体是空的，但你不能不写。这是"为框架而存在"的代码，不是"为业务而存在"的代码。

## 二、Mapper 层到底解决了什么问题？

要回答"能不能砍掉"，先得搞清楚它在解决什么。

回到 2010 年前后，MyBatis（当时还叫 iBatis）出现的背景：

1. **JDBC 太啰嗦**：手动管理 Connection、PreparedStatement、ResultSet，一行查询写十行代码。
2. **SQL 要和 Java 分离**：SQL 写在 Java 里是字符串，没有语法高亮，没法 review，DBA 看不懂。
3. **结果映射太烦**：ResultSet 要手动 get 每一列，转成对象。
4. **SQL 复用**：同一段查询逻辑要在多处使用，需要有地方"存"起来。

Mapper 层 + XML 一次性解决了这四个问题。在当时，这是合理的设计。

**但问题是——这四个问题，今天还成立吗？**

## 三、逐个拆解：2010 年的问题，2025 年的解法

### 问题 1：JDBC 太啰嗦

这个 MyBatis 解决得很好，没有争议。但问题是——你不需要为了"简化 JDBC"而引入整个 Mapper 机制。

直接封装一个静态入口就能做到：

```java
User user = DB.Pojo.select(User.class)
    .eq(User::getId, 1L)
    .queryBean();
```

一行代码，Connection/PreparedStatement/ResultSet 全自动管理，结果自动映射成 Bean。没有 Mapper 接口，没有 XML，没有 ServiceImpl 继承。

**JDBC 繁琐的问题，不需要用 Mapper 层来解决。**

### 问题 2：SQL 要和 Java 分离

这是 Mapper 派最有力的论点。但我认为它混淆了两件事：

**应该分离的是"复杂 SQL"，不是"所有 SQL"。**

一个 `where status = 1 and name like '%tom%'` 的条件查询，SQL 逻辑就两行，藏在 Java 代码里完全没问题——它本来就和业务逻辑是一体的。

真正需要分离的是那种三五十行的报表 SQL、五六个表 JOIN 的复杂查询。这种 SQL 确实应该独立管理。

但解决方案不是"所有查询都走 Mapper"，而是"按场景分入口"：

```java
// 简单查询：直接在代码里用 Lambda 链式
List<User> users = DB.Pojo.select(User.class)
    .eq(User::getStatus, 1)
    .like(User::getName, "tom")
    .queryBeanList();

// 复杂查询：SQL 独立管理，支持文件预设和数据库预设
List<ResultMap> list = DB.Sql.select("key.user.findActiveWithDept")
    .addPara("status", 1)
    .queryList();
```

第一种，SQL 逻辑和业务代码在一起，所见即所得。
第二种，复杂 SQL 用 key 引用，存在文件或数据库里，DBA 可以 review。

**关键洞察：复杂 SQL 需要分离，不代表所有 SQL 都要分离。MyBatis 的问题是用同一种机制处理两种场景——简单查询被迫套上 Mapper 的壳。**

### 问题 3：结果映射太烦

MyBatis 用 `resultMap` 解决这个问题，但配置很重。现代做法是反射 + 注解自动映射：

```java
// dlz-db 的方式：自动映射，零配置
User user = DB.Pojo.select(User.class)
    .eq(User::getId, 1L)
    .queryBean();

// 不想定义 Bean？用 ResultMap，支持深度取值
ResultMap row = DB.Jdbc.select("SELECT * FROM user WHERE id = ?", 1).queryOne();
String city = row.getStr("profile.address.city", "未知");
List<Order> orders = row.getList("orders", Order.class);
```

不需要 `resultMap`，不需要 `@Results` 注解，框架自动按字段名映射。不想要 Bean 的话，`ResultMap` 直接当 Map 用，还支持 JSON 深度取值。

**结果映射的问题，不需要 XML 配置来解决。**

### 问题 4：SQL 复用

这是 Mapper 派最后的阵地："同一段查询逻辑，Mapper 方法可以在多处复用，你怎么搞？"

这个问题问得好，答案分三层。

**第一层：条件复用——用 Condition 对象**

```java
// 构建一个可复用的条件
Condition baseCondition = Condition.where()
    .eq("status", 1)
    .gt("age", 18);

// 查询时复用
DB.Pojo.selectW(User.class)
    .where(baseCondition)
    .queryList();

// 更新时复用同一个条件
DB.Pojo.updateW(User.class)
    .set("flag", 1)
    .where(baseCondition)
    .execute();

// 删除时还能复用
DB.Table.delete("user")
    .where(baseCondition)
    .execute();
```

同一个 `Condition` 对象，跨 select/update/delete 复用，零冗余。在 MyBatis 里要做到同样的事，你得写三个 Mapper 方法，即使它们的 WHERE 子句完全一样。

**第二层：SQL 片段复用——用预设 SQL 的 key 引用**

```xml
<!-- 存在文件里，集中管理 -->
<sql sqlId="key.user.baseWhere"><![CDATA[
    WHERE 1=1 [AND status = #{status}] [AND name LIKE #{name}]
]]></sql>

<sql sqlId="key.user.findActive"><![CDATA[
    SELECT * FROM user ${key.user.baseWhere}
]]></sql>
```

```java
// Java 里用 key 引用
DB.Sql.select("key.user.findActive")
    .addPara("status", 1)
    .queryList();
```

`${key.user.baseWhere}` 是片段引用，等价于 MyBatis 的 `<include refid="..."/>`。但更进一步——SQL 还能存在数据库里，运行时修改不用重启：

```java
// 把 SQL 存进数据库
SysSql sql = new SysSql();
sql.setSqlKey("user.findActive");
sql.setSqlValue("SELECT * FROM user WHERE 1=1 [and status = #{status}]");
DB.Pojo.insert(sql);

// 调用方式完全一样
DB.Sql.select("user.findActive").addPara("status", 1).queryList();
```

这是 MyBatis 做不到的。报表系统、运营后台、灰度调优——改一行数据库记录就生效，不用改 XML 重启服务。

**第三层：业务逻辑复用——Service 方法本身就是复用单元**

说到底，Mapper 方法被复用的本质是什么？是"同一段数据库操作逻辑在多处调用"。但这段逻辑完全可以放在 Service 方法里：

```java
@Service
public class UserService {
    public List<User> findActive() {
        return DB.Pojo.select(User.class)
            .eq(User::getStatus, 1)
            .queryBeanList();
    }
}
```

需要复用？`@Autowired UserService` 调方法就行。Mapper 这一层是"为了复用 SQL 而存在的中间层"，而实际上**SQL 复用可以直接放在 Service 方法里，不需要再下沉一层**。

## 四、动态 SQL：Mapper 的最后一张牌

MyBatis 的 `<if>` / `<choose>` / `<foreach>` XML 标签，是很多人离不开 Mapper 的真正原因。

但动态 SQL 真的需要写在 XML 里吗？

### 写法一：boolean 三参条件（替代 `<if test="...">`）

```java
// MyBatis XML
<select id="findUsers">
    SELECT * FROM user WHERE 1=1
    <if test="name != null">AND name LIKE #{name}</if>
    <if test="status != null">AND status = #{status}</if>
    <if test="minAge != null">AND age &gt; #{minAge}</if>
</select>

// 等价的 Java 代码
List<User> users = DB.Pojo.select(User.class)
    .like(name != null, User::getName, name)
    .eq(status != null, User::getStatus, status)
    .gt(minAge != null, User::getAge, minAge)
    .queryBeanList();
```

条件逻辑写在 Java 里，IDE 能跳转、能调试、能单元测试。而 MyBatis 的 `<if test="name != null">` 是字符串表达式，改字段名时编译器不会报错。

### 写法二：lambda 嵌套（替代 `<choose>/<when>`）

```java
// MyBatis XML
<where>
    <choose>
        <when test="type == 'vip'">
            vip = 1
        </when>
        <otherwise>
            (vip = 0 AND score > 100)
        </otherwise>
    </choose>
</where>

// 等价的 Java 代码
DB.Pojo.selectW(User.class)
    .ors(w -> w
        .eq(User::getVip, 1)
        .ands(o -> o
            .eq(User::getVip, 0)
            .gt(User::getScore, 100)
        )
    )
    .queryList();
// WHERE (vip = 1 OR (vip = 0 AND score > 100))
```

嵌套逻辑用 lambda 就地表达，括号自动加。对比 MyBatis 的 `<choose>` + `<when>` + `<otherwise>` 标签堆叠，可读性差距是肉眼可见的。

### 写法三：方括号空值忽略（写在 SQL 里，但更紧凑）

```java
// 复杂 SQL 里偶尔需要条件拼接
DB.Pojo.selectW(User.class)
    .eq(User::getStatus, 1)
    .sql("age > #{minAge} [AND age < #{maxAge}]", params)
    .queryList();
```

`[AND age < #{maxAge}]` 的语义：`maxAge` 缺失时整段被剔除。等价于 `<if test="maxAge != null">AND age &lt; #{maxAge}</if>`，但写在一行里。

**三种写法，三种场景，没有一个需要 XML。**

## 五、不用 Mapper，代码到底长什么样？

说了这么多，来看一个完整的 Service 类长什么样：

```java
@Service
public class UserService {

    // 查询：活跃用户列表
    public List<User> findActive(String keyword) {
        return DB.Pojo.select(User.class)
            .eq(User::getStatus, 1)
            .like(keyword != null, User::getName, keyword)
            .orderByDesc(User::getCreateTime)
            .queryBeanList();
    }

    // 分页：用户列表
    public Page<User> page(int pageNum, int pageSize) {
        return DB.Pojo.select(User.class)
            .page(pageNum, pageSize)
            .orderByDesc(User::getId)
            .queryBeanPage();
    }

    // 新增
    public User create(User user) {
        return DB.Pojo.insert(user);
    }

    // 修改
    public void update(User user) {
        DB.Pojo.updateW(user).eq(User::getId, user.getId()).execute();
    }

    // 删除
    public void delete(Long id) {
        DB.Pojo.deleteW(User.class).eq(User::getId, id).execute();
    }

    // 事务：创建用户并写日志
    @Transactional
    public void createUserWithLog(User user) {
        DB.Pojo.insert(user);
        DB.Pojo.insert(new UserLog(user.getId(), "created"));
    }

    // 复杂联表查询：直接写 SQL
    public List<ResultMap> findUsersWithDept(int status) {
        return DB.Jdbc.select("""
            SELECT u.*, d.name as dept_name
            FROM user u
            LEFT JOIN department d ON u.dept_id = d.id
            WHERE u.status = ?
            """, status).queryList();
    }

    // 跨数据源事务：显式 lambda
    public void crossDbOperation() {
        DB.Tx.run("slave", () -> {
            DB.Pojo.insert(new User("new user"));
            DB.Pojo.insert(new UserLog(1L, "cross-db"));
        });
    }
}
```

整个类：一个文件，没有 Mapper 接口，没有 XML，没有 `ServiceImpl` 继承，没有 `@Autowired UserMapper`。

增删改查、分页排序、动态条件、事务、联表、跨数据源——全部在一个类里搞定。

## 六、什么时候你仍然需要 Mapper？

说了这么多"不需要"，但诚实很重要。有几种场景 Mapper 层依然有价值：

**1. 遗留系统迁移**

已经几千行 XML 的老项目，没必要重写。渐进式引入新写法即可，新功能用新方式，老 XML 保持不动。

**2. DBA 强管控的团队**

有些团队要求所有 SQL 必须 DBA review，XML 是天然的 review 载体。这种情况可以把复杂 SQL 走预设 SQL 文件，简单查询走代码——按场景分，不是一刀切。

**3. 极其复杂的报表 SQL**

那种几百行的报表 SQL，确实适合独立文件管理。但这种 SQL 本来就不该用 Mapper 接口包——它应该是 `DB.Sql.select("key.report.xxx")` 的形式，SQL 存文件或数据库，Java 侧只传参。

**4. 团队习惯**

如果整个团队都习惯 MyBatis，切换有学习成本。这不是技术问题，是管理问题。但至少新项目可以考虑不用 Mapper。

## 七、核心分歧：间接层 vs 直接调用

说到底，Mapper 层的本质是一个**间接层**——业务代码不直接碰 SQL，而是通过"接口定义→XML映射→代理对象→SQL执行"的链路完成操作。

这个间接层在 2010 年是必要的，因为那时候 Java 没有 Lambda、没有方法引用、没有链式 API，直接写 JDBC 确实太痛苦。

但 2025 年的 Java 已经不一样了。Lambda 链式查询让"在代码里写查询"变得类型安全、可重构、可补全。动态 SQL 的条件判断可以用 boolean 三参解决。复杂 SQL 可以用预设 SQL 独立管理。结果映射可以自动完成。

**当直接调用的体验已经不输于间接调用时，间接层的存在就从"必要"变成了"负担"。**

- 想改个字段名，要改 Entity + Mapper XML + Service 三处
- 想看一个查询的完整逻辑，要在 Interface → XML → resultMap 之间跳来跳去
- 出异常了，栈里 15 层代理，看不出错在哪一行业务代码

这些痛点不是 MyBatis 的 bug，是间接层的固有代价。**你为"分离"获得了灵活性，同时也为"分离"付出了维护成本。**

## 写在最后

我写了一个叫 dlz-db 的轻量数据库框架，核心代码不到 7000 行，没有 Mapper 层。这篇文章不是要说服你换框架，而是想让你重新想一个问题：

**你的项目里，Mapper 层是在解决问题，还是在制造问题？**

如果答案是后者，也许该试试不用 Mapper 的写法了。

dlz-db 在 GitHub 上开源：[github.com/dingkui/dlz-db](https://github.com/dingkui/dlz-db)，支持 Spring Boot 和 Solon，Maven 坐标 `top.dlzio:dlz-db-spring-boot-starter`。你可以先用在一个小的 Service 类上试试——不需要迁移整个项目，也不需要删掉现有的 Mapper，只是试试"不用 Mapper 写一个查询"是什么感觉。

也许你会发现：少了一层，世界并没有塌。
