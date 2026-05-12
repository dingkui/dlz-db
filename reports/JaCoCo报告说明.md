# JaCoCo 测试覆盖率报告说明

## 📊 报告类型

JaCoCo 会生成两种类型的报告：

### 1. 模块级报告（Module Reports）

每个模块都有自己的覆盖率报告：

```
dlz-db-core/target/reports/jacoco/index.html
dlz-db-spring-boot-starter/target/reports/jacoco/index.html
dlz-db-solon-plugin/target/reports/jacoco/index.html
```

**特点**：
- 显示单个模块的覆盖率
- 详细到每个类、方法
- 适合模块开发时查看

### 2. 聚合报告（Aggregate Report）⭐ 推荐

整个项目的汇总报告：

```
target/reports/jacoco-aggregate/index.html
```

**特点**：
- 显示所有模块的总体覆盖率
- 包含所有模块的代码
- 适合查看项目整体质量

---

## 🚀 如何生成报告

### 方式 1：使用测试脚本（推荐）

```cmd
cd reports
test-jacoco.bat
```

脚本会自动：
1. 运行 `mvn clean test`（运行测试）
2. 运行 `mvn jacoco:report-aggregate`（生成聚合报告）
3. 询问是否打开报告

### 方式 2：手动运行 Maven 命令

```cmd
# 1. 运行测试
mvn clean test

# 2. 生成聚合报告
mvn jacoco:report-aggregate
```

---

## 📍 报告位置详解

### 聚合报告（优先查看）

**路径**：`target/reports/jacoco-aggregate/index.html`

**何时生成**：
- 运行 `mvn jacoco:report-aggregate`
- 或运行 `mvn verify`（verify 阶段会自动生成）

**内容**：
- 所有模块的总体覆盖率
- 按模块分组的详细报告
- 包含所有包和类

### 模块报告

**路径**：
- `dlz-db-core/target/reports/jacoco/index.html`
- `dlz-db-spring-boot-starter/target/reports/jacoco/index.html`
- `dlz-db-solon-plugin/target/reports/jacoco/index.html`

**何时生成**：
- 运行 `mvn test`（test 阶段会自动生成）
- 或运行 `mvn jacoco:report`

**内容**：
- 单个模块的覆盖率
- 该模块的所有包和类

---

## 🔍 报告查看顺序

### 推荐查看顺序

1. **聚合报告**（`target/reports/jacoco-aggregate/index.html`）
   - 查看整体覆盖率
   - 识别覆盖率低的模块

2. **模块报告**（`模块名/target/reports/jacoco/index.html`）
   - 深入查看具体模块
   - 找到未覆盖的代码

---

## ❓ 常见问题

### Q1: 为什么找不到聚合报告？

**A**: 聚合报告需要单独生成：

```cmd
# 确保先运行测试
mvn clean test

# 然后生成聚合报告
mvn jacoco:report-aggregate
```

或者使用脚本：
```cmd
cd reports
test-jacoco.bat
```

### Q2: 为什么只有模块报告，没有聚合报告？

**A**: 如果只运行 `mvn test`，只会生成模块报告。需要额外运行：

```cmd
mvn jacoco:report-aggregate
```

### Q3: 聚合报告和模块报告有什么区别？

**A**: 
- **聚合报告**：所有模块的汇总，显示项目整体覆盖率
- **模块报告**：单个模块的详细报告

推荐先看聚合报告了解整体情况，再看模块报告深入分析。

### Q4: 报告显示 0% 覆盖率？

**A**: 可能的原因：
1. 测试没有运行成功
2. 测试代码没有覆盖到源代码
3. JaCoCo agent 没有正确配置

解决方法：
```cmd
# 清理并重新运行
mvn clean test
mvn jacoco:report-aggregate
```

### Q5: 如何只生成聚合报告？

**A**: 
```cmd
# 方式 1：使用脚本
cd reports
test-jacoco.bat

# 方式 2：手动运行
mvn clean test
mvn jacoco:report-aggregate
```

---

## 📊 报告内容说明

### 覆盖率指标

JaCoCo 报告包含以下指标：

| 指标 | 说明 | 目标值 |
|------|------|--------|
| **Instructions** | 字节码指令覆盖率 | - |
| **Branches** | 分支覆盖率 | ≥ 60% |
| **Lines** | 代码行覆盖率 | ≥ 70% |
| **Methods** | 方法覆盖率 | - |
| **Classes** | 类覆盖率 | - |

### 颜色说明

- 🟢 **绿色**：已覆盖
- 🟡 **黄色**：部分覆盖
- 🔴 **红色**：未覆盖

---

## 🎯 使用建议

### 日常开发

```cmd
cd reports
test-jacoco.bat
```

查看聚合报告，确保新代码有测试覆盖。

### 提交前检查

```cmd
cd reports
test-jacoco.bat
```

确保覆盖率达标：
- 行覆盖率 ≥ 70%
- 分支覆盖率 ≥ 60%

### 深入分析

1. 先看聚合报告（整体情况）
2. 再看模块报告（具体问题）
3. 点击包名深入到类级别
4. 点击类名查看具体未覆盖的代码行

---

## 🔧 配置说明

### pom.xml 配置

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <!-- 模块报告：test 阶段自动生成 -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        
        <!-- 聚合报告：需要手动运行或在 verify 阶段生成 -->
        <execution>
            <id>report-aggregate</id>
            <phase>verify</phase>
            <goals>
                <goal>report-aggregate</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/reports/jacoco-aggregate</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 📚 相关文档

- [JaCoCo 官方文档](https://www.jacoco.org/jacoco/trunk/doc/)
- [Maven JaCoCo 插件](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- `如何运行测试.md` - 测试运行指南
- `报告位置速查.md` - 所有报告位置

---

**快速开始**：

```cmd
cd reports
test-jacoco.bat
# 选择 Y 打开报告
```

报告会自动在浏览器中打开！
