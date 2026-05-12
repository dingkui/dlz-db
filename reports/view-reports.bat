@echo off
chcp 65001 >nul
REM 查看代码质量报告

setlocal enabledelayedexpansion

echo ==========================================
echo DLZ-DB 质量报告查看器
echo ==========================================
echo.

REM 切换到项目根目录
cd ..

REM 检查报告是否存在
set REPORTS_FOUND=0

echo 检查可用的报告...
echo.

REM 1. JaCoCo 覆盖率报告（聚合）
if exist "target\reports\jacoco-aggregate\index.html" (
    echo [✓] JaCoCo 覆盖率报告（多模块聚合）
    set JACOCO_AGGREGATE=target\reports\jacoco-aggregate\index.html
    set REPORTS_FOUND=1
) else if exist "target\site\jacoco\index.html" (
    echo [✓] JaCoCo 覆盖率报告
    set JACOCO_AGGREGATE=target\site\jacoco\index.html
    set REPORTS_FOUND=1
) else (
    echo [✗] JaCoCo 覆盖率报告 - 未找到
)

REM 2. 各模块的覆盖率报告
if exist "dlz-db-core\target\reports\jacoco\index.html" (
    echo [✓] dlz-db-core 覆盖率报告
    set JACOCO_CORE=dlz-db-core\target\reports\jacoco\index.html
    set REPORTS_FOUND=1
)

if exist "dlz-db-spring-boot-starter\target\reports\jacoco\index.html" (
    echo [✓] dlz-db-spring-boot-starter 覆盖率报告
    set JACOCO_SPRING=dlz-db-spring-boot-starter\target\reports\jacoco\index.html
    set REPORTS_FOUND=1
)

if exist "dlz-db-solon-plugin\target\reports\jacoco\index.html" (
    echo [✓] dlz-db-solon-plugin 覆盖率报告
    set JACOCO_SOLON=dlz-db-solon-plugin\target\reports\jacoco\index.html
    set REPORTS_FOUND=1
)

REM 3. SpotBugs 报告
if exist "target\reports\spotbugs\spotbugsXml.html" (
    echo [✓] SpotBugs 静态分析报告
    set SPOTBUGS=target\reports\spotbugs\spotbugsXml.html
    set REPORTS_FOUND=1
) else (
    echo [✗] SpotBugs 报告 - 未找到
)

REM 4. Checkstyle 报告
if exist "target\site\checkstyle.html" (
    echo [✓] Checkstyle 代码规范报告
    set CHECKSTYLE=target\site\checkstyle.html
    set REPORTS_FOUND=1
) else (
    echo [✗] Checkstyle 报告 - 未找到
)

REM 5. PMD 报告
if exist "target\site\pmd.html" (
    echo [✓] PMD 代码质量报告
    set PMD=target\site\pmd.html
    set REPORTS_FOUND=1
) else (
    echo [✗] PMD 报告 - 未找到
)

REM 6. CPD 重复代码报告
if exist "target\site\cpd.html" (
    echo [✓] CPD 重复代码报告
    set CPD=target\site\cpd.html
    set REPORTS_FOUND=1
) else (
    echo [✗] CPD 报告 - 未找到
)

REM 7. 测试报告
if exist "target\surefire-reports" (
    echo [✓] 测试报告目录
    set TEST_REPORTS=target\surefire-reports
    set REPORTS_FOUND=1
) else (
    echo [✗] 测试报告 - 未找到
)

REM 8. 综合站点
if exist "target\site\index.html" (
    echo [✓] Maven 综合站点
    set SITE=target\site\index.html
    set REPORTS_FOUND=1
)

echo.

if %REPORTS_FOUND%==0 (
    echo ==========================================
    echo 未找到任何报告！
    echo ==========================================
    echo.
    echo 请先运行质量检查：
    echo   - 单项测试: 在 reports 目录运行 test-*.bat
    echo   - 完整检查: 在 reports 目录运行 test-all.bat
    echo.
    cd reports
    pause
    exit /b 1
)

echo ==========================================
echo 选择要查看的报告
echo ==========================================
echo.
echo 1. JaCoCo 覆盖率报告（聚合）
echo 2. dlz-db-core 覆盖率
echo 3. dlz-db-spring-boot-starter 覆盖率
echo 4. dlz-db-solon-plugin 覆盖率
echo 5. SpotBugs 静态分析
echo 6. Checkstyle 代码规范
echo 7. PMD 代码质量
echo 8. CPD 重复代码
echo 9. 测试报告目录
echo 0. 打开所有报告
echo.
set /p choice="请选择 (0-9): "

if "%choice%"=="1" (
    if defined JACOCO_AGGREGATE (
        echo 打开 JaCoCo 覆盖率报告...
        start "" "%JACOCO_AGGREGATE%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="2" (
    if defined JACOCO_CORE (
        echo 打开 dlz-db-core 覆盖率报告...
        start "" "%JACOCO_CORE%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="3" (
    if defined JACOCO_SPRING (
        echo 打开 dlz-db-spring-boot-starter 覆盖率报告...
        start "" "%JACOCO_SPRING%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="4" (
    if defined JACOCO_SOLON (
        echo 打开 dlz-db-solon-plugin 覆盖率报告...
        start "" "%JACOCO_SOLON%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="5" (
    if defined SPOTBUGS (
        echo 打开 SpotBugs 报告...
        start "" "%SPOTBUGS%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="6" (
    if defined CHECKSTYLE (
        echo 打开 Checkstyle 报告...
        start "" "%CHECKSTYLE%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="7" (
    if defined PMD (
        echo 打开 PMD 报告...
        start "" "%PMD%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="8" (
    if defined CPD (
        echo 打开 CPD 报告...
        start "" "%CPD%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="9" (
    if defined TEST_REPORTS (
        echo 打开测试报告目录...
        start "" "%TEST_REPORTS%"
    ) else (
        echo 报告不存在
    )
)

if "%choice%"=="0" (
    echo 打开所有可用报告...
    if defined JACOCO_AGGREGATE start "" "%JACOCO_AGGREGATE%"
    timeout /t 1 /nobreak >nul
    if defined SPOTBUGS start "" "%SPOTBUGS%"
    timeout /t 1 /nobreak >nul
    if defined CHECKSTYLE start "" "%CHECKSTYLE%"
    timeout /t 1 /nobreak >nul
    if defined PMD start "" "%PMD%"
    timeout /t 1 /nobreak >nul
    if defined CPD start "" "%CPD%"
    timeout /t 1 /nobreak >nul
    if defined SITE start "" "%SITE%"
)

echo.
echo 完成！
echo.

REM 返回 reports 目录
cd reports

endlocal
pause
