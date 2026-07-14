# Service 规范

## 什么时候建 Service

✅ **需要建 Service：**
- 操作涉及多张表（如订单 + 订单明细）
- 业务逻辑可复用（多处调用同一个查询/写操作）
- 需要复杂校验或编排

❌ **不需要建 Service：**
- 单表 CRUD（直接在 Controller 写）
- 简单的条件查询
- 一次性的简单操作

## Service 模板

```java
@Service
public class OrderService {

    // 多表操作，加事务
    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        // 1. 插入订单主表
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(req.getUserId());
        order.setTotalAmount(req.getTotalAmount());
        order = DB.pojo.insert(order);

        // 2. 插入订单明细
        for (OrderItem item : req.getItems()) {
            item.setOrderId(order.getId());
            DB.pojo.insert(item);
        }

        return order;
    }

    // 可复用的复杂查询
    public List<Order> findUserOrders(Long userId, Integer status) {
        return DB.pojo.selectWrapper(Order.class)
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime)
                .queryBeanList();
    }

    // 条件更新（如库存扣减）
    @Transactional
    public void deductStock(Long productId, int quantity) {
        // 先查后判
        Product p = DB.pojo.selectWrapper(Product.class)
                .eq(Product::getId, productId)
                .queryBean();
        if (p.getStockQuantity() < quantity) {
            throw new BusinessException("库存不足");
        }
        // 条件更新
        DB.pojo.update(Product.class)
                .set(Product::getStockQuantity, p.getStockQuantity() - quantity)
                .eq(Product::getId, productId)
                .execute();
    }
}
```

## 事务写法

| 场景 | 写法 |
|------|------|
| 单数据源 | `@Transactional` 注解在方法上（Spring） |
| 单数据源 | `@Tran` 注解在方法上（Solon） |
| 编程式事务（跨框架通用） | `DB.Tx.run(() -> { ... })` |
| 事务内多数据源 | `DB.Tx.run("datasourceName", () -> { ... })` |
| 事务有返回值 | `Long id = DB.Tx.run(() -> { insert(); return id; });` |

```java
// 编程式事务（推荐用于多表操作，不依赖框架注解）
DB.Tx.run(() -> {
    DB.pojo.insert(order);
    DB.pojo.insert(orderItem);
});

// 带返回值的编程式事务
Order result = DB.Tx.run(() -> {
    return DB.pojo.insert(order);
});
Long orderId = result.getId();
```

## 批量操作

```java
// 批量插入
List<User> users = loadFromExcel();
DB.Batch.insert(users);

// 条件删除含批量
DB.pojo.deleteWrapper(User.class)
    .in(User::getId, Arrays.asList(1L, 2L, 3L))
    .execute();
```

## 多数据源

```java
// 读走从库
List<User> users = DB.Dynamic.use("slave", () ->
    DB.pojo.selectWrapper(User.class).queryBeanList()
);

// 运行时注册新数据源
DataSourceProperty prop = new DataSourceProperty();
prop.setName("tenant_001");
prop.setUrl("jdbc:mysql://...");
prop.setUsername("root");
prop.setPassword("123456");
DB.Dynamic.setDataSource(prop);
```

## 原生 SQL / 预设 SQL

```java
// 原生 SQL（占位符 ?）
List<ResultMap> rows = DB.Jdbc.select("SELECT * FROM user WHERE id = ?", 1).queryList();

// 预设 SQL（占位符 #{key}，定义在 resources/sql/ 下）
List<User> r = DB.Sql.select("key.user.find").addPara("status", 1).queryList(User.class);

// 标量查询
String name = DB.pojo.selectWrapper(User.class).eq(User::getId, 1).queryStr();
Long count = DB.pojo.selectWrapper(User.class).queryLong();
```
