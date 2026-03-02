@echo off
chcp 65001 >nul
echo ========================================
echo  强制将 AIDL 文件转换为 UTF-8 编码
echo ========================================
echo.

REM 检查 Python 是否可用
python --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到 Python，请安装 Python 3
    echo.
    echo 或者使用 Android Studio 手动修复:
    echo 1. 打开每个 AIDL 文件
    echo 2. File -^> Save with Encoding -^> UTF-8
    echo.
    pause
    exit /b 1
)

echo 正在运行 Python 脚本转换文件编码...
echo.
python "%~dp0force-utf8-aidl.py"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo  转换完成！请运行: gradlew clean build
    echo ========================================
) else (
    echo.
    echo ========================================
    echo  转换过程中出现错误
    echo ========================================
)

echo.
pause

