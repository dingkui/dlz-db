@echo off
chcp 65001 >nul
REM PMD 代码质量检查

echo ==========================================
echo PMD 代码质量检查
echo ==========================================
echo.

REM 切换到项目根目录
cd ..

echo 运行 PMD 分析（含重复代码检测）...
call mvn pmd:check pmd:cpd-check

if errorlevel 1 (
    echo.
    echo [警告] PMD 发现代码质量问题
)

echo.
echo ==========================================
echo PMD 分析完成！
echo ==========================================
echo.
echo 报告位置：
echo   - PMD报告: target\reports\pmd\pmd.html
echo   - CPD报告: target\reports\pmd\cpd.html
echo   - 站点报告: target\site\pmd.html
echo.

set /p open="是否打开报告？(Y/N): "
if /i "%open%"=="Y" (
    if exist "target\site\pmd.html" (
        start "" "target\site\pmd.html"
    ) else if exist "target\reports\pmd\pmd.html" (
        start "" "target\reports\pmd\pmd.html"
    )
)

REM 返回 reports 目录
cd reports

pause
