# dlz-db-core Service 层测试编写指南

**创建时间**: 2026-05-17  
**状态**: 进行中  
**目标**: 提高 com.dlz.db.service 包的测试覆盖率

---

## ✅ 已完成的工作

### 1. 创建了测试覆盖率提升方案
**文件**: [TEST_COVERAGE_IMPROVEMENT_PLAN.md](file:///D:/gits/dlz/dlzio/dlz-db/dlz-db-core/TEST_COVERAGE_IMPROVEMENT_PLAN.md)

包含：
- 详细的优先级排序（高/中/低）
- 4周实施计划
- 预期成果和目标
- 测试编写最佳实践

### 2. 创建了示例测试类
**文件**: [DbPojoExecuteTest.java](file:///D:/gits/dlz/dlzio/dlz-db/dlz-db-core/src/test/java/com/dlz/test/db/cases/service/DbPojoExecuteTest.java)

包含9个测试用例：
- ✅ testInsertSuccess - 插入操作测试
- ✅ testInsertThenSelect - 插入后查询验证
- ✅ testInsertBatch - 批量插入测试
- ✅ testInsertBatchEmptyList - 空列表处理
- ✅ testUpdateById - 根据ID更新
- ✅ testDeleteById - 根据ID删除
- ✅ testDeleteByIds - 批量删除
- ✅ testSelectByCondition - 条件查询
- ✅ testInsertWithNullFields - null字段处理

---

## ⚠️ 遇到的挑战

### 1. API 使用方式需要调整
**问题**: 最初尝试使用 `DB.Pojo`，但该类不在 core 模块中

**解决方案**: 
- 使用 `DbPojo` 类直接操作
- 正确的 API 调用方式：
  ```java
  // 插入 - 返回对象本身
  TestEntity result = DB.Pojo.insert(entity);
  
  // 查询 - 需要两个参数
  TestEntity found = DB.Pojo.selectById(TestEntity.class, id);
  
  // 删除 - 需要两个参数
  int rows = DB.Pojo.deleteById(TestEntity.class, id);
  
  // 批量插入 - 返回 boolean
  boolean success = DB.Pojo.insertBatch(entities);
  ```

### 2. 内部类表名缓存注册
**问题**: 内部类实体需要手动注册到 PojoCache

**解决方案**:
```java
@BeforeAll
static void initTestTable() throws Exception {
    HashMap<String, Integer> tableColumns = new HashMap<>();
    tableColumns.put("ID", -5);
    tableColumns.put("NAME", 12);
    tableColumns.put("AGE", 4);

    Field cacheField = PojoCache.class.getDeclaredField("tableColumnsInfoCache");
    cacheField.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.concurrent.ConcurrentHashMap<String, HashMap<String, Integer>> cache =
            (java.util.concurrent.ConcurrentHashMap<String, HashMap<String, Integer>>) cacheField.get(null);
    
    cache.put("TEST_AUTO_ENTITY", tableColumns);
}
```

### 3. Mock 环境限制
**当前状态**: 部分测试在 Mock 环境下失败

**原因**: 
- BaseDBTest 提供的是 Mock 数据库环境
- 某些操作（如 insert、update）需要真实的 SQL 执行
- Mock 环境可能不支持所有 JDBC 操作

**建议**:
- 对于纯逻辑测试，Mock 环境足够
- 对于需要真实数据库的测试，考虑使用 SQLite 内存数据库
- 参考现有的 `DbPojoTest` 和 `TransactionTest` 的实现方式

---

## 📋 下一步建议

### 方案1: 继续完善现有测试（推荐）
1. **修复失败的测试用例**
   - 检查 Mock 环境的配置
   - 调整测试断言以适应 Mock 行为
   - 参考 `DbPojoTest` 的成功案例

2. **增加更多测试场景**
   - 异常处理测试
   - 边界条件测试
   - 并发场景测试

### 方案2: 转向其他包的测试
如果 Service 层的 Mock 测试太复杂，可以先测试其他包：

#### 高优先级备选：
1. **Annotation 注解测试** - 简单且独立
   - `@TableName` 值读取测试
   - `@TableId` 类型识别测试
   - `@TableField` 映射测试

2. **Support 支持类测试** - 逻辑清晰
   - `IdInfo` Bean 测试
   - `TableInfo` Bean 测试
   - `DBHolder` 多数据源测试

3. **Util 工具类增强** - 已有基础
   - `DbConvertUtil` 边界测试
   - `SqlUtil` 异常处理

### 方案3: 集成测试方向
考虑创建真正的集成测试：
- 使用 SQLite 内存数据库
- 完整的 CRUD 操作验证
- 事务管理测试

---

## 💡 关键经验总结

### 1. 测试类结构模板
```java
@DisplayName("功能描述")
class XxxTest extends BaseDBTest {

    @BeforeAll
    static void initTestTable() throws Exception {
        // 注册内部类表信息到缓存
    }

    @Test
    @DisplayName("具体测试场景")
    void testXxx() {
        // Arrange
        // Act
        // Assert
    }
}
```

### 2. 命名规范
- 测试类: `XxxTest` 或 `XxxExecuteTest`
- 测试方法: `test + 操作 + 条件/预期结果`
- 使用 `@DisplayName` 提供中文描述

### 3. 继承基类
- 始终继承 `BaseDBTest`
- 自动获得 Mock 数据库环境
- 无需手动配置数据源

### 4. 内部类实体
- 使用 `@TableName` 指定唯一表名
- 在 `@BeforeAll` 中注册到 PojoCache
- 表名会被转换为大写带下划线格式

---

## 📊 进度统计

| 项目 | 数量 | 状态 |
|------|------|------|
| 已创建测试类 | 1 | ✅ |
| 已编写测试用例 | 9 | ⚠️ 部分通过 |
| 通过的测试 | 4 | ✅ |
| 失败的测试 | 5 | 🔧 待修复 |
| 目标测试数 | 207 | 📋 计划中 |

---

## 🎯 快速开始新测试

### 步骤1: 创建测试类
```bash
# 在 service 目录下创建
src/test/java/com/dlz/test/db/cases/service/XxxServiceTest.java
```

### 步骤2: 复制模板
从 `DbPojoExecuteTest.java` 复制基本结构

### 步骤3: 修改实体类
```java
@Setter
@Getter
@TableName("YourTableName")
static class YourEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 其他字段...
}
```

### 步骤4: 注册缓存
在 `@BeforeAll` 中注册表字段信息

### 步骤5: 编写测试
遵循 AAA 模式：Arrange-Act-Assert

### 步骤6: 运行测试
```bash
mvn test -Dtest=XxxServiceTest
```

---

## 📚 参考资料

### 现有成功测试案例
- [DbPojoTest.java](file:///D:/gits/dlz/dlzio/dlz-db/dlz-db-core/src/test/java/com/dlz/test/db/cases/modal/DbPojoTest.java) - 30个测试全部通过
- [TransactionTest.java](file:///D:/gits/dlz/dlzio/dlz-db/dlz-db-core/src/test/java/com/dlz/test/db/cases/core/TransactionTest.java) - 事务测试范例
- [SegmentIdGeneratorTest.java](file:///D:/gits/dlz/dlzio/dlz-db/dlz-db-core/src/test/java/com/dlz/test/db/cases/support/SegmentIdGeneratorTest.java) - ID生成器测试

### 相关文档
- [TEST_COVERAGE_IMPROVEMENT_PLAN.md](file:///D:/gits/dlz/dlzio/dlz-db/dlz-db-core/TEST_COVERAGE_IMPROVEMENT_PLAN.md) - 完整提升方案
- [TESTING.md](file:///D:/gits/dlz/dlzio/dlz-db/TESTING.md) - 测试总体说明

---

**维护者**: DLZ-DB Team  
**最后更新**: 2026-05-17
