# 6.1 从 MyBatis / MyBatis-Plus 迁移

> 从 MyBatis / MyBatis-Plus / JPA 平滑迁移到 DLZ-DB

## 迁移收益

| 收益项 | 具体表现 |
|--------|---------|
| 代码量减少 | 减少 60% 的文件和代码 |
| 启动速度 | 提升 3-5 倍 |
| 开发效率 | 无需 Mapper/XML |
| SQL 定位 | 日志直接显示代码位置 |

## 迁移策略：渐进式

DLZ-DB 可与 MyBatis/JPA 共存，支持逐步迁移。

```java
@Configuration
public class DataConfig {
    // MyBatis 配置保留
    @Bean
    public SqlSessionFactory sqlSessionFactory() { ... }

    // 新功能用 DLZ-DB
    @Bean
    public DlzDbConfigs dlzDbConfig() { ... }
}
```

## 依赖替换

**移除旧依赖，添加 DLZ-DB：**

```xml
<dependency>
    <groupId>top.dlzio</groupId>
    <artifactId>dlz-db-spring-boot-starter</artifactId>
    <version>7.0.1-3</version>
</dependency>
```

## 实体类兼容

MyBatis-Plus 的 `@TableName` 注解可直接沿用：

```java
// MyBatis-Plus → DLZ-DB（实体类无需修改）
@TableName("sys_user")
public class User {
    private Long id;
    private String name;
    private Integer deleted;  // 自动识别逻辑删除
}
```

## 查询迁移对照

| MyBatis-Plus | DLZ-DB                                                  |
|-------------|---------------------------------------------------------|
| `userMapper.selectById(1L)` | `DB.Pojo.selectById(User.class, 1L)`                    |
| `userMapper.selectList(wrapper)` | `DB.Pojo.select(User.class).eq(...).queryBeanList()`    |
| `userMapper.selectPage(page, wrapper)` | `DB.Pojo.select(User.class).page(1,10).eq(...).queryBeanPage()` |
| `userMapper.insert(user)` | `DB.Pojo.insert(user)`                                  |
| `userMapper.updateById(user)` | `DB.Pojo.updateById(user)`                              |
| `userMapper.deleteById(1L)` | `DB.Pojo.deleteById(User.class,1L)`           |

## 配置迁移

```yaml
# MyBatis-Plus 配置（移除）
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  # ...

# DLZ-DB 配置（添加）
dlz:
  db:
    logic-delete-field: deleted
    log:
      show-run-sql: true
      show-caller: true
```

## Service 层迁移

```java
// MyBatis-Plus
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> {
    public List<User> findActive(String keyword) {
        return this.list(new LambdaQueryWrapper<User>()
            .eq(User::getStatus, 1)
            .like(StringUtils.isNotBlank(keyword), User::getName, keyword));
    }
}

// DLZ-DB（无需继承，无需注入）
@Service
public class UserService {
    public List<User> findActive(String keyword) {
        return DB.Pojo.select(User.class)
            .eq(User::getStatus, 1)
            .like(StringUtil.isNotBlank(keyword), User::getName, keyword)
            .queryBeanList();
    }
}
```

## 迁移检查清单

- [ ] 添加 DLZ-DB 依赖
- [ ] 配置数据源和 DLZ-DB
- [ ] 迁移实体类（通常无需修改）
- [ ] 迁移简单 CRUD 操作
- [ ] 迁移复杂查询
- [ ] 删除旧的 Mapper/Repository
- [ ] 运行测试验证
