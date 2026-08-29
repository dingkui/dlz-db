# Controller 规范

## 原则

**简单 CRUD 不建 Service。Controller 里直接写 `DB.xxx`。**

只有以下情况才建 Service：
- 涉及多张表的复杂操作
- 有可复用的业务逻辑
- 需要在多处调用的查询/写操作

## 模板（无 Service 版本）

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    // ===== 查询：直接写 =====
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return DB.pojo.selectWrapper(User.class)
                .eq(User::getId, id)
                .queryBean();
    }

    @GetMapping
    public List<User> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return DB.pojo.selectWrapper(User.class)
                .eq(status != null, User::getStatus, status)
                .like(keyword != null, User::getName, keyword)
                .orderByDesc(User::getCreateTime)
                .queryBeanList();
    }

    @GetMapping("/page")
    public Page<User> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return DB.pojo.selectWrapper(User.class)
                .page(pageNum, pageSize)
                .queryBeanPage();
    }

    // ===== 写操作 =====
    @PostMapping
    @Transactional
    public User create(@RequestBody User user) {
        user.setCreateTime(new Date());
        return DB.pojo.insert(user); // 执行插入；生成的主键会回填到 user
    }

    @PutMapping("/{id}")
    @Transactional
    public void update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        DB.pojo.updateById(user);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        DB.pojo.deleteWrapper(User.class)
                .eq(User::getId, id)
                .execute();
    }
}
```

## 需要 Service 时的 Controller

```java
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;  // 复杂逻辑委托给 Service

    @PostMapping
    public Order create(@RequestBody CreateOrderRequest req) {
        return orderService.createOrder(req);  // 涉及 order + order_item 多表
    }
}
```

## 关键规则

| 规则 | 说明 |
|------|------|
| 条件参数 | 用三参形式 `eq(condition, field, value)`，condition=false 自动跳过 |
| 查询返回 | Bean 用 `queryBean/queryBeanList/queryBeanPage`；ResultMap 用 `queryOne/queryList/queryPage` |
| 插入回填 | `DB.pojo.insert(entity)` 返回实体，自增主键回填到原对象 |
| 更新方式 | 按主键更新用 `updateById()`；部分更新用 `.updateWrapper().set().eq().execute()` |
