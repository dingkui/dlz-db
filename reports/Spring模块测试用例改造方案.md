# Spring 模块测试用例改造方案

## 目标

解决 `dlz-db-spring-boot-starter` 模块在 IDEA 中可以正常运行测试，但使用 `mvn test` 时出现以下问题：

- Spring 上下文重复初始化
- 测试日志不显示
- Maven 没有真正执行某些测试类
- `sqlExecutor is null` 等初始化时序问题

核心思路：

- 所有 Spring 集成测试继承同一个测试基类
- 所有测试类使用完全一致的 Spring Test 配置
- Maven Surefire 使用单 JVM、复用 fork
- 不使用 `@DirtiesContext`，避免破坏 Spring Test 的上下文缓存
- 测试类命名必须匹配 Surefire includes 规则

---

## 一、当前推荐测试模式

### 1. 使用统一测试启动类

文件：

```text
dlz-db-spring-boot-starter/src/test/java/com/dlz/test/db/Starter.java
```

推荐保持为 Spring Boot 测试启动入口：

```java
package com.dlz.test.db;

import com.dlz.db.holder.DBHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {"com.dlz.spring", "com.dlz.test.db.config"})
public class Starter implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

    public void afterPropertiesSet() throws Exception{
        DBHolder.getSqlExecutor();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("Spring 容器启动完成");
    }
}
```

说明：

- `Starter` 是所有测试共用的 Spring Boot 配置入口
- `@SpringBootApplication` 的 `scanBasePackages` 必须保持稳定
- 不要让不同测试类使用不同的配置类，否则 Spring 会认为是不同上下文

---

## 二、统一测试基类

文件：

```text
dlz-db-spring-boot-starter/src/test/java/com/dlz/test/db/config/SpingDbBaseTest.java
```

推荐写法：

```java
package com.dlz.test.db.config;

import com.dlz.db.inf.ISqlPara;
import com.dlz.db.modal.items.JdbcItem;
import com.dlz.db.modal.para.AParaPojo;
import com.dlz.db.modal.para.ParaMap;
import com.dlz.db.util.SqlUtil;
import com.dlz.kit.util.id.TraceUtil;
import com.dlz.test.db.Starter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class)
@Slf4j
public abstract class SpingDbBaseTest {
    @Before
    public void before(){
        if(TraceUtil.getTraceid()==null){
            TraceUtil.setTraceId();
        }
    }

    private String clearSql(String sql){
        return sql.replaceAll("\\s+"," ").trim();
    }

    public void showSql(ISqlPara paraMap, String fn, String re) {
        JdbcItem jdbcSql = paraMap.jdbcSql();
        String runSqlByJdbc = SqlUtil.getRunSqlByJdbc(jdbcSql.sql, jdbcSql.paras).trim();
        if(re==null){
            log.info(runSqlByJdbc);
        }else if(clearSql(re).equalsIgnoreCase(clearSql(runSqlByJdbc))){
            log.info("sucess:"+runSqlByJdbc);
        }else{
            log.error("error:"+runSqlByJdbc);
            log.error("target:"+re);
            assert false;
        }
    }

    public void showSql(ParaMap paraMap, String fn) {
        showSql(paraMap, fn, null);
    }

    public void showSql(AParaPojo wrapper, String fn) {
        showSql(wrapper, fn, null);
    }
}
```

关键点：

- 基类必须是 `abstract`
- 基类上统一放 `@RunWith(SpringRunner.class)`
- 基类上统一放 `@SpringBootTest(classes = Starter.class)`
- 不要在每个测试类上重复写 `@SpringBootTest`
- 不要加 `@DirtiesContext`
- 不要在基类中写 `@Test` 方法

---

## 三、具体测试类写法

所有需要 Spring 环境的测试类统一继承 `SpingDbBaseTest`。

示例：

```java
package com.dlz.test.db.cases.db;

import com.dlz.test.db.config.SpingDbBaseTest;
import org.junit.Test;

public class TableDeleteTest extends SpingDbBaseTest {
    @Test
    public void tableDeleteTest1() {
        // test code
    }
}
```

