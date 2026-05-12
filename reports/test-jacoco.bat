@echo off
chcp 65001 >nul
REM JaCoCo 测试覆盖率检查
REM 注意：Spring Boot 模块的测试已跳过（需要外部依赖：数据库、Redis）

echo ==========================================
echo JaCoCo 测试覆盖率检查
echo ==========================================
echo.
echo 注意：Spring Boot 模块的测试已跳过
echo      （需要外部依赖：MySQL、Redis）
echo.

echo 运行测试并生成覆盖率报告...
cd ..
call mvn clean test -Dmaven.test.skip=false -DskipTests=false
call mvn jacoco:report-aggregate
cd reports

if errorlevel 1 (
    echo.
    echo [错误] 测试失败
    pause
    exit /b 1
)

echo.
echo ==========================================
echo 覆盖率报告生成完成！
echo ==========================================
echo.
echo 报告位置：
echo   - 聚合报告: ..\target\reports\jacoco-aggregate\index.html
echo   - Core模块: ..\dlz-db-core\target\reports\jacoco\index.html
echo   - Spring Boot模块: ..\dlz-db-spring-boot-starter\target\reports\jacoco\index.html
echo   - Solon模块: ..\dlz-db-solon-plugin\target\reports\jacoco\index.html
echo.

set /p open="是否打开报告？(Y/N): "
if /i "%open%"=="Y" (
    if exist "..\target\reports\jacoco-aggregate\index.html" (
        echo 打开聚合报告...
        start "" "..\target\reports\jacoco-aggregate\index.html"
    ) else if exist "..\dlz-db-core\target\reports\jacoco\index.html" (
        echo 打开 Core 模块报告...
        start "" "..\dlz-db-core\target\reports\jacoco\index.html"
    ) else (
        echo [警告] 未找到报告文件
        echo.
        echo 请检查以下位置：
        echo   - ..\target\reports\jacoco-aggregate\
        echo   - ..\dlz-db-core\target\reports\jacoco\
    )
)

pause
