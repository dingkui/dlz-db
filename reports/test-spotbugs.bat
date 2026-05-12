@echo off
chcp 65001 >nul
REM SpotBugs 静态分析检查

echo ==========================================
echo SpotBugs 静态分析检查
echo ==========================================
echo.

echo 运行 SpotBugs 分析（含安全漏洞检测）...
cd ..
call mvn compile spotbugs:check
cd reports

if errorlevel 1 (
    echo.
    echo [警告] SpotBugs 发现问题
)

echo.
echo ==========================================
echo SpotBugs 分析完成！
echo ==========================================
echo.
echo 报告位置：
echo   - HTML报告: ..\target\reports\spotbugs\spotbugsXml.html
echo   - XML报告: ..\target\reports\spotbugs\spotbugsXml.xml
echo.

set /p open="是否打开报告？(Y/N): "
if /i "%open%"=="Y" (
    if exist "..\target\reports\spotbugs\spotbugsXml.html" (
        start "" "..\target\reports\spotbugs\spotbugsXml.html"
    )
)

pause
