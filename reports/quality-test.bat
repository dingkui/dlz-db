@echo off
chcp 65001 >nul
REM DLZ-DB 代码质量测试入口

echo ==========================================
echo DLZ-DB 代码质量测试
echo ==========================================
echo.
echo 选择操作：
echo.
echo 1. 运行所有质量检查
echo 2. 运行单个工具检查
echo 3. 查看测试报告
echo 4. 查看使用文档
echo.
set /p choice="请选择 (1-4): "

if "%choice%"=="1" (
    cd reports
    call test-all.bat
    cd ..
)

if "%choice%"=="2" (
    echo.
    echo 选择测试工具：
    echo.
    echo 1. JaCoCo - 测试覆盖率
    echo 2. SpotBugs - 静态分析
    echo 3. Checkstyle - 代码规范
    echo 4. PMD - 代码质量
    echo.
    set /p tool="请选择 (1-4): "
    
    cd reports
    if "!tool!"=="1" call test-jacoco.bat
    if "!tool!"=="2" call test-spotbugs.bat
    if "!tool!"=="3" call test-checkstyle.bat
    if "!tool!"=="4" call test-pmd.bat
    cd ..
)

if "%choice%"=="3" (
    cd reports
    call view-reports.bat
    cd ..
)

if "%choice%"=="4" (
    start "" "reports\README-质量测试工具.md"
)

pause
