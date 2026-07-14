# 从 MyBatis / MyBatis-Plus 迁移到 DLZ-DB

> 本文档帮助从 MyBatis 或 MyBatis-Plus 迁移到 DLZ-DB。核心变化：**删除所有 Mapper 接口和 XML，改用 `DB.pojo` 静态调用。**

---

## 一、概念对照

| MyBatis / MP | DLZ-DB | 说明 |
|-------------|--------|------|
| `@Mapper` 接口 | **删除** | 不需要 Mapper 层 |
| `*Mapper.xml` | **删除** | 不需要 XML 映射文件 |
| `BaseMapper<T>` | `DB.pojo` | 静态方法替代继承 |
| `IService<T>` / `ServiceImpl` | 可选 Service | 简单 CRUD 不需要 Service |
| `QueryWrapper<T>` | 条件构造器（Lambda） | `DB.pojo.selectWrapper(...).eq(...)` |
| `@Select` / `@Update` 注解 | `DB.Jdbc` / `DB.Sql` | 原生 SQL 或预设 SQL |
| `Page<T>`（MP） | `Page<T>`（DLZ-DB） | 分页用法类似 |
| `@TableName` | `@TableName` | **保留**，用法一致 |
| `@TableId` | `@TableId` | **保留**，用法一致 |
| `@TableField` | `@TableField` | **保留**，用法一致 |
| `@TableLogic` | 自动检测 `deleted` 字段 | 无需注解，有字段即启用 |

---

## 二、代码改造对照

### 查询

```java
// ❌ MyBatis-Plus
userMapper.selectById(1);
userMapper.selectList(new QueryWrapper<User>().like("name", "张"));
userMapper.selectPage(page, wrapper);

// ✅ DLZ-DB
DB.pojo.selectWrapper(User.class).eq(User::getId, 1).queryBean();
DB.pojo.selectWrapper(User.class).like(User::getName, "张").queryBeanList();
DB.pojo.selectWrapper(User.class).like(User::getName, "张").page(1, 10).queryBeanPage();
```

### 新增

```java
// ❌ MyBatis-Plus
userMapper.insert(user);

// ✅ DLZ-DB
DB.pojo.insert(user);  // 自动回填主键
```

### 更新

```java
// ❌ MyBatis-Plus
userMapper.updateById(user);
userMapper.update(user, new QueryWrapper<User>().eq("id", id));

// ✅ DLZ-DB
DB.pojo.updateById(user);
DB.pojo.update(User.class).set(User::getName, "新名字").eq(User::getId, id).execute();
```

### 删除

```java
// ❌ MyBatis-Plus
userMapper.deleteById(1);

// ✅ DLZ-DB
DB.pojo.deleteWrapper(User.class).eq(User::getId, 1).execute();
```

---

## 三、删除清单

迁移到 DLZ-DB 后，以下文件/代码**应全部删除**：

- [ ] 所有 `*Mapper.java` 接口文件
- [ ] 所有 `*Mapper.xml` XML 映射文件
- [ ] `extends BaseMapper<T>` 继承
- [ ] `extends IService<T>` / `extends ServiceImpl` 继承（如不需要复用逻辑）
- [ ] `QueryWrapper` / `LambdaQueryWrapper` / `UpdateWrapper` 相关代码
- [ ] MyBatis-Plus 依赖（`mybatis-plus-boot-starter`）

---

## 四、保留清单

以下可直接保留：

- [ ] `@TableName`、`@TableId`、`@TableField` 注解（用法一致）
- [ ] Entity 实体类（字段不变）
- [ ] `deleted` 字段（逻辑删除自动启用）
- [ ] 数据库连接配置（DataSource 相关）

---

## 五、依赖替换

```xml
<!-- 删除 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>

<!-- 替换为 -->
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>7.1.0</version>
</dependency>
```

配置类参考：`spring-boot/README.md`
