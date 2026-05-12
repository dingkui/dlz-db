@echo off
chcp 65001 >nul
REM 运行所有代码质量检查

setlocal enabledelayedexpansion

echo ==========================================
echo DLZ-DB 完整代码质量检查
echo ==========================================
echo.

REM 切换到项目根目录
cd ..

REM 创建报告目录
set REPORT_DIR=target\reports
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"

echo.
echo [1/5] 编译项目...
call mvn clean compile -DskipTests
if errorlevel 1 goto error

echo.
echo [2/5] 运行单元测试 + JaCoCo 覆盖率...
call mvn test
call mvn jacoco:report-aggregate
if errorlevel 1 goto error

echo.
echo [3/5] SpotBugs 静态分析（含安全漏洞检测）...
call mvn spotbugs:check
if errorlevel 1 echo [警告] SpotBugs 发现问题，请查看报告

echo.
echo [5/5] PMD 代码质量分析（含重复代码检测）...
call mvn pmd:check pmd:cpd-check
if errorlevel 1 echo [警告] PMD 发现问题，请查看报告

echo.
echo ==========================================
echo 所有质量检查完成！
echo ==========================================
echo.
echo 报告位置：
echo.
echo 1. JaCoCo 覆盖率
echo    target\reports\jacoco-aggregate\index.html
echo.
echo 2. SpotBugs 静态分析
echo    target\reports\spotbugs\spotbugsXml.html
echo.
echo 3. Checkstyle 代码规范
echo    target\site\checkstyle.html
echo.
echo 4. PMD 代码质量
echo    target\site\pmd.html
echo.
echo 5. CPD 重复代码
echo    target\site\cpd.html
echo.
echo.
echo 提示：OWASP 依赖安全检查需要较长时间，可单独运行 test-owasp.bat
echo.

set /p open="是否打开报告查看器？(Y/N): "
if /i "%open%"=="Y" (
    cd reports
    call view-reports.bat
    cd ..
)

goto end

:error
echo.
echo [错误] 构建失败，请检查错误信息
cd reports
pause
exit /b 1

:end
cd reports
endlocal
pause
