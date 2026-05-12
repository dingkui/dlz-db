@echo off
chcp 65001 >nul
REM Checkstyle 代码规范检查

echo ==========================================
echo Checkstyle 代码规范检查
echo ==========================================
echo.

echo 运行 Checkstyle 检查（Google Java Style）...
cd ..
call mvn checkstyle:check
cd reports

if errorlevel 1 (
    echo.
    echo [警告] Checkstyle 发现规范问题
)

echo.
echo ==========================================
echo Checkstyle 检查完成！
echo ==========================================
echo.
echo 报告位置：
echo   - XML报告: ..\target\reports\checkstyle\checkstyle-result.xml
echo   - 站点报告: ..\target\site\checkstyle.html
echo.

set /p open="是否打开报告？(Y/N): "
if /i "%open%"=="Y" (
    if exist "..\target\site\checkstyle.html" (
        start "" "..\target\site\checkstyle.html"
    ) else if exist "..\target\reports\checkstyle\checkstyle-result.xml" (
        start "" "..\target\reports\checkstyle\checkstyle-result.xml"
    )
)

pause
