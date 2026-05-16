@echo off
chcp 65001 >nul
REM 快速测试 Core 模块并查看覆盖率报告

echo ==========================================
echo 快速测试 - Core 模块
echo ==========================================
echo.

cd ..

echo [1/2] 运行 Core 模块测试...
call mvn clean test -pl dlz-db-core -am -q

if errorlevel 1 (
    echo.
    echo [错误] 测试失败！
    pause
    exit /b 1
)

echo [2/2] 打开覆盖率报告...
if exist "dlz-db-core\target\reports\jacoco\index.html" (
    start "" "dlz-db-core\target\reports\jacoco\index.html"
    echo ✓ 报告已打开
) else (
    echo [错误] 报告文件不存在
    echo 请检查: dlz-db-core\target\reports\jacoco\
)

echo.
pause
