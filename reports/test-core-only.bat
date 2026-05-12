@echo off
chcp 65001 >nul
REM 只测试 dlz-db-core 模块（不需要外部依赖）

echo ==========================================
echo 测试 dlz-db-core 模块
echo ==========================================
echo.

echo 运行 dlz-db-core 的单元测试...
cd ..
call mvn clean test -pl dlz-db-core -DskipTests=false
call mvn jacoco:report -pl dlz-db-core
cd reports

if errorlevel 1 (
    echo.
    echo [错误] 测试失败
    pause
    exit /b 1
)

echo.
echo ==========================================
echo 测试完成！
echo ==========================================
echo.
echo 报告位置：
echo   - Core模块: ..\dlz-db-core\target\reports\jacoco\index.html
echo.

set /p open="是否打开报告？(Y/N): "
if /i "%open%"=="Y" (
    if exist "..\dlz-db-core\target\reports\jacoco\index.html" (
        echo 打开 Core 模块报告...
        start "" "..\dlz-db-core\target\reports\jacoco\index.html"
    ) else (
        echo [警告] 未找到报告文件
    )
)

pause
