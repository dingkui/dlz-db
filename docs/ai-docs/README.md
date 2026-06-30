# DLZ 生态 AI 开发指南

> 🚨 **任何 AI 接手本项目，必须先读完本文件，再开始工作。**
>
> 本文件是项目的"总控提示词"——告诉你做什么、按什么顺序做、去哪里读具体文档。
> 所有技术文档集中在 `docs/ai-docs/` 目录下，**按需读取，不要一次全读。**

---

## 一、核心定位

| 组件 | 一句话 | 坐标 |
|------|--------|------|
| **DLZ-DB** | 轻量 Java ORM（<7000 行），无 Mapper/XML，静态入口 `DB.` + Lambda 链式 API | `top.dlzio:dlz-db-spring-boot-starter:7.1.0` |
| **DLZ-KIT** | JSON 工具库，JSONMap 路径取值 + 自动类型转换 + `@SetValue` 注解映射 | `top.dlzio:dlz-kit:6.6.4` |

**设计哲学**：显式 > 隐式。用 Lambda 链式调用替代注解代理，控制流写在代码里才是可靠的控制流。

---

## 二、开发流程（6 步）

```
Step 1  需求理解
        ↓
Step 2  方案讨论
        ↓
Step 3  架构设计
        ↓
Step 4  工具选型
        ↓
Step 5  编码实现  ← 核心步骤，按路由表读取对应文档
        ↓
Step 6  验证测试
```

### Step 1：需求理解

- **做什么**：明确要开发的功能、输入输出、业务规则
- **输出**：一句话需求描述 + 关键业务规则列表
- **注意**：与用户确认边界条件、异常场景、权限要求

### Step 2：方案讨论

- **做什么**：讨论技术方案的可行性、取舍、风险
- **输出**：选定的技术方案 + 关键决策点
- **注意**：涉及数据库操作的方案，先读 `dlz-db-速读.md` 确认 API 能力边界

### Step 3：架构设计

- **做什么**：设计表结构、模块划分、接口定义、数据流
- **输出**：ER 图 / 表结构 DDL、模块职责、API 接口清单
- **注意**：
  - Entity 命名遵循驼峰自动映射规则（详见 `web/entity.md`）
  - 简单 CRUD 不需要 Service 层，Controller 直接写
  - 涉及 JSON 嵌套数据时，先读 `dlz-kit-速读.md` 了解 JSONMap 能力

### Step 4：工具选型

- **做什么**：确定框架和依赖
- **输出**：框架选择（Spring Boot / Solon）+ pom.xml 依赖清单
- **注意**：根据目标框架读取对应文档

| 框架 | 读取 |
|------|------|
| Spring Boot | `spring-boot/README.md` |
| Solon | `solon/README.md` |

### Step 5：编码实现

- **做什么**：按规范编写 Entity → Controller → Service（如需）
- **输出**：可运行的代码
- **关键**：**开始写代码前，必须先读路由表中对应的规范文件**

### Step 6：验证测试

- **做什么**：运行项目、检查 SQL 日志、验证功能
- **注意**：
  - DLZ-DB 日志会显示调用位置（如 `UserController.java:42`），方便定位
  - 写操作检查 `.execute()` 是否遗漏
  - 分页检查返回的 `total` 和 `record` 是否正确

---

## 三、文档路由表（按需读取）

> 🔴 **AI 自检规则**：写代码前对照此表，确定需要读哪些文件。
> 每个文件都是独立可读的，不需要按顺序通读。

### 🔧 核心 API 文档

| 你要做的事 | 读哪个文件 | 必读？ |
|-----------|-----------|--------|
| **写任何数据库操作** | [`dlz-db-速读.md`](dlz-db-速读.md) | ✅ **必读** |
| **操作 JSON / 嵌套数据 / ResultMap** | [`dlz-kit-速读.md`](dlz-kit-速读.md) | ✅ **必读** |
| 从 MyBatis/MP 迁移 | [`migration.md`](migration.md) | 按需 |

### 📐 编码规范文档

| 你要做的事 | 读哪个文件 | 必读？ |
|-----------|-----------|--------|
| **定义 Entity 实体类** | [`web/entity.md`](web/entity.md) | ✅ 写 Entity 前必读 |
| **写 Controller** | [`web/controller.md`](web/controller.md) | ✅ 写 Controller 前必读 |
| **写 Service** | [`web/service.md`](web/service.md) | 需要 Service 时读 |
| **查看硬约束** | [`web/constraints.md`](web/constraints.md) | 按需查 |

### 🏗️ 项目搭建文档

| 你要做的事 | 读哪个文件 | 必读？ |
|-----------|-----------|--------|
| 搭建 Spring Boot 项目 | [`spring-boot/README.md`](spring-boot/README.md) | ✅ 搭建时必读 |
| 搭建 Solon 项目 | [`solon/README.md`](solon/README.md) | ✅ 搭建时必读 |

---

## 四、基本原则（5 秒速记）

> 这 5 条覆盖 90% 的场景，剩下的查路由表。

1. **无 Mapper/DAO/XML**，直接用 `DB.Pojo.xxx`
2. **简单 CRUD → Controller 里直接写**，不建 Service
3. **复杂多表 / 可复用逻辑 → 才建 Service**
4. **写操作必须 `.execute()` 结尾**（`insert` 除外，它直接执行）
5. **事务**：Spring 用 `@Transactional`，Solon 用 `@Tran`，通用用 `DB.Tx.run()`

---

## 五、AI 自检清单

开始编码前，逐项确认：

- [ ] 我读了 `dlz-db-速读.md`（只要涉及数据库就必须读）
- [ ] 我读了 `dlz-kit-速读.md`（只要涉及 JSON 操作就必须读）
- [ ] 我读了目标框架的搭建文档（如果是搭建新项目）
- [ ] 我读了 `web/entity.md`（如果要定义 Entity）
- [ ] 我读了 `web/controller.md`（如果要写 Controller）
- [ ] 我确认了 10 条硬约束（不确定时查 `web/constraints.md`）

---

## 六、项目目录结构速览

```
docs/ai-docs/
├── README.md              ← 你正在读的文件（总控提示词）
├── dlz-db-速读.md          ← DLZ-DB 完整 API + 硬约束 + 代码模板
├── dlz-kit-速读.md         ← DLZ-KIT JSONMap API + 类型转换 + @SetValue
├── migration.md           ← MyBatis/MP → DLZ-DB 迁移指南
├── web/
│   ├── entity.md          ← Entity 注解 + 命名映射 + 逻辑删除
│   ├── controller.md      ← Controller 规范 + 模板代码
│   ├── service.md         ← Service 规范 + 事务 + 批量 + 多数据源
│   └── constraints.md     ← 10 条硬约束
├── spring-boot/
│   └── README.md          ← Spring Boot 集成（依赖 + 配置 + yml）
└── solon/
    └── README.md          ← Solon 集成（依赖 + 注解差异 + yml）
```
