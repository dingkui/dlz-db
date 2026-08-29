# DLZ-DB AI 文档加载顺序

> 本文件只负责告诉 AI “什么时候读什么”，不定义 API，也不复制编程规则。

## 默认加载顺序

1. 第一次进入仓库时读取根目录 `llms.txt`，了解版本、模块、能力和边界。
2. 任务涉及生成、修改、审查或迁移 DLZ-DB 代码时，必须完整读取 [dlz-db-速读.md](./dlz-db-速读.md)。
3. 根据任务类型加载必要的少量专题文件；单一任务通常只需 1 个，组合任务可以加载多个对应增量文件。
4. 只有速读和专题文件无法回答时，才读取面向人的教程、参考手册或源码。

## 按任务加载

| 任务 | 必读 | 按需补充 |
|---|---|---|
| 配置 AI 工具如何加载本项目 | `llms.txt` | `5.1-AI工具配置指南.md` |
| 普通 CRUD、查询、分页、批量、事务 | `dlz-db-速读.md` | 无 |
| 新建或修改 Spring Boot 项目 | `dlz-db-速读.md` | `1.框架差异/spring-boot.md` |
| 新建或修改 Solon 项目 | `dlz-db-速读.md` | `1.框架差异/solon.md` |
| 从 MyBatis / MyBatis-Plus 迁移 | `dlz-db-速读.md` | `2.任务指南/migration-mybatis.md` |
| 决定 Controller / Service 分层 | `dlz-db-速读.md` | `2.任务指南/web-layer.md` |
| 使用 JSONMap、JSONList、ResultMap 深度取值 | `dlz-db-速读.md` | `3.按需能力/dlz-kit.md` |
| 开发 DLZ-DB 方言、适配器或插件 | `llms.txt` | `docs/8.维护者文档/` 与源码 |

## 不要这样加载

- 不要同时读取 `llms.txt`、AI 速读、全部教程和全部源码后再开始普通 CRUD。
- 不要把框架差异文档当成公共 API 手册。
- 不要把 Web 分层建议当成 DLZ-DB 的强制架构。
- 不要从旧路径、历史报告或升级计划中推断当前方法名。
- 不要将按需的 DLZ-KIT 文档加入所有数据库任务的默认上下文。

## 事实优先级

发生冲突时按以下顺序处理：

1. 当前版本 Java 源码和 POM。
2. `docs/5.AI辅助/dlz-db-速读.md`。
3. 本目录中的任务或框架专题文件。
4. `docs/6.参考手册/` 和其他面向人的当前教程。
5. 历史迁移记录、测试快照和规划材料。

发现速读与源码不一致时，不要在其他 AI 文档里另写一份规则；应修正速读，并让专题文件继续只描述差异。

## 目录

```text
docs/5.AI辅助/
├── README.md
├── dlz-db-速读.md
├── 5.1-AI工具配置指南.md
├── 1.框架差异/
│   ├── spring-boot.md
│   └── solon.md
├── 2.任务指南/
│   ├── migration-mybatis.md
│   └── web-layer.md
└── 3.按需能力/
    └── dlz-kit.md
```
