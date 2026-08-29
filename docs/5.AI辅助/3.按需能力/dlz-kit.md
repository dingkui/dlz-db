# DLZ-KIT 按需能力速读

> 只在任务涉及 `JSONMap`、`JSONList`、`ResultMap` 深度取值或 `@SetValue` 映射时加载。普通 Pojo CRUD 不需要读取本文件。

`dlz-kit` 是 `dlz-db-core` 的非 optional 传递依赖，不需要业务项目为了这些签名再重复声明依赖；“按需”指本文无需为普通 Pojo CRUD 默认加载。DLZ-DB 的 `ResultMap` 继承 `JSONMap`，公共签名还使用 `DlzFn`。本文只补充当前源码所用 DLZ-KIT 的数据访问能力，不重复 DLZ-DB API；数据库编程规则仍以 [DLZ-DB AI 编程契约](../dlz-db-速读.md) 为准。

---

## 是什么

JSONMap 继承 LinkedHashMap，为 JSON 嵌套数据提供路径取值、自动类型转换、链式构建能力。

业务代码可直接导入 DLZ-DB 公共签名涉及的 DLZ-KIT 类型。只有应用明确要锁定或直接管理其版本时才单独声明依赖，并且必须与 `dlz-db-core` POM 的版本保持兼容。

---

## 构造

```java
new JSONMap("{\"name\":\"张三\"}");                      // JSON 字符串
new JSONMap(existingMap);                                  // 从 Map
new JSONMap();                                             // 空对象
new JSONMap("name", "张三", "age", 25);                    // 键值对
new JSONMap(anyObject);                                    // 从 POJO（字段值拷贝）
```

---

## get 方法 — 路径取值

所有 get 方法：路径不存在 → 返回 null 或默认值，不会 NPE。

```java
map.getStr("user.name")              // → String 或 null
map.getInt("user.age")               // → Integer 或 null
map.getInt("user.age", 0)            // → Integer 或 默认值
map.getLong("key")                   // → Long 或 null
map.getDouble("key")                 // → Double 或 null
map.getBoolean("key")                // → Boolean 或 null
map.getBigDecimal("key")             // → BigDecimal 或 null
map.getMap("key")                    // → JSONMap 或 null（子对象）
map.getList("key")                   // → JSONList 或 null
map.getList("key", Integer.class)    // → List<Integer> 或 null
map.getObj("key", User.class)        // → User 或 null（子节点转 Bean）
```

路径语法：
```java
"user.name"                         // 对象属性
"tags[0]"                           // 数组元素
"tags[-1]"                          // 负索引（倒数第一个）
"data.orders[0].id"                 // 混合
```

---

## set / put — 写入

```java
map.set("a.b.c", 1);    // 解析路径，自动创建中间层 → {"a":{"b":{"c":1}}}
map.put("a.b.c", 1);    // 不解析路径，直接作为键名 → {"a.b.c":1}
map.add("tags", "x");   // 追加到数组，自动创建数组
```

`set` 是路径模式，`put` 是 Map 原语。构造嵌套结构用 `set`。

---

## 类型转换规则（ValUtil）

```java
ValUtil.toInt(obj)              // null 返回 null；内容不可转换时抛异常
ValUtil.toInt(obj, defaultValue) // 带默认值
ValUtil.toList("1,2,3", Integer.class)  // → [1,2,3]
```

| 输入 | 目标 | 结果 |
|------|------|------|
| "25" | int | 25 |
| "99.9" | BigDecimal | 99.9 |
| "true" | boolean | true |
| null / 缺失 | 任意 | null 或默认值 |
| "abc" | int | 抛异常（内容不可转换） |

这就是"有界宽容"：缺失/类型宽容，内容严格。

---

## @SetValue 注解 — 扁平 Bean ↔ 嵌套 JSON

```java
public class User {
    private String name;
    @SetValue("ext_info")     // → 存到 ext_info.phone
    private String phone;
    @SetValue("ext_info")
    private String address;
}

// 扁平 Bean → 嵌套 JSON
User user = getUser();
JSONMap target = ConvertUtil.convert(user, JSONMap.class);
// → {"name":"张三","ext_info":{"phone":"138xxx","address":"上海"}}

// 嵌套 JSON → 扁平 Bean
JSONMap source = new JSONMap("{\"name\":\"张三\",\"ext_info\":{\"phone\":\"138xxx\"}}");
User restored = ConvertUtil.convert(source, User.class);
```

`ConvertUtil` 位于 `com.dlz.kit.util.system`，`@SetValue` 位于
`com.dlz.kit.util.system.annotation`。当前 API 没有旧版 `BeanUtil.copyAsSource/copyAsTarget` 方法。

---

## JSONList

```java
JSONList list = new JSONList("[\"a\",\"b\",\"c\"]");
list.getStr(0);     // "a"
list.getStr(-1);    // "c"（负索引）
list.getStr(-2);    // "b"
```

---

## 核心行为总结

1. 路径取值：任意环节为 null，返回 null，不抛异常
2. 类型转换：源类型不重要，目标类型决定
3. 内容不可转换：抛异常（不静默吞掉）
4. set 解析路径，put 不解析
5. 链式构建：`set(...)`、`add(...)` 返回当前 JSONMap；继承的 `put(...)` 返回旧值，不参与链式调用

不要因为查询返回 `ResultMap` 就把所有结果都改成 JSONMap。已有 Entity 或 DTO 时，优先使用 DLZ-DB 的 `queryBean*` 或指定类型查询。
