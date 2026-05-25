# DLZ-DB AI 开发文档

> **给 AI 看**：先读本文件，根据需要读取对应目录。

---

## 使用流程

1. 先看 `requirements/README.md` 选需求
2. 再看下面，按你需要的框架选一个入口
3. 写代码时参照 `web/` 下的开发规范
4. **所有 DB 操作必须参照** `AI-Quick-Reference.md`

## 目录

| 目录 | 内容 | 什么时候读 |
|------|------|-----------|
| `web/` | **公共开发规范**（Entity、Controller、Service） | 写任何代码前必读 |
| `spring-boot/` | Spring Boot 集成胶水（依赖、配置、事务） | 用户要 Spring Boot 项目 |
| `solon/` | Solon 集成胶水（依赖、配置、注解映射） | 用户要 Solon 项目 |
| `requirements/` | 10 个需求场景 | 选一个来生成项目 |
| `[AI-Quick-Reference.md](AI-Quick-Reference.md)` | **DLZ-DB 完整 API 与硬约束** | 写 DB 操作时必查 |

## 基本原则（5 秒记住）

1. 没有 Mapper、DAO、XML，直接用 `DB.Pojo.xxx`
2. 简单 CRUD → Controller 里直接写，**不建 Service**
3. 复杂多表 / 可复用逻辑 → 才建 Service
4. 写操作必须 `.execute()` 结尾
5. 事务用 `@Transactional`（Spring）或 `@Tran`（Solon）或 `DB.Tx.run()`（通用）
