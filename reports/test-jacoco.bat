@echo off
chcp 65001 >nul
REM JaCoCo 测试覆盖率检查
REM 注意：Spring Boot 模块的测试已跳过（需要外部依赖：数据库、Redis）

echo ==========================================
echo JaCoCo 测试覆盖率检查
echo ==========================================
echo.
echo 注意：Spring Boot 和 Solon 模块需要外部依赖（MySQL、Redis）
echo      本脚本仅测试 Core 模块
echo.

echo 运行测试并生成覆盖率报告...
cd ..

REM 步骤1：清理项目
echo [步骤1/4] 清理项目...
call mvn clean -q

REM 步骤2：运行dlz-db-core模块的测试
echo [步骤2/4] 运行 Core 模块测试...
call mvn test -pl dlz-db-core -am
if errorlevel 1 (
    echo.
    echo [错误] Core 模块测试失败
    pause
    exit /b 1
)

REM 步骤3：生成Core模块的独立报告（实际上在test阶段已经自动生成）
echo [步骤3/4] 检查 Core 模块报告...
if exist "..\dlz-db-core\target\reports\jacoco\index.html" (
    echo ✓ Core 模块报告已生成
) else (
    echo [警告] Core 模块报告未找到，尝试重新生成...
    call mvn jacoco:report -pl dlz-db-core -q
)

REM 步骤4：生成聚合报告（使用profile）
echo [步骤4/4] 生成聚合报告...
call mvn verify -Pjacoco-aggregate -pl dlz-db-core -am -q
cd reports

if errorlevel 1 (
    echo.
    echo [错误] 报告生成失败
    pause
    exit /b 1
)

echo.
echo ==========================================
echo 覆盖率报告生成完成！
echo ==========================================
echo.
echo 报告位置：
echo   - Core模块: ..\dlz-db-core\target\reports\jacoco\index.html
echo   - 聚合报告: ..\target\reports\jacoco-aggregate\index.html (如果生成成功)
echo.
echo 注意：
echo   - Spring Boot 和 Solon 模块需要外部依赖（MySQL、Redis）
echo   - 如需测试这些模块，请单独运行对应模块的测试
echo.

set /p open="是否打开报告？(Y/N): "
if /i "%open%"=="Y" (
    if exist "..\dlz-db-core\target\reports\jacoco\index.html" (
        echo 打开 Core 模块报告...
        start "" "..\dlz-db-core\target\reports\jacoco\index.html"
    ) else if exist "..\target\reports\jacoco-aggregate\index.html" (
        echo 打开聚合报告...
        start "" "..\target\reports\jacoco-aggregate\index.html"
    ) else (
        echo [警告] 未找到报告文件
        echo.
        echo 请检查以下位置：
        echo   - ..\dlz-db-core\target\reports\jacoco\
        echo   - ..\target\reports\jacoco-aggregate\
    )
)

pause
