@echo off
chcp 65001 >nul
REM 测试代码质量工具是否可用

setlocal enabledelayedexpansion

echo ==========================================
echo 测试代码质量工具可用性
echo ==========================================
echo.

REM 检查 Maven
echo 检查 Maven...
call mvn --version >nul 2>&1
if errorlevel 1 (
    echo [✗] Maven 不可用 - 请先安装 Maven
    goto end
) else (
    echo [✓] Maven 可用
)

REM 检查 Java
echo 检查 Java...
call java -version >nul 2>&1
if errorlevel 1 (
    echo [✗] Java 不可用 - 请先安装 Java
    goto end
) else (
    echo [✓] Java 可用
)

echo.
echo 测试 Maven 插件...
echo.

REM 测试 JaCoCo
echo 检查 JaCoCo...
call mvn help:describe -Dplugin=org.jacoco:jacoco-maven-plugin -Dbrief >nul 2>&1
if errorlevel 1 (
    echo [!] JaCoCo - 需要下载
) else (
    echo [✓] JaCoCo - 已安装
)

REM 测试 SpotBugs
echo 检查 SpotBugs...
call mvn help:describe -Dplugin=com.github.spotbugs:spotbugs-maven-plugin -Dbrief >nul 2>&1
if errorlevel 1 (
    echo [!] SpotBugs - 需要下载
) else (
    echo [✓] SpotBugs - 已安装
)

REM 测试 Checkstyle
echo 检查 Checkstyle...
call mvn help:describe -Dplugin=org.apache.maven.plugins:maven-checkstyle-plugin -Dbrief >nul 2>&1
if errorlevel 1 (
    echo [!] Checkstyle - 需要下载
) else (
    echo [✓] Checkstyle - 已安装
)

REM 测试 PMD
echo 检查 PMD...
call mvn help:describe -Dplugin=org.apache.maven.plugins:maven-pmd-plugin -Dbrief >nul 2>&1
if errorlevel 1 (
    echo [!] PMD - 需要下载
) else (
    echo [✓] PMD - 已安装
)

echo.
echo ==========================================
echo 测试完成
echo ==========================================
echo.
echo 提示：
echo   - 标记为 '需要下载' 的插件会在首次运行时自动下载
echo   - 运行 'run-quality-checks.bat' 开始质量检查
echo.

:end
endlocal
pause
