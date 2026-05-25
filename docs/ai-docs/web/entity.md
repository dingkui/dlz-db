# Entity 规范

## 注解

所有 Entity 必须放在 `entity` 包下。DLZ-DB 有三个注解：

| 注解 | 作用 | 示例 |
|------|------|------|
| `@TableName` | 自定义表名 | `@TableName("t_user")` |
| `@TableId` | 标记主键字段 | `@TableId` |
| `@TableField` | 自定义列名 | `@TableField("email_address")` |

## 命名映射（自动）

| Java | 数据库 |
|------|--------|
| 类名 `User.java` | 表 `user` |
| 类名 `OrderItem.java` | 表 `order_item` |
| getter `getUserName()` | 列 `user_name` |
| getter `getRealName()` | 列 `real_name` |

> 不写 `@TableName` 和 `@TableField` 时自动转换。

## 字段注释

每个字段加 `@ApiModelProperty(value = "xxx")`（Swagger），描述字段含义。

## 模板

```java
package com.example.{package}.entity;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

@Data
@TableName("t_user")  // 可选，不写自动转
public class User {

    @TableId
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "状态 1=启用 0=禁用")
    private Integer status;

    @TableField("real_name")  // 可选
    @ApiModelProperty(value = "真实姓名")
    private String realName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
```

## 逻辑删除

Entity 里只要有 `isDeleted` 字段（`Boolean` 或 `Integer`），DLZ-DB 自动启用逻辑删除：

- 查询自动加 `WHERE is_deleted = 0`
- DELETE 语句自动转换为 `UPDATE SET is_deleted = 1`
- 需要物理删除时加 `.ignoreLogicDelete(true)`
