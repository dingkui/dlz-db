# JaCoCo 测试覆盖率报告

## 📁 脚本说明

### 1. `test-core-quick.bat` - 快速测试（推荐日常使用）

**用途**：快速运行Core模块测试并打开覆盖率报告

**执行命令**：
```batch
test-core-quick.bat
```

**特点**：
- ✅ 只测试Core模块（速度快）
- ✅ 自动打开HTML报告
- ✅ 适合日常开发使用

**报告位置**：`..\dlz-db-core\target\reports\jacoco\index.html`

---

### 2. `test-jacoco.bat` - 完整测试（包含聚合报告）

**用途**：运行Core模块测试并生成聚合报告

**执行命令**：
```batch
test-jacoco.bat
```

**特点**：
- ✅ 测试Core模块
- ✅ 生成独立报告
- ✅ 尝试生成聚合报告
- ⚠️ 速度较慢（需要执行verify阶段）

**报告位置**：
- Core模块：`..\dlz-db-core\target\reports\jacoco\index.html`
- 聚合报告：`..\target\reports\jacoco-aggregate\index.html`

---

## 🔧 Maven 命令参考

### 仅测试 Core 模块
```bash
mvn clean test -pl dlz-db-core -am
```

### 生成 Core 模块报告
```bash
mvn jacoco:report -pl dlz-db-core
```

### 生成聚合报告
```bash
mvn verify -Pjacoco-aggregate
```

### 完整构建（所有模块）
```bash
mvn clean verify
```

---

## 📊 报告说明

### 独立报告 vs 聚合报告

| 类型 | 说明 | 位置 |
|------|------|------|
| **独立报告** | 单个模块的覆盖率 | `模块/target/reports/jacoco/` |
| **聚合报告** | 所有模块的汇总覆盖率 | `父项目/target/reports/jacoco-aggregate/` |

### 为什么需要两种报告？

1. **独立报告**：
   - 查看单个模块的详细覆盖率
   - 快速定位问题
   - 不依赖其他模块

2. **聚合报告**：
   - 查看整个项目的总体覆盖率
   - 跨模块的代码覆盖情况
   - 适合CI/CD集成

---

## ⚠️ 注意事项

### Spring Boot 和 Solon 模块

这两个模块需要外部依赖才能运行测试：
- MySQL 数据库
- Redis 缓存

如果需要测试这些模块，请：
1. 确保MySQL和Redis已启动
2. 配置正确的连接信息
3. 单独运行对应模块的测试：

```bash
# 测试 Spring Boot 模块
mvn clean test -pl dlz-db-spring-boot-starter

# 测试 Solon 模块
mvn clean test -pl dlz-db-solon-plugin
```

---

## 🎯 覆盖率目标

| 模块 | 当前覆盖率 | 目标覆盖率 |
|------|-----------|-----------|
| dlz-db-core | ~5% | 80%+ |
| dlz-db-solon-plugin | - | 80%+ |
| dlz-db-spring-boot-starter | - | 80%+ |

**改进计划**：
1. 增加单元测试覆盖边界条件
2. 添加集成测试验证真实场景
3. 补充异常处理路径的测试

---

## 🐛 常见问题

### Q1: 为什么Core模块的报告是空的？

**原因**：测试可能被跳过或失败

**解决**：
```bash
# 检查测试是否执行
mvn test -pl dlz-db-core

# 手动生成报告
mvn jacoco:report -pl dlz-db-core
```

### Q2: 聚合报告为什么没有数据？

**原因**：
- 某些模块的测试被跳过
- 没有执行到verify阶段

**解决**：
```bash
# 使用profile生成聚合报告
mvn verify -Pjacoco-aggregate
```

### Q3: 如何查看具体的未覆盖代码？

**步骤**：
1. 打开HTML报告
2. 点击包名进入详细视图
3. 点击类名查看代码行级别的覆盖率
4. 红色行表示未覆盖，绿色行表示已覆盖

---

## 📞 技术支持

如有问题，请联系：
- Email: 59461202@qq.com
- GitHub: https://github.com/dingkui/dlz-db
