# Spring Test Demo

Spring Boot 测试最佳实践演示模块

## 目的

演示如何正确配置 Spring 测试，避免 Maven 运行时多次初始化 Spring 上下文的问题。

## 最佳实践

### 1. 使用 @ContextConfiguration 而非 @SpringBootTest

```java
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DemoTestConfig.class)
public abstract class DemoTestBase {
}
```

**优点**：
- 初始化更快
- 上下文缓存更有效
- 适合单元测试

### 2. 配置 Maven Surefire

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
    </configuration>
</plugin>
```

**作用**：
- 确保所有测试在同一个 JVM 中运行
- 复用 JVM 进程，避免重复初始化

### 3. 统一测试配置

所有测试类继承同一个基类，使用相同的配置类，确保 Spring 上下文缓存生效。

## 运行测试

### IDEA 运行

```bash
# 运行单个测试类
右键点击 TestClass1 -> Run

# 运行整个模块
右键点击 spring-test-demo -> Run All Tests
```

### Maven 运行

```bash
# 进入模块目录
cd spring-test-demo

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=TestClass1

# 运行多个测试类
mvn test -Dtest=TestClass1,TestClass2,TestClass3
```

## 验证上下文复用

观察控制台输出中的 `DemoService 初始化次数`：

**预期结果**：
- 同一测试类内的多个测试方法：初始化 1 次
- Maven 运行多个测试类（配置相同）：初始化 1 次（上下文缓存生效）

## 对比方案

### 方案 A：使用 @SpringBootTest
- 每次启动完整 Spring Boot 应用
- 初始化慢
- 可能导致多次初始化

### 方案 B：使用 @ContextConfiguration（推荐）
- 只加载必要的配置
- 初始化快
- 上下文缓存有效
