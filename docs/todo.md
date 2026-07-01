# dlz-db API 升级待办

> 门面 API 骨架已落地（DB + 8 门面 + Batch Builder + InsertOption）。以下为后续待完成事项。

---

## 一、终结方法统一改名（影响所有 Query/Execute Builder）

- [ ] `IExecutorQuery` 接口方法改名：
  - `queryOne()` → `one()`
  - `queryList()` → `list()`
  - `queryPage()` → `page()`
  - `count()` 保持
  - 新增 `oneOrThrow()`（无结果抛异常）
  - 新增 `exists()`（是否存在）
  - `queryStr/queryLong/queryInt/queryDouble` 及 List 版 → 统一为 `value(Class)` + `valueList(Class)`，或保留旧名加 `value` 别名
- [ ] `PojoQuery` 的 `queryBean/queryBeanList/queryBeanPage` → `one/list/page`（或加别名委托，渐进迁移）
- [ ] 所有 wrapper（TableQuery/JdbcQuery/SqlQuery/PojoQuery）终结方法对齐
- [ ] 策略：先加新名 default 方法委托旧名，旧名标 `@Deprecated`，下个大版本删旧名

## 二、旧类迁移（DB.java 改动后的连锁修复）

- [ ] `DB.Dynamic` / `DB.Tx` / `DB.Pojo` 等旧大写字段的所有引用 → 新小写 `DB.ds` / `DB.tx` / `DB.pojo`
  - 重点：`DBTx.java`（ds 包）内 `DB.Dynamic.getCurrentConfig()`、`DBDynamic.java` 内引用
  - 测试类：`DBTest.java`、`DbPojoTest.java`、`DbTableTest.java`、`DbJdbcTest.java`、`DbSqlTest.java`、`DbBatch*Test.java`
- [ ] 旧 `DBDynamic`（ds 包）标记 `@Deprecated`，能力已由 `DbDs`（modal 包）继承覆盖
- [ ] 旧 `DBTx`（ds 包）标记 `@Deprecated`，能力已由 `DbTx`（modal 包）替代
- [ ] 旧 `DbPojo.insertOrUpdate` / `DbPojo.updateById`(返回 bean 的旧版) 调用方迁移到 `save` / `updateById`(返回 int)

## 三、wrapper 方法名/签名调整

- [ ] `PojoQuery` / `PojoDelete` / `PojoUpdate`：`selectW/deleteW/updateW` 已在门面层改名为 `select/delete/update`，wrapper 内部静态工厂 `wrapper()` 可保留
- [ ] `SqlExecute`：现状 `addParas(Map).execute()`，需加链式 `set(k,v)` 方法，对齐文档 `.set(k,v).execute()`
- [ ] `TableInsert`：确认 `set(k,v)` 链式 + `value(JSONMap)` 都可用
- [ ] `PojoUpdate.set(entity, Function)` 确认 ignoreNull 链式 `.ignoreNull()` 是否需要新增

## 四、方言机制（config.dialect + supports）

- [ ] `SqlHelper` 抽象类加 `boolean supports(String url)` 默认方法（返回 false）
- [ ] 内置方言实现各自声明 `supports`：
  - `DbOpMysql` → `url.contains(":mysql") || url.contains(":mariadb")`
  - `DbOpPostgresql` → `url.contains(":postgresql")`
  - `DbOpDm8` → `url.contains(":oracle") || url.contains(":dm")`
  - `DbOpSqlite` → `url.contains(":sqlite")`
- [ ] `DataSourceConfig.getSqlHelper()` 改造：先遍历 `DB.config.dialects()` 匹配 `supports`，找不到回退内置
- [ ] `DataSourceConfig.getDbType()` 未识别 url 不再抛异常，返回 null/UNKNOWN，让方言 `supports` 接管
- [ ] `DbConfig.dialect/dialects` 实现方言注册表（`CopyOnWriteArrayList`）

## 五、条件复用（Condition）

- [ ] `Condition.of(Class)` / `Condition.of()` 静态工厂（不开 DB 顶级入口）
- [ ] wrapper 加 `.where(Condition)` 接受外部条件：`PojoQuery` / `TableQuery` / `PojoDelete` / `TableDelete` / `PojoUpdate` / `TableUpdate`
- [ ] 确认 `PojoQuery.where()` 导出当前 Condition（现状是否已有）

## 六、模块化（方言拆扩展包）

- [ ] 新建 `dlz-db-dialect-postgresql` / `dlz-db-dialect-oracle` / `dlz-db-dialect-sqlite` 模块
- [ ] 每个方言包放 `META-INF/services/com.dlz.db.support.helper.SqlHelper`
- [ ] core 启动时 `ServiceLoader.load(SqlHelper.class)` 自动注册到 `DB.config`
- [ ] core 留 `DbOpMysql` 作为默认方言（开箱即用）
- [ ] 评估：方言数量少（≤4）可缓拆，靠 `config.dialect()` 注册即可

## 七、insert 选项实现

- [ ] `DbPojo.insert(entity, InsertOption...)` 按 options 处理：
  - `IGNORE_NULL` / `INCLUDE_NULL` 控制 null 字段
  - `ON_DUPLICATE_UPDATE` / `ON_DUPLICATE_IGNORE` 主键冲突策略
- [ ] `PojoInsert` 支持上述选项（可能需扩展）

## 八、事务增强

- [ ] `DbTx.runNew/callNew` 验证 Solon `TranExecutor` 是否支持 REQUIRES_NEW
- [ ] 若不支持，文档标注"依赖底层"，或降级为不支持

## 九、Record / ResultMap 兼容

- [ ] 决定：`ResultMap` 加 `Record` 别名（`public class Record extends ResultMap {}` 或 typealias），保持向后兼容
- [ ] 或 `ResultMap` 直接加 `Record` 作为同义词文档说明
- [ ] 深度取值 `getByPath("orders[0].amount")` 确认 `JSONMap` 是否已支持

## 十、Starter 适配

- [ ] `dlz-db-spring-boot-starter` 适配新 `DB` 入口（8 小写字段）
- [ ] `dlz-db-solon-plugin` 适配
- [ ] starter 内 `DB.config` 自动注册逻辑（逻辑删除插件、SPI 方言）

## 十一、测试与文档

- [ ] 现有测试迁移：`selectW`→`select`、`DB.Pojo`→`DB.pojo`、`insertOrUpdate`→`save` 等
- [ ] 新增测试：`save`（手动主键场景）、`insert(options)`、`deleteByIds(可变参数)`、`batch.insert().size().execute()`
- [ ] `README.md` / `README_EN.md` 更新 API 示例
- [ ] `CONTRIBUTING.md` 写入"5 年稳定性规则"（8 字段冻结、动词冻结、快捷方法准入准则）

---

## 优先级建议

| 优先级 | 事项 | 理由 |
|---|---|---|
| P0 | 二、旧类迁移（修复编译） | DB.java 改动后连锁断裂，先让项目能编译 |
| P0 | 一、终结方法改名 | 影响所有调用方，需尽早统一 |
| P1 | 四、方言 supports 机制 | config.dialect 的依赖项 |
| P1 | 三、SqlExecute 链式 set | DbSql.execute 链式依赖 |
| P2 | 五、Condition 复用 | 低频，可后做 |
| P2 | 六、模块化拆包 | 方言少时可缓 |
| P3 | 七~十一 | 增强/兼容/文档 |