禁止写法：

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class TableDeleteTest extends SpingDbBaseTest {
}
```

原因：

- 每个测试类单独声明 Spring 配置，容易产生不同的 `MergedContextConfiguration`
- Spring Test 缓存 key 不一致时，会重新创建 ApplicationContext

---

## 四、Maven Surefire 配置

文件：

```text
dlz-db-spring-boot-starter/pom.xml
```

推荐配置：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <skip>false</skip>
                <skipTests>false</skipTests>
                <forkCount>1</forkCount>
                <reuseForks>true</reuseForks>
                <redirectTestOutputToFile>false</redirectTestOutputToFile>
                <includes>
                    <include>**/*Test.java</include>
                    <include>**/*Tests.java</include>
                </includes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

配置含义：

- `skip=false`：不跳过 Surefire 插件
- `skipTests=false`：不跳过测试执行
- `forkCount=1`：测试使用一个 JVM fork
- `reuseForks=true`：复用同一个 fork
- `redirectTestOutputToFile=false`：测试输出直接显示在控制台
- `includes`：只运行命名符合规则的测试类

---

## 五、测试类命名规则

Maven Surefire 只会执行匹配 includes 的类。

推荐命名：

```text
TableDeleteTest.java
WrapperSelectTest.java
IDbExecuteServiceTest.java
XxxTests.java
```

不推荐命名：

```text
TestClass1.java
DemoCase.java
SpringDemo.java
```

如果必须执行 `TestClass1.java` 这种名字，需要额外加入：

```xml
<include>**/Test*.java</include>
```

但在正式模块中建议统一使用 `*Test.java`。

---

## 六、为什么不要使用 @DirtiesContext

不要在通用基类上加：

```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
```

原因：

- `@DirtiesContext` 会把当前 ApplicationContext 标记为脏
- 当前测试类结束后，Spring 会关闭并丢弃上下文
- 下一个测试类即使配置完全相同，也必须重新初始化
- 这正好违背“多个测试类共享上下文”的目标

只有当某个测试确实修改了全局 Bean、环境变量、静态缓存，且会污染后续测试时，才在那个具体测试类上单独使用 `@DirtiesContext`。

---

## 七、为什么当前模式仍然使用 @SpringBootTest

`dlz-db-spring-boot-starter` 和简单 demo 不一样。

简单 demo 可以使用：

```java
@ContextConfiguration(classes = DemoTestConfig.class)
```

但真实 Spring Boot starter 模块通常依赖：

- `application.yml`
- Spring Boot 自动配置
- `@ConfigurationProperties`
- 数据源配置
- starter 自动装配逻辑

因此真实模块推荐：

```java
@SpringBootTest(classes = Starter.class)
```

而不是只用 `@ContextConfiguration`。

重点不是完全不用 `@SpringBootTest`，而是所有测试类必须使用同一个 `@SpringBootTest(classes = Starter.class)` 配置。

---

## 八、运行方式

只运行 Spring 模块：

```bash
mvn test -pl dlz-db-spring-boot-starter
```

从模块目录运行：

```bash
mvn test
```

运行单个测试类：

```bash
mvn test -pl dlz-db-spring-boot-starter -Dtest=TableDeleteTest
```

运行多个测试类：

```bash
mvn test -pl dlz-db-spring-boot-starter -Dtest=TableDeleteTest,WrapperSelectTest
```

---

## 九、验证是否复用 Spring 上下文

观察控制台输出：

```text
Spring 容器启动完成
```

预期：

- 同一次 `mvn test` 中，如果所有测试类配置一致，Spring 上下文应该被缓存复用
- 不应该每个测试类都完整重新启动一次 Spring 容器

如果仍然重复初始化，检查：

1. 是否有测试类单独加了 `@SpringBootTest`
2. 是否有测试类单独加了 `@ContextConfiguration`
3. 是否有测试类加了 `@DirtiesContext`
4. 是否 Surefire 使用了多个 fork
5. 是否不同测试类使用了不同 active profile
6. 是否不同测试类使用了不同 `@TestPropertySource`
7. 是否测试类命名没有被 Surefire 执行，导致误判

---

## 十、其他 AI 执行步骤

请按以下顺序改造：

1. 打开 `dlz-db-spring-boot-starter/pom.xml`
2. 找到 `maven-surefire-plugin`
3. 设置 `skip=false`、`skipTests=false`
4. 设置 `forkCount=1`、`reuseForks=true`
5. 设置 `redirectTestOutputToFile=false`
6. 设置 includes 为 `**/*Test.java` 和 `**/*Tests.java`
7. 打开 `SpingDbBaseTest.java`
8. 移除 `@ContextConfiguration`
9. 移除 `@SpringBootApplication`
10. 移除 `@DirtiesContext`
11. 移除基类中的 `@Test` 方法
12. 设置基类为 `abstract`
13. 设置基类注解为 `@RunWith(SpringRunner.class)` 和 `@SpringBootTest(classes = Starter.class)`
14. 确认所有 Spring 测试类继承 `SpingDbBaseTest`
15. 确认具体测试类不再重复声明 Spring 测试注解
16. 运行 `mvn test -pl dlz-db-spring-boot-starter`

---

## 十一、已按本方案修改的文件

本次已修改：

```text
dlz-db-spring-boot-starter/pom.xml
dlz-db-spring-boot-starter/src/test/java/com/dlz/test/db/config/SpingDbBaseTest.java
```

本次不强制删除：

```text
dlz-db-spring-boot-starter/src/test/java/com/dlz/test/db/simple/
```

该目录是前面用于验证 Maven/Surefire 行为的实验代码，当前正式 Surefire includes 不会执行 `SimpleTest1.java`、`SimpleTest2.java` 这种命名的类。
