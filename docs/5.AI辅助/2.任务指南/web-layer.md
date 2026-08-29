# Web 分层任务指南

> 这是可选的应用架构建议，不是 DLZ-DB API 规则。公共数据库编程规则以 [DLZ-DB AI 编程契约](../dlz-db-速读.md) 为准。

## 如何决定是否需要 Service

演示、原型或没有业务规则的单表只读接口，可以在 Controller 中直接调用 `DB.pojo`。出现以下任一情况时，应使用 Service：

- 多表写入或事务。
- 权限、状态机、库存、金额等业务规则。
- 查询或写入逻辑会被多个入口复用。
- 需要领域异常、审计、缓存或外部服务编排。
- 需要独立单元测试。

“无需 Mapper/DAO”不等于“无需 Service”。DLZ-DB 只减少数据库访问样板，不替应用决定业务边界。

## 推荐职责

| 层 | 职责 |
|---|---|
| Controller | 协议适配、参数校验、权限入口、响应转换 |
| Service | 业务规则、事务边界、跨表编排 |
| Entity / DTO | 持久化模型与接口模型按项目需要分离 |
| DLZ-DB | 数据访问，不要求额外 Mapper |

Entity 的包名、Lombok、OpenAPI/Swagger 注解和统一响应类型由项目规范决定，不是 DLZ-DB 强制约定。

## Spring Boot 示例

~~~java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order create(@RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }
}

@Service
public class OrderService {
    @Transactional
    public Order create(CreateOrderRequest request) {
        Order order = toOrder(request);
        DB.pojo.insert(order);

        for (OrderItem item : toItems(request)) {
            item.setOrderId(order.getId());
            DB.pojo.insert(item);
        }
        return order;
    }
}
~~~

Solon 项目应将 Controller、注入和事务注解替换为 [Solon 差异](../1.框架差异/solon.md) 中的对应形式，不要照搬 Spring 注解。

## 生成代码时

- 先遵守项目已有分层，不强行新增或删除 Service。
- Controller 接收外部输入后先校验，再传入查询条件或写操作。
- UPDATE/DELETE 的主键和业务条件必须显式验证。
- 多表写入将事务放在 Service 边界。
- API DTO 不必直接复用数据库 Entity，尤其是外部写接口。
- 不在 Controller 中暴露动态表名、列名、排序或 SQL 片段。
