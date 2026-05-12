# DLZ-DB 代码质量测试工具包

## 📦 工具清单

本项目包含 4 种独立的代码质量测试工具，每个工具都有独立的运行脚本：

| # | 工具 | 脚本 | 用途 |
|---|------|------|------|
| 1 | **JaCoCo** | `test-jacoco.bat` | 测试覆盖率分析 |
| 2 | **SpotBugs** | `test-spotbugs.bat` | 静态分析 + 安全检查 |
| 3 | **Checkstyle** | `test-checkstyle.bat` | 代码规范检查 |
| 4 | **PMD** | `test-pmd.bat` | 代码质量 + 重复代码检测 |
| 5 | **全部** | `test-all.bat` | 运行所有检查 |

---

## 🚀 快速开始

### 1. 运行所有检查（推荐）

```cmd
test-all.bat
```

这将依次运行所有 4 种检查，并在完成后询问是否打开报告查看器。

---

### 2. 单独运行某个工具

```cmd
REM 测试覆盖率
test-jacoco.bat

REM 静态分析
test-spotbugs.bat

REM 代码规范
test-checkstyle.bat

REM 代码质量
test-pmd.bat
```

---

### 3. 查看报告

```cmd
view-reports.bat
```

交互式菜单，选择要查看的报告。

---

## 📊 报告位置

所有报告统一存放在 `target/reports/` 目录下：

```
target/
├── reports/
│   ├── jacoco-aggregate/
│   │   └── index.html          ⭐ JaCoCo 覆盖率报告
│   ├── spotbugs/
│   │   └── spotbugsXml.html    ⭐ SpotBugs 分析报告
│   ├── checkstyle/
│   │   └── checkstyle-result.xml
│   └── pmd/
│       ├── pmd.xml
│       └── cpd.xml
│
└── site/
    ├── checkstyle.html          ⭐ Checkstyle 报告
    ├── pmd.html                 ⭐ PMD 报告
    └── cpd.html                 ⭐ CPD 报告
```

---

## 🎯 工具详解

### 1. JaCoCo - 测试覆盖率

**运行**：
```cmd
test-jacoco.bat
```

**检测内容**：
- 行覆盖率
- 分支覆盖率
- 方法覆盖率
- 类覆盖率

**报告**：
- 聚合报告：`target/reports/jacoco-aggregate/index.html`
- 各模块报告：`{module}/target/reports/jacoco/index.html`

**如何查看**：
1. 打开 HTML 报告
2. 🟢 绿色 = 已覆盖
3. 🔴 红色 = 未覆盖
4. 🟡 黄色 = 部分覆盖

**质量目标**：
- 行覆盖率 ≥ 70%
- 分支覆盖率 ≥ 60%

---

### 2. SpotBugs - 静态分析

**运行**：
```cmd
test-spotbugs.bat
```

**检测内容**：
- Bug 模式（空指针、资源泄漏等）
- 安全漏洞（SQL注入、XSS等）
- FindSecBugs 安全检查

**报告**：
- HTML：`target/reports/spotbugs/spotbugsXml.html`
- XML：`target/reports/spotbugs/spotbugsXml.xml`

**问题级别**：
- 🔴 High = 严重，必须修复
- 🟡 Medium = 中等，建议修复
- 🟢 Low = 轻微，可选修复

**质量目标**：
- 0 个 High 级别问题
- 0 个安全漏洞

---

### 3. Checkstyle - 代码规范

**运行**：
```cmd
test-checkstyle.bat
```

**检测内容**：
- 命名规范
- 缩进和空格
- 导入顺序
- Javadoc 注释
- 代码结构

**标准**：Google Java Style Guide

**报告**：
- HTML：`target/site/checkstyle.html`
- XML：`target/reports/checkstyle/checkstyle-result.xml`

**质量目标**：
- 逐步减少违规数量
- 新代码符合规范

---

### 4. PMD - 代码质量

**运行**：
```cmd
test-pmd.bat
```

**检测内容**：
- 圈复杂度
- 代码坏味道
- 未使用的变量
- 空的 catch 块
- 重复代码（CPD）

**报告**：
- PMD：`target/site/pmd.html`
- CPD：`target/site/cpd.html`

**质量目标**：
- 圈复杂度 < 10
- 重复代码率 < 5%
- 0 个 Priority 1-2 问题

---

## 📚 文档清单

| 文档 | 说明 |
|------|------|
| `代码质量测试使用指南.md` | 📖 完整使用指南 |
| `测试工具速查表.md` | 📋 快速参考 |
| `报告位置速查.md` | 📍 报告位置清单 |
| `PMD报告分析-DLZ-DB.md` | 📊 PMD 报告分析 |
| `如何解读PMD报告.md` | 📖 PMD 解读指南 |

---

## 💡 使用建议

### 日常开发

```cmd
REM 提交前运行
test-jacoco.bat
```

### Pull Request

```cmd
REM PR 前运行
test-all.bat
```

### 定期维护

```cmd
REM 每周运行一次
test-all.bat
REM 查看报告，逐步改进
```

---

## 🔧 自定义配置

### 修改覆盖率阈值

编辑 `pom.xml`：

```xml
<properties>
    <jacoco.line.coverage>0.70</jacoco.line.coverage>
    <jacoco.branch.coverage>0.60</jacoco.branch.coverage>
</properties>
```

### 修改报告输出目录

编辑 `pom.xml` 中各插件的 `outputDirectory` 配置。

---

## ❓ 常见问题

### Q: 如何运行所有检查？

```cmd
test-all.bat
```

### Q: 报告在哪里？

```cmd
view-reports.bat
```

### Q: 如何只运行某个工具？

```cmd
test-jacoco.bat      # 只运行 JaCoCo
test-spotbugs.bat    # 只运行 SpotBugs
test-checkstyle.bat  # 只运行 Checkstyle
test-pmd.bat         # 只运行 PMD
```

### Q: 首次运行很慢？

正常现象，Maven 需要下载插件。后续运行会快很多。

### Q: 如何在 CI/CD 中使用？

参考 `代码质量测试使用指南.md` 中的 CI/CD 集成部分。

---

## 🎯 质量目标

| 指标 | 当前 | 目标 |
|------|------|------|
| 行覆盖率 | - | ≥ 70% |
| 分支覆盖率 | - | ≥ 60% |
| SpotBugs High | - | 0 |
| 安全漏洞 | - | 0 |
| PMD Priority 1-2 | 2 | 0 |
| 圈复杂度 | ✅ | < 10 |

---

## 📞 获取帮助

1. 查看 `代码质量测试使用指南.md`
2. 查看 `测试工具速查表.md`
3. 运行 `view-reports.bat` 查看报告

---

**开始使用**：

```cmd
test-all.bat
```

然后查看生成的报告，了解代码质量状况！
