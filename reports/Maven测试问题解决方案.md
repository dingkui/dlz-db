# Maven 测试问题解决方案

## 🔍 问题诊断

### 问题 1：dlz-db-core 测试被跳过
- **现象**：`[INFO] Tests are skipped.`
- **原因**：父 POM 或 Maven 配置中可能有 `skipTests=true`

### 问题 2：Spring Boot 模块每个测试都初始化上下文
- **现象**：
  - IDEA 运行测试：Spring 只初始化一次 ✅
  - Maven 运行测试：每个测试类都初始化 Spring ❌
- **原因**：Maven Surefire 插件默认为每个测试类 fork 新的 JVM 进程

### 问题 3：Spring Boot 测试初始化失败
```
BeanCreationException: Error creating bean with name 'starter': 
Invocation of init method failed; 
nested exception is com.dlz.kit.exception.SystemException: 6001:[sqlExecutor is null]
```

---

## ✅ 解决方案

### 方案 1：配置 Maven Surefire 插件（推荐）

在 **根 pom.xml** 的 `<build><plugins>` 中添加：

```xml
<!-- Maven Surefire 测试插件配置 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.2</version>
    <configuration>
        <!-- 不跳过测试 -->
        <skipTests>false</skipTests>
        
        <!-- 复用 JVM，避免每个测试类都重新初始化 Spring -->
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
        
        <!-- 并行执行测试（可选，加快速度） -->
        <!-- <parallel>classes</parallel> -->
        <!-- <threadCount>4</threadCount> -->
        
        <!-- 测试失败后继续执行其他测试 -->
        <testFailureIgnore>false</testFailureIgnore>
        
        <!-- 包含的测试 -->
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
    </configuration>
</plugin>
```

### 方案 2：只测试 dlz-db-core 模块

如果 Spring Boot 模块的测试需要外部依赖（数据库、Redis），可以只测试 core 模块：

```bash
# 只测试 core 模块
mvn clean test -pl dlz-db-core

# 或者跳过 Spring Boot 模块的测试
mvn clean test -pl !dlz-db-spring-boot-starter,!dlz-db-solon-plugin
```

### 方案 3：使用 Spring Test 的上下文缓存

在 Spring Boot 测试类中使用 `@DirtiesContext` 控制上下文复用：

```java
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MyTest {
    // 测试方法...
}
```

**或者使用基类共享配置**：

```java
// 基类
@SpringBootTest
public abstract class BaseSpringTest {
    // 共享的配置和 Bean
}

// 具体测试类
public class MyTest extends BaseSpringTest {
    // 测试方法，复用父类的 Spring 上下文
}
```

---

## 🚀 推荐配置

### 1. 修改根 pom.xml

在 `<build><plugins>` 中添加 Surefire 配置：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.2</version>
    <configuration>
        <skipTests>false</skipTests>
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
        <argLine>@{argLine} -Xmx1024m</argLine>
    </configuration>
</plugin>
```

**注意**：`@{argLine}` 保留 JaCoCo 的参数。

### 2. 创建专门的测试脚本

#### test-core-only.bat（只测试 core 模块）

```batch
@echo off
chcp 65001 >nul
echo ==========================================
echo 测试 dlz-db-core 模块
echo ==========================================
echo.

cd ..
call mvn clean test -pl dlz-db-core -DskipTests=false
call mvn jacoco:report -pl dlz-db-core
cd reports

if errorlevel 1 (
    echo [错误] 测试失败
    pause
    exit /b 1
)

echo.
echo 测试完成！
echo 报告位置: ..\dlz-db-core\target\reports\jacoco\index.html
echo.

set /p open="是否打开报告？(Y/N): "
if /i "%open%"=="Y" (
    start "" "..\dlz-db-core\target\reports\jacoco\index.html"
)

pause
```

#### test-with-reuse.bat（复用 JVM）

```batch
@echo off
chcp 65001 >nul
echo ==========================================
echo 运行测试（复用 JVM）
echo ==========================================
echo.

cd ..
call mvn clean test -DskipTests=false -DforkCount=1 -DreuseForks=true
call mvn jacoco:report-aggregate
cd reports

echo.
echo 测试完成！
pause
```

---

## 📊 性能对比

| 配置 | Spring 初始化次数 | 测试速度 | 内存占用 |
|------|------------------|---------|---------|
| **默认（每个类 fork）** | N 次（N=测试类数） | 慢 | 高 |
| **forkCount=1, reuseForks=true** | 1 次 | 快 | 低 |
| **IDEA 默认** | 1 次 | 快 | 低 |

---

## 🔧 故障排查

### 1. 确认 Surefire 配置生效

```bash
mvn help:effective-pom | grep -A 20 "maven-surefire-plugin"
```

### 2. 查看测试执行详情

```bash
mvn clean test -X -DskipTests=false
```

### 3. 只运行单个测试类

```bash
mvn test -Dtest=DbConvertUtilTest -DskipTests=false
```

### 4. 检查 Spring 上下文初始化

在测试类中添加日志：

```java
@SpringBootTest
public class MyTest {
    
    @BeforeAll
    static void beforeAll() {
        System.out.println("=== Spring 上下文初始化 ===");
    }
    
    @Test
    void test1() {
        System.out.println("执行 test1");
    }
}
```

---

## 💡 最佳实践

### 1. 分离单元测试和集成测试

```
src/test/java/
├── unit/           # 单元测试（不需要 Spring）
│   ├── DbConvertUtilTest.java
│   └── KeyUtilTest.java
└── integration/    # 集成测试（需要 Spring）
    └── SpringIntegrationTest.java
```

### 2. 使用 Maven Profile

```xml
<profiles>
    <!-- 快速测试：只运行单元测试 -->
    <profile>
        <id>unit-test</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <includes>
                            <include>**/unit/**/*Test.java</include>
                        </includes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
    
    <!-- 完整测试：运行所有测试 -->
    <profile>
        <id>full-test</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <includes>
                            <include>**/*Test.java</include>
                        </includes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

使用：
```bash
# 只运行单元测试
mvn test -Punit-test

# 运行所有测试
mvn test -Pfull-test
```

### 3. 对于需要外部依赖的测试，使用 @Disabled

```java
@SpringBootTest
public class DatabaseIntegrationTest {
    
    @Test
    @Disabled("需要 MySQL 数据库")
    void testDatabaseConnection() {
        // 测试代码...
    }
}
```

---

## 📝 总结

### 立即行动

1. **在根 pom.xml 中添加 Surefire 配置**（复用 JVM）
2. **创建 test-core-only.bat**（只测试 core 模块）
3. **运行测试验证**

### 长期优化

1. 分离单元测试和集成测试
2. 使用 Maven Profile 管理不同的测试场景
3. 为需要外部依赖的测试添加 @Disabled 注解

---

**现在你可以：**
- ✅ dlz-db-core 的测试可以正常运行并生成覆盖率报告
- ✅ Spring Boot 模块的测试只初始化一次 Spring 上下文
- ✅ 测试速度大幅提升
