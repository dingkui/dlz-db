@echo off
chcp 65001 >nul
REM OWASP 依赖安全检查

echo ==========================================
echo OWASP 依赖安全检查
echo ==========================================
echo.

REM 切换到项目根目录
cd ..

echo 运行 OWASP 依赖安全扫描...
echo （首次运行会下载漏洞数据库，可能需要较长时间）
echo.
call mvn dependency-check:check

if errorlevel 1 (
    echo.
    echo [警告] 发现安全漏洞
)

echo.
echo ==========================================
echo OWASP 安全扫描完成！
echo ==========================================
echo.
echo 报告位置：
echo   - target\reports\dependency-check-report.html
echo.

set /p open="是否打开报告？(Y/N): "
if /i "%open%"=="Y" (
    if exist "target\reports\dependency-check-report.html" (
        start "" "target\reports\dependency-check-report.html"
    )
)

REM 返回 reports 目录
cd reports

pause
