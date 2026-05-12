# DLZ-DB 代码质量测试工具

## 🚀 快速开始

### 从项目根目录运行

```cmd
quality-test.bat
```

### 从 reports 目录运行

```cmd
cd reports

REM 运行所有检查
test-all.bat

REM 运行单个工具
test-jacoco.bat      # 测试覆盖率
test-spotbugs.bat    # 静态分析
test-checkstyle.bat  # 代码规范
test-pmd.bat         # 代码质量
test-owasp.bat       # 依赖安全（耗时较长）

REM 查看报告
view-reports.bat
```

---

## 📋 工具清单

| 脚本 | 工具 | 用途 | 耗时 |
|------|------|------|------|
| `test-all.bat` | 全部 | 运行所有检查（不含OWASP） | 3-5分钟 |
| `test-jacoco.bat` | JaCoCo | 测试覆盖率 | 1-2分钟 |
| `test-spotbugs.bat` | SpotBugs | 静态分析 + 安全检查 | 1-2分钟 |
| `test-checkstyle.bat` | Checkstyle | 代码规范 | 30秒 |
| `test-pmd.bat` | PMD | 代码质量 + 重复代码 | 1分钟 |
| `test-owasp.bat` | OWASP | 依赖安全扫描 | 5-10分钟 |
| `view-reports.bat` | - | 查看报告 | 即时 |

---

## 📊 报告位置

所有报告生成在 `../target/reports/` 目录：

```
../target/
├── reports/
│   ├── jacoco-aggregate/
│   │   └── index.html                    ⭐ 覆盖率报告
│   ├── spotbugs/
│   │   └── spotbugsXml.html              ⭐ 静态分析报告
│   ├── dependency-check-report.html      ⭐ OWASP 安全报告
│   ├── checkstyle/
│   │   └── checkstyle-result.xml
│   └── pmd/
│       ├── pmd.xml
│       └── cpd.xml
│
└── site/
    ├── checkstyle.html                    ⭐ Checkstyle 报告
    ├── pmd.html                           ⭐ PMD 报告
    └── cpd.html                           ⭐ CPD 报告
```

---

## 📚 文档清单

| 文档 | 说明 |
|------|------|
| `README-质量测试工具.md` | 📖 完整说明 |
| `代码质量测试使用指南.md` | 📚 详细指南 |
| `测试工具速查表.md` | 📋 快速参考 |
| `报告位置速查.md` | 📍 报告位置 |
| `PMD报告分析-DLZ-DB.md` | 📊 PMD 分析 |
| `如何解读PMD报告.md` | 📖 PMD 解读 |

---

## 💡 使用示例

### 场景 1：提交前检查

```cmd
cd reports
test-jacoco.bat
```

### 场景 2：PR 前完整检查

```cmd
cd reports
test-all.bat
```

### 场景 3：查找 Bug

```cmd
cd reports
test-spotbugs.bat
```

### 场景 4：查看所有报告

```cmd
cd reports
view-reports.bat
```

---

## 🎯 质量目标

| 指标 | 目标 |
|------|------|
| 行覆盖率 | ≥ 70% |
| 分支覆盖率 | ≥ 60% |
| SpotBugs High | 0 |
| 安全漏洞 | 0 |
| PMD Priority 1-2 | 0 |
| 圈复杂度 | < 10 |

---

## ❓ 常见问题

### Q: 如何运行所有检查？

```cmd
cd reports
test-all.bat
```

### Q: 报告在哪里？

```cmd
cd reports
view-reports.bat
```

### Q: 如何查看文档？

打开 `README-质量测试工具.md` 查看完整文档。

---

**开始使用**：

```cmd
cd reports
test-all.bat
```
