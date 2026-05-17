# DLZ-DB 测试工具

**项目**: dlz-db  
**版本**: 7.0.0

---

## 📁 文件说明

### 📊 测试报告（按日期命名）

| 文件 | 说明 |
|------|------|
| `TEST_REPORT_YYYY-MM-DD.md` | 详细测试报告 |
| `TEST_SUMMARY_YYYY-MM-DD.md` | 快速汇总报告 |

**最新报告**: [TEST_REPORT_2026-05-17.md](TEST_REPORT_2026-05-17.md)

### 🧪 测试脚本

| 脚本 | 说明 |
|------|------|
| `test-all.bat` | 运行所有测试（推荐） |
| `test-core-only.bat` | 仅运行 core 模块测试 |
| `test-jacoco.bat` | 运行测试并生成覆盖率报告 |
| `view-reports.bat` | 查看测试报告 |

### ⚙️ 配置文件

| 文件 | 说明 |
|------|------|
| `dependency-check-suppressions.xml` | OWASP依赖检查抑制配置 |
| `spotbugs-exclude.xml` | SpotBugs排除规则 |

---

## 🚀 快速开始

### 方式1: 使用测试脚本（推荐）

#### 运行所有测试
```bash
test-all.bat
```

#### 仅测试 core 模块
```bash
test-core-only.bat
```

#### 生成覆盖率报告
```bash
test-jacoco.bat
```

#### 查看测试结果
```bash
view-reports.bat
```

### 方式2: 使用 Maven 命令

#### 完整测试
```bash
mvn clean test
```

#### 单个模块
```bash
mvn clean test -pl dlz-db-core
mvn clean test -pl dlz-db-spring-boot-starter
mvn clean test -pl dlz-db-solon-plugin
```

#### 特定测试类
```bash
mvn test -Dtest=DbPojoTest
mvn test -Dtest=TransactionTest
```

---

## ✅ 测试通过标准

- 所有测试用例通过（605/605）
- 无失败、无错误
- 成功率 100%

---

## 📊 测试报告位置

运行测试后，报告生成在：
- **Surefire报告**: `../dlz-db-*/target/surefire-reports/`
- **JaCoCo报告**: `../dlz-db-*/target/site/jacoco/index.html`

---

## 📝 更多信息

### 最新测试报告（2026-05-17）
- **[详细报告](2026-05-17/TEST_REPORT.md)** - 完整的测试分析和数据
- **[快速汇总](2026-05-17/TEST_SUMMARY.md)** - 一页纸了解测试结果

### 测试文档索引
- [../TESTING.md](../TESTING.md) - dlz-db 测试文档总索引

---

*最后更新: 2026-05-17*
