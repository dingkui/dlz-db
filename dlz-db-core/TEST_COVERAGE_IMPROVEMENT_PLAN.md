# dlz-db-core 测试覆盖率提升方案

**目标**: 提高 `com.dlz.db` 核心包的测试覆盖率  
**当前状态**: 605个测试用例，106个类已分析  
**期望目标**: 核心包覆盖率达到 80%+

---

## 📊 当前测试覆盖情况分析

### 已有测试的包
✅ **cases/convertor/** - 转换器测试  
✅ **cases/core/** - 核心功能测试  
✅ **cases/ds/** - 数据源测试  
✅ **cases/enums/** - 枚举测试  
✅ **cases/exception/** - 异常测试  
✅ **cases/inf/** - 接口测试  
✅ **cases/modal/** - 模态对象测试  
✅ **cases/util/** - 工具类测试  

### 缺少测试的包
❌ **service/** - 服务层接口（7个接口）  
❌ **annotation/** - 注解定义（6个注解）  
❌ **support/** - 支持类（7个类，部分可能有测试）  

---

## 🎯 优先级排序

### 🔴 高优先级（核心业务逻辑）

#### 1. Service 层接口测试
**位置**: `com.dlz.db.service`  
**文件列表**:
- `IDbBaseService.java` - 基础服务接口
- `IDbJdbcService.java` - JDBC服务接口
- `IDbExecuteService.java` - 执行服务接口
- `IDbOneService.java` - 单条查询服务
- `IDbListService.java` - 列表查询服务
- `IDbColumnService.java` - 列查询服务
- `ICommService.java` - 通用服务接口
- `impl/CommServiceImpl.java` - 通用服务实现

**建议测试数**: 每个接口 10-15 个测试  
**预计新增**: ~100 个测试用例

**测试重点**:
```java
// IDbBaseService 测试
- doDb() 正常执行
- doDb() 异常处理
- doCnt() 计数操作
- 事务上下文传递

// IDbExecuteService 测试
- insertWithAutoKey() 插入并返回自增ID
- update() 更新操作
- delete() 删除操作
- insertBatch() 批量插入
- ASSIGN_ID 预生成测试

// IDbOneService 测试
- getMap() 单条Map查询
- getBean() 单条Bean查询
- 空结果处理
- 异常抛出控制

// IDbListService 测试
- getMapList() 列表查询
- getBeanList() Bean列表查询
- getPage() 分页查询
- getCnt() 计数查询

// IDbColumnService 测试
- getStr() 字符串列查询
- getInt() 整数列查询
- getLong() 长整型列查询
- getColumnList() 列列表查询
```

---

### 🟡 中优先级（支撑功能）

#### 2. Annotation 注解测试
**位置**: `com.dlz.db.annotation`  
**文件列表**:
- `@TableName` - 表名注解
- `@TableId` - 主键注解
- `@TableField` - 字段注解
- `@IdType` - ID类型枚举

**建议测试数**: 每个注解 5-8 个测试  
**预计新增**: ~30 个测试用例

**测试重点**:
```java
// @TableName 测试
- 注解值读取
- 默认表名推导
- 特殊字符处理

// @TableId 测试
- 主键类型识别
- AUTO/ASSIGN_ID/ASSIGN_UUID
- 组合注解使用

// @TableField 测试
- 字段映射
- 忽略字段
- 自定义列名
```

#### 3. Support 支持类测试
**位置**: `com.dlz.db.support`  
**文件列表**:
- `PojoCache.java` - POJO缓存（已有部分测试）
- `DBHolder.java` - 数据库持有者
- `SqlRunThreadHolder.java` - SQL运行线程持有者
- `SegmentIdGenerator.java` - 号段ID生成器（已有测试）
- `bean/IdInfo.java` - ID信息类
- `bean/TableInfo.java` - 表信息类

**建议测试数**: 每个类 8-12 个测试  
**预计新增**: ~50 个测试用例

**测试重点**:
```java
// DBHolder 测试
- 多数据源切换
- 默认数据源获取
- 线程安全测试

// SqlRunThreadHolder 测试
- 线程局部变量设置
- 清理机制
- 并发场景

// IdInfo/TableInfo 测试
- 属性读写
- 序列化
- 边界值处理
```

---

### 🟢 低优先级（边缘功能）

#### 4. Convertor 转换器增强测试
**位置**: `com.dlz.db.convertor`  
**当前状态**: 已有基础测试

**建议补充**:
- 复杂类型转换
- 自定义转换器注册
- 转换失败处理

**预计新增**: ~15 个测试用例

#### 5. Core 核心类增强测试
**位置**: `com.dlz.db.core`  
**当前状态**: 已有基础测试

**建议补充**:
- ISqlExecutor 边界条件
- 批量操作性能
- 大数据量处理

**预计新增**: ~20 个测试用例

---

## 📝 具体实施计划

### 第一阶段：Service 层测试（1-2周）

#### Week 1: 基础服务测试
```
✅ IDbBaseServiceTest (15个测试)
   - doDb正常流程
   - doDb异常处理
   - doCnt计数操作
   - 事务传播测试

✅ IDbExecuteServiceTest (20个测试)
   - 单条插入（AUTO/ASSIGN_ID）
   - 批量插入
   - 更新操作
   - 删除操作
   - ID回填验证
```

#### Week 2: 查询服务测试
```
✅ IDbOneServiceTest (15个测试)
   - 单条Map查询
   - 单条Bean查询
   - 空结果处理
   - 异常控制

✅ IDbListServiceTest (20个测试)
   - 列表查询
   - 分页查询
   - 计数查询
   - 条件构造

✅ IDbColumnServiceTest (15个测试)
   - 各类型列查询
   - 列列表查询
   - 类型转换
```

**阶段目标**: 新增 ~85 个测试用例

---

### 第二阶段：Annotation 和 Support 测试（1周）

#### Week 3: 注解和支持类测试
```
✅ TableNameTest (8个测试)
✅ TableIdTest (8个测试)
✅ TableFieldTest (8个测试)

✅ DBHolderTest (12个测试)
✅ SqlRunThreadHolderTest (10个测试)
✅ IdInfoTest (8个测试)
✅ TableInfoTest (8个测试)
```

**阶段目标**: 新增 ~62 个测试用例

---

### 第三阶段：增强现有测试（1周）

#### Week 4: 补充边界和异常测试
```
✅ Convertor 增强测试 (15个)
✅ Core 增强测试 (20个)
✅ Modal 边界测试 (15个)
✅ Util 异常测试 (10个)
```

**阶段目标**: 新增 ~60 个测试用例

---

## 📊 预期成果

### 测试数量增长
```
当前: 605 个测试用例
目标: 605 + 207 = 812 个测试用例
增长: +34%
```

### 覆盖率提升
```
当前估算: ~60%（基于106个类）
目标: 80%+
提升: +20个百分点
```

### 代码质量指标
- ✅ 核心业务逻辑全覆盖
- ✅ 边界条件充分测试
- ✅ 异常处理完整验证
- ✅ 多线程场景覆盖

---

## 💡 测试编写最佳实践

### 1. 继承 BaseDBTest
```java
public class IDbExecuteServiceTest extends BaseDBTest {
    // 自动获得 Mock 数据库环境
}
```

### 2. 使用 @DisplayName 清晰描述
```java
@Test
@DisplayName("插入操作 - AUTO类型ID应自动回填")
void testInsertWithAutoId() {
    // 测试代码
}
```

### 3. 遵循 AAA 模式
```java
@Test
void testExample() {
    // Arrange - 准备数据
    TestEntity entity = new TestEntity();
    entity.setName("test");
    
    // Act - 执行操作
    Long id = DB.Pojo.insert(entity);
    
    // Assert - 验证结果
    assertNotNull(id);
    assertEquals(id, entity.getId());
}
```

### 4. 参数化测试减少重复
```java
@ParameterizedTest
@ValueSource(strings = {"AUTO", "ASSIGN_ID", "ASSIGN_UUID"})
void testDifferentIdTypes(String idType) {
    // 测试不同ID类型
}
```

### 5. Mock 外部依赖
```java
@Test
void testWithMock() {
    // 使用 BaseDBTest 提供的 Mock 环境
    // 无需真实数据库
}
```

---

## 🔧 工具和资源

### 现有测试参考
- `DbPojoTest.java` - POJO操作测试范例（30个测试）
- `TransactionTest.java` - 事务测试范例
- `SegmentIdGeneratorTest.java` - ID生成器测试范例

### 测试基类
- `BaseDBTest.java` - 提供 Mock 数据库环境
- `BaseMockTest.java` - 基础 Mock 测试

### 实体类
- `entity/` 目录下有16个测试实体类可用

---

## 📈 进度跟踪

### 完成度检查清单

#### 第一阶段：Service 层
- [ ] IDbBaseServiceTest
- [ ] IDbExecuteServiceTest
- [ ] IDbOneServiceTest
- [ ] IDbListServiceTest
- [ ] IDbColumnServiceTest

#### 第二阶段：Annotation & Support
- [ ] TableNameTest
- [ ] TableIdTest
- [ ] TableFieldTest
- [ ] DBHolderTest
- [ ] SqlRunThreadHolderTest
- [ ] IdInfoTest
- [ ] TableInfoTest

#### 第三阶段：增强测试
- [ ] Convertor 增强
- [ ] Core 增强
- [ ] Modal 边界测试
- [ ] Util 异常测试

---

## 🎓 学习资源

### JaCoCo 报告查看
```bash
# 生成报告
mvn clean test jacoco:report

# 打开报告
start target/site/jacoco/index.html
```

### 重点关注
1. **红色区域** - 未覆盖的代码
2. **黄色区域** - 部分覆盖的分支
3. **绿色区域** - 完全覆盖的代码

### 持续改进
- 每次提交前运行测试
- 定期查看覆盖率报告
- 优先覆盖核心业务逻辑
- 逐步提升边缘功能覆盖

---

## 🚀 快速开始

### 创建第一个 Service 测试
```java
package com.dlz.test.db.cases.service;

import com.dlz.test.db.config.BaseDBTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDbExecuteServiceTest extends BaseDBTest {
    
    @Test
    @DisplayName("插入操作 - 应成功插入并返回ID")
    void testInsertSuccess() {
        // TODO: 实现测试
    }
}
```

### 运行单个测试类
```bash
mvn test -Dtest=IDbExecuteServiceTest
```

---

**创建时间**: 2026-05-17  
**版本**: 1.0  
**维护者**: DLZ-DB Team
